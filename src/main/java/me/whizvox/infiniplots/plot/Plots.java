package me.whizvox.infiniplots.plot;

import me.whizvox.infiniplots.InfiniPlots;
import me.whizvox.infiniplots.db.PlotMemberRepository;
import me.whizvox.infiniplots.db.PlotRepository;
import me.whizvox.infiniplots.db.PlotWorldRepository;
import me.whizvox.infiniplots.util.ChunkPos;
import me.whizvox.infiniplots.util.InfPlotUtils;
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

public class Plots {

  private final Connection conn;

  private final Set<UUID> plotIds;
  private final Map<UUID, PlotWorld> plotWorlds;

  private final PlotMemberRepository memberRepo;
  private final PlotRepository plotRepo;
  private final PlotWorldRepository worldRepo;

  public Plots(Connection conn) {
    this.conn = conn;
    plotIds = new HashSet<>();
    plotWorlds = new HashMap<>();

    memberRepo = new PlotMemberRepository(conn);
    plotRepo = new PlotRepository(conn, memberRepo);
    worldRepo = new PlotWorldRepository(conn);
  }

  public Stream<Map.Entry<UUID, PlotWorld>> plotWorlds() {
    return plotWorlds.entrySet().stream();
  }

  @Nullable
  public PlotWorld getPlotWorld(UUID worldId) {
    return plotWorlds.get(worldId);
  }

  public boolean doesPlotExist(UUID plotId) {
    return plotIds.contains(plotId);
  }

  @Nullable
  public Plot getPlot(UUID plotId, boolean populate) {
    return plotRepo.getById(plotId, populate);
  }

  @Nullable
  public Plot getPlot(UUID ownerId, int localId, boolean populate) {
    return plotRepo.getByOwner(ownerId, localId, populate);
  }

  public List<Plot> getPlots(UUID ownerId, boolean populate) {
    return plotRepo.getByOwner(ownerId, populate);
  }

  public void sync() throws SQLException {
    plotIds.clear();
    plotWorlds.clear();

    plotRepo.initialize();
    memberRepo.initialize();
    worldRepo.initialize();

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
          plotWorld.setNextChunkPos(props.nextPos());
          plotWorlds.put(props.id(), plotWorld);
        } else {
          InfiniPlots.getInstance().getLogger().log(Level.WARNING, "Could not load plot world %s with unexpected unique ID. Expected: %s, Actual: %s", new Object[] {props.name(), props.id(), world.getUID()});
        }
      } else {
        InfiniPlots.getInstance().getLogger().log(Level.WARNING, "Could not load plot world (%s) since its generator (%s) is not registered", new Object[] {props.id(), props.generator()});
      }
    });
    plotRepo.forEach(plot -> {
      plotIds.add(plot.id());
      PlotWorld plotWorld = plotWorlds.get(plot.world());
      if (plotWorld != null) {
        plotWorld.add(plot);
      } else {
        InfiniPlots.getInstance().getLogger().log(Level.WARNING, "Could not load plot (%s, (%d,%d)) since its world (%s) could not be found", new Object[] {plot.id(), plot.pos().x(), plot.pos().z(), plot.world()});
      }
    }, true);
  }

  @Nullable
  public PlotWorld importWorld(World world, PlotWorldGenerator generator, boolean writeToDatabase) {
    UUID worldId = world.getUID();
    if (plotWorlds.containsKey(worldId)) {
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
      worldRepo.insert(new PlotWorldProperties(worldId, world.getName(), generatorKey, plotWorld.getNextAvailableChunkPos()));
    }
    plotWorlds.put(worldId, plotWorld);
    return plotWorld;
  }

  public void deleteWorldPlotData(World world) {
    UUID worldId = world.getUID();
    worldRepo.delete(worldId);
    plotRepo.removeByWorld(worldId);
  }

  public void deletePlotData(UUID plotId) {
    plotRepo.remove(plotId);
  }

  @Nullable
  public ChunkPos getNextAvailableChunkPos(UUID worldId) {
    PlotWorld world = getPlotWorld(worldId);
    if (world == null) {
      return null;
    }
    return world.getNextAvailableChunkPos();
  }

  public void updateNextChunkPos(UUID worldId) {
    PlotWorld world = plotWorlds.get(worldId);
    if (world != null) {
      worldRepo.updateNextPos(worldId, world.getNextAvailableChunkPos());
    }
  }

  @Nullable
  public Plot addPlot(Player owner, int localId, UUID worldId, ChunkPos pos) {
    PlotWorld world = plotWorlds.get(worldId);
    if (world != null) {
      Plot plot = new Plot(UUID.randomUUID(), owner.getUniqueId(), localId, Set.of(), worldId, pos);
      plotRepo.insert(plot);
      world.add(plot);
      return plot;
    }
    return null;
  }

}
