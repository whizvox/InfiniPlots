package me.whizvox.infiniplots.plot;

import me.whizvox.infiniplots.InfiniPlots;
import me.whizvox.infiniplots.db.*;
import me.whizvox.infiniplots.util.InfPlotUtils;
import me.whizvox.infiniplots.worldgen.PlotWorldGenerator;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Stream;

public class PlotManager {

  private final Map<UUID, PlotWorld> worlds;

  private final PlotMemberRepository memberRepo;
  private final PlotFlagsRepository plotFlagsRepo;
  private final WorldFlagsRepository worldFlagsRepo;
  private final PlotRepository plotRepo;
  private final PlotWorldRepository worldRepo;

  private String defaultWorldName;
  private PlotWorld defaultWorld;

  public PlotManager(Connection conn) {
    worlds = new HashMap<>();

    memberRepo = new PlotMemberRepository(conn);
    plotFlagsRepo = new PlotFlagsRepository(conn);
    worldFlagsRepo = new WorldFlagsRepository(conn);
    plotRepo = new PlotRepository(conn, memberRepo, plotFlagsRepo);
    worldRepo = new PlotWorldRepository(conn, worldFlagsRepo);

    defaultWorldName = null;
    defaultWorld = null;
  }

  public PlotRepository getPlotRepository() {
    return plotRepo;
  }

  public PlotWorldRepository getWorldRepository() {
    return worldRepo;
  }

  public PlotMemberRepository getMemberRepository() {
    return memberRepo;
  }

  public PlotFlagsRepository getPlotFlagsRepository() {
    return plotFlagsRepo;
  }

  public WorldFlagsRepository getWorldFlagsRepository() {
    return worldFlagsRepo;
  }

  public Stream<PlotWorld> plotWorlds() {
    return worlds.values().stream();
  }

  @Nullable
  public PlotWorld getPlotWorld(UUID worldId) {
    return worlds.get(worldId);
  }

  @Nullable
  public Plot getPlot(PlotId plotId, boolean populate) {
    return plotRepo.get(plotId.world(), plotId.plot(), populate);
  }

  @Nullable
  public Plot getPlot(UUID ownerId, int ownerPlotId, boolean populate) {
    return plotRepo.getOneWithOwner(ownerId, ownerPlotId, populate);
  }

  public List<Plot> getPlots(UUID ownerId, boolean populate) {
    return plotRepo.getByOwner(ownerId, populate);
  }

  @Nullable
  public String getDefaultWorldName() {
    return defaultWorldName;
  }

  @Nullable
  public PlotWorld getDefaultWorld() {
    return defaultWorld;
  }

  public void sync() throws SQLException {
    worlds.clear();

    plotRepo.initialize();
    memberRepo.initialize();
    plotFlagsRepo.initialize();
    worldFlagsRepo.initialize();
    worldRepo.initialize();

    defaultWorldName = InfiniPlots.getInstance().getConfig().getString(InfiniPlots.CFG_DEFAULT_PLOT_WORLD);
    defaultWorld = null;
    worldRepo.forEach(props -> {
      PlotWorldGenerator generator = InfiniPlots.getInstance().getPlotGenRegistry().getGenerator(props.generator());
      if (generator != null) {
        World world = Bukkit.getWorld(props.id());
        if (world == null) {
          File worldFolder = InfPlotUtils.getWorldFolder(props.name());
          if (worldFolder.exists() && InfPlotUtils.isWorldFolder(worldFolder)) {
            world = generator.createWorld(props.name());
          } else {
            InfiniPlots.getInstance().getLogger().log(Level.WARNING, "Could not load plot world %s as its world folder does not exist", new Object[] {props.name()});
            return;
          }
        }
        // possible for this to fail when loading via the world folder
        if (props.id().equals(world.getUID())) {
          PlotWorld plotWorld = new PlotWorld(props.name(), generator, world);
          plotWorld.worldFlags.addAll(props.flags());
          plotWorld.nextPlotNumber = plotRepo.getLastPlotNumber(props.id()) + 1;
          worlds.put(props.id(), plotWorld);
          if (props.name().equals(defaultWorldName)) {
            defaultWorld = plotWorld;
          }
        } else {
          InfiniPlots.getInstance().getLogger().log(Level.WARNING, "Could not load plot world %s with unexpected unique ID. Expected: %s, Actual: %s", new Object[] {props.name(), props.id(), world.getUID()});
        }
      } else {
        InfiniPlots.getInstance().getLogger().log(Level.WARNING, "Could not load plot world (%s) since its generator (%s) is not registered", new Object[] {props.id(), props.generator()});
      }
    }, true);
    Set<UUID> unknownPlotWorlds = new HashSet<>();
    plotRepo.forEach(plot -> {
      PlotWorld plotWorld = getPlotWorld(plot.world());
      if (plotWorld != null) {
        plotWorld.add(plot);
      } else {
        if (unknownPlotWorlds.add(plot.world())) {
          InfiniPlots.getInstance().getLogger().log(Level.WARNING, "Found plot belonging to unknown world %s", plot.world());
        }
      }
    }, true);
  }

  @Nullable
  public PlotWorld importWorld(World world, PlotWorldGenerator generator, boolean writeToDatabase) {
    UUID worldId = world.getUID();
    if (worlds.containsKey(worldId)) {
      return null;
    }
    // technically don't need to do this if it isn't necessary to write to the database, but it's good to check if the
    // passed generator is registered or not
    String generatorKey = InfiniPlots.getInstance().getPlotGenRegistry().getKey(generator);
    if (generatorKey == null) {
      throw new IllegalArgumentException("Attempted to use unregistered generator of type " + generator.getClass());
    }
    PlotWorld plotWorld = new PlotWorld(world.getName(), generator, world);
    if (writeToDatabase) {
      // TODO Implement default world flags
      worldRepo.insert(new PlotWorldProperties(worldId, world.getName(), generatorKey, LockdownLevel.OFF, Set.of()));
    }
    worlds.put(worldId, plotWorld);
    return plotWorld;
  }

  public void deleteWorldPlotData(World world) {
    UUID worldId = world.getUID();
    worldRepo.delete(worldId);
    plotRepo.removeByWorld(worldId);
  }

  public void deletePlotData(PlotId plotId) {
    plotRepo.remove(plotId.world(), plotId.plot());
  }

  @Nullable
  public Plot addPlot(UUID worldId, int worldPlotNumber, UUID ownerId, int ownerPlotNumber) {
    PlotWorld world = worlds.get(worldId);
    if (world != null) {
      Plot plot = new Plot(worldId, worldPlotNumber, ownerId, ownerPlotNumber, Set.of(), Set.of());
      plotRepo.insert(plot);
      world.add(plot);
      return plot;
    }
    return null;
  }

  public void removePlot(UUID ownerId, int ownerPlotId) {
    Plot plot = getPlot(ownerId, ownerPlotId, false);
    if (plot == null) {
      return;
    }
    memberRepo.removePlot(plot.world(), plot.worldPlotId());
    plotFlagsRepo.removePlot(plot.world(), plot.worldPlotId());
    plotRepo.remove(plot.world(), plot.worldPlotId());
    Objects.requireNonNull(getPlotWorld(plot.world())).remove(plot.worldPlotId());
  }

}
