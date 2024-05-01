package me.whizvox.infiniplots.plot;

import me.whizvox.infiniplots.InfiniPlots;
import me.whizvox.infiniplots.db.*;
import me.whizvox.infiniplots.flag.Flags;
import me.whizvox.infiniplots.util.PlotId;
import me.whizvox.infiniplots.util.WorldUtils;
import me.whizvox.infiniplots.worldgen.PlotWorldGenerator;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
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
  private String kickDestinationWorldName;
  private World kickDestinationWorld;

  public PlotManager(Connection conn) {
    worlds = new HashMap<>();

    memberRepo = new PlotMemberRepository(conn);
    plotFlagsRepo = new PlotFlagsRepository(conn);
    worldFlagsRepo = new WorldFlagsRepository(conn);
    plotRepo = new PlotRepository(conn, memberRepo, plotFlagsRepo);
    worldRepo = new PlotWorldRepository(conn, worldFlagsRepo);

    defaultWorldName = null;
    defaultWorld = null;
    kickDestinationWorldName = null;
    kickDestinationWorld = null;
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
  public String getDefaultWorldName() {
    return defaultWorldName;
  }

  @Nullable
  public PlotWorld getDefaultWorld() {
    return defaultWorld;
  }

  public String getKickDestinationWorldName() {
    return kickDestinationWorldName;
  }

  public World getKickDestinationWorld() {
    return kickDestinationWorld;
  }

  /**
   * Attempt to get either the plot world that the player is standing in or the default plot world if the player is not
   * in a plot world.
   * @param player The player to check
   * @return The plot world the player is standing in, or the default. Returns <code>null</code> if the player is not
   * standing in a plot world and the default plot world is not configured.
   */
  @Nullable
  public PlotWorld getPlotWorld(Player player) {
    PlotWorld plotWorld = worlds.get(player.getWorld().getUID());
    if (plotWorld == null) {
      return defaultWorld;
    }
    return plotWorld;
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
          File worldFolder = WorldUtils.getWorldFolder(props.name());
          if (worldFolder.exists() && WorldUtils.isWorldFolder(worldFolder)) {
            world = generator.createWorld(props.name());
          } else {
            InfiniPlots.getInstance().getLogger().log(Level.WARNING, "Could not load plot world %s as its world folder does not exist", new Object[] {props.name()});
            return;
          }
        }
        // possible for this to fail when loading via the world folder
        if (props.id().equals(world.getUID())) {
          PlotWorld plotWorld = new PlotWorld(props.name(), generator, world, props.lockdown());
          plotWorld.worldFlags.set(props.flags());
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
    if (defaultWorld == null) {
      if (worlds.size() == 1) {
        String oldDefaultWorldName = defaultWorldName;
        defaultWorld = worlds.values().stream().findFirst().get();
        defaultWorldName = defaultWorld.name;
        InfiniPlots.getInstance().getConfig().set(InfiniPlots.CFG_DEFAULT_PLOT_WORLD, defaultWorldName);
        InfiniPlots.getInstance().saveConfig();
        InfiniPlots.getInstance().getLogger().log(Level.INFO, "Default plot world %s either not found or isn't a plot world, setting it to %s instead", new Object[] {oldDefaultWorldName, defaultWorldName});
      } else {
        InfiniPlots.getInstance().getLogger().log(Level.WARNING, "Default plot world %s either not found or isn't a plot world", defaultWorldName);
      }
    }
    kickDestinationWorldName = InfiniPlots.getInstance().getConfig().getString(InfiniPlots.CFG_KICK_DESTINATION_WORLD);
    kickDestinationWorld = Bukkit.getWorld(kickDestinationWorldName);
    if (kickDestinationWorld == null) {
      InfiniPlots.getInstance().getLogger().log(Level.WARNING, "Kick destination world %s does not exist", kickDestinationWorldName);
    }
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
    PlotWorld plotWorld = new PlotWorld(world.getName(), generator, world, LockdownLevel.OFF);
    if (writeToDatabase) {
      worldRepo.insert(new PlotWorldProperties(worldId, plotWorld.name, generatorKey, plotWorld.lockdownLevel, Flags.EMPTY));
    }
    worlds.put(worldId, plotWorld);
    if (defaultWorld == null && worlds.size() == 1) {
      defaultWorld = plotWorld;
      defaultWorldName = defaultWorld.name;
      InfiniPlots.getInstance().getConfig().set(InfiniPlots.CFG_DEFAULT_PLOT_WORLD, defaultWorldName);
      InfiniPlots.getInstance().saveConfig();
      InfiniPlots.getInstance().getLogger().log(Level.INFO, "Setting default plot world to %s", new Object[] {defaultWorldName});
    }
    return plotWorld;
  }

  /* ========= *
   *   PLOTS   *
   * ========= */

  @Nullable
  public Plot getPlot(PlotId id, boolean populate) {
    return plotRepo.get(id, populate);
  }

  public List<Plot> getPlots(UUID ownerId, boolean populate) {
    return plotRepo.getByOwner(ownerId, populate);
  }

  @Nullable
  public Plot addPlot(UUID worldId, int worldNumber, UUID ownerId, int ownerNumber) {
    PlotWorld world = worlds.get(worldId);
    if (world != null) {
      Plot plot = new Plot(worldId, worldNumber, ownerId, ownerNumber, Set.of(), Flags.EMPTY);
      plotRepo.insert(plot);
      world.add(plot);
      return plot;
    }
    return null;
  }

  public void removePlot(PlotId plotId) {
    Plot plot = plotRepo.get(plotId, false);
    if (plot != null) {
      plotRepo.remove(PlotId.fromWorld(plot));
      Objects.requireNonNull(getPlotWorld(plot.world())).remove(plot.worldNumber());
    }
  }

  /* =========== *
   *   MEMBERS   *
   * =========== */

  public List<UUID> getMembers(PlotId plotId) {
    plotId = plotRepo.getWorldBasedId(plotId);
    return memberRepo.getMembers(plotId.world(), plotId.worldNumber());
  }

  public void addMember(PlotId plotId, UUID member) {
    plotId = plotRepo.getWorldBasedId(plotId);
    memberRepo.addMember(plotId.world(), plotId.worldNumber(), member);
    getPlotWorld(plotId.world()).addEditor(plotId.worldNumber(), member);
  }

  public void removeMember(PlotId plotId, UUID memberId) {
    plotId = plotRepo.getWorldBasedId(plotId);
    memberRepo.removeMember(plotId.world(), plotId.worldNumber(), memberId);
    getPlotWorld(plotId.world()).removeEditor(plotId.worldNumber(), memberId);
  }

  public void clearMembers(PlotId plotId) {
    Plot plot = getPlot(plotId, false);
    if (plot != null) {
      memberRepo.removePlot(plot.world(), plot.worldNumber());
      getPlotWorld(plot.world()).clearMembers(plot.worldNumber(), plot.owner());
    }
  }

}
