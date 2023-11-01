package me.whizvox.infiniplots.plot;

import me.whizvox.infiniplots.db.PlotMemberRepository;
import me.whizvox.infiniplots.db.PlotRepository;
import me.whizvox.infiniplots.db.PlotWorldRepository;
import me.whizvox.infiniplots.util.ChunkPos;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

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

    plotRepo.forEach(plot -> {
      plotIds.add(plot.id());
      plotWorlds.computeIfAbsent(plot.world(), worldId -> new PlotWorld()).add(plot);
    }, true);
    worldRepo.forEach(props -> {
      plotWorlds.computeIfAbsent(props.id(), id -> new PlotWorld()).setNextChunkPos(props.nextPos());
    });
  }

  public boolean importWorld(World world) {
    UUID worldId = world.getUID();
    if (plotWorlds.containsKey(worldId)) {
      return false;
    }
    PlotWorld pWorld = new PlotWorld();
    plotWorlds.put(worldId, pWorld);
    worldRepo.insert(new PlotWorldProperties(worldId, pWorld.getNextAvailableChunkPos()));
    return true;
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
