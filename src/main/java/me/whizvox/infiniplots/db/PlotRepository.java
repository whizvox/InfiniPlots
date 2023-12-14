package me.whizvox.infiniplots.db;

import me.whizvox.infiniplots.InfiniPlots;
import me.whizvox.infiniplots.flag.Flag;
import me.whizvox.infiniplots.flag.Flags;
import me.whizvox.infiniplots.flag.FlagsManager;
import me.whizvox.infiniplots.plot.Plot;
import me.whizvox.infiniplots.plot.PlotId;
import me.whizvox.infiniplots.util.Page;
import me.whizvox.infiniplots.util.ProtectionFlags;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;

public class PlotRepository extends Repository {

  private static final int PAGE_SIZE = 10;

  private static final String
      CREATE_TABLE = "CREATE TABLE IF NOT EXISTS plots(" +
        "world CHAR(36) NOT NULL, " +
        "world_plot_id INT NOT NULL, " +
        "owner CHAR(36) NOT NULL, " +
        "owner_plot_id TINYINT NOT NULL, " +
        "UNIQUE (world,world_plot_id)," +
        "UNIQUE (owner,owner_plot_id)" +
      ")",
      SELECT_ALL = "SELECT world,world_plot_id,owner,owner_plot_id FROM plots",
      SELECT_BY_WORLD = SELECT_ALL + " WHERE world=? ORDER BY world_plot_id LIMIT ? OFFSET ?",
      SELECT_BY_WORLD_AND_ID = SELECT_ALL + " WHERE world=? AND world_plot_id=?",
      SELECT_BY_OWNER = SELECT_ALL + " WHERE owner=? ORDER BY owner_plot_id",
      SELECT_BY_OWNER_AND_ID = SELECT_ALL + " WHERE owner=? AND owner_plot_id=?",
      SELECT_ALL_OWNERS = "SELECT DISTINCT owner FROM plots",
      SELECT_LAST_PLOT_NUMBER = "SELECT world_plot_id FROM plots WHERE world=? ORDER BY world_plot_id ASC LIMIT 1",
      SELECT_COUNT = "SELECT COUNT(*) FROM plots",
      INSERT = "INSERT INTO plots (world,world_plot_id,owner,owner_plot_id) VALUES (?,?,?,?)",
      UPDATE_FORMAT = "UPDATE plots SET %s WHERE world=? AND world_plot_id=?",
      UPDATE_OWNER = UPDATE_FORMAT.formatted("owner=?"),
      DELETE = "DELETE FROM plots WHERE world=? AND world_plot_id=?",
      DELETE_BY_WORLD = "DELETE FROM plots WHERE world=?";

  private final PlotMemberRepository memberRepo;
  private final PlotFlagsRepository plotFlagsRepo;

  public PlotRepository(Connection conn, PlotMemberRepository memberRepo, PlotFlagsRepository plotFlagsRepo) {
    super(conn);
    this.memberRepo = memberRepo;
    this.plotFlagsRepo = plotFlagsRepo;
  }

  @Override
  public void initialize() {
    execute(CREATE_TABLE);
  }

  public int getCount() {
    return executeQuery(SELECT_COUNT, List.of(), rs -> {
      rs.next();
      return rs.getInt(1);
    });
  }

  @Nullable
  public Plot get(UUID worldId, int worldPlotId, boolean populate) {
    return executeQuery(SELECT_BY_WORLD_AND_ID, List.of(worldId, worldPlotId), rs -> {
      if (rs.next()) {
        return fromRow(rs, populate);
      } else {
        return null;
      }
    });
  }

  public Page<Plot> getByWorld(UUID worldId, int page, boolean populate) {
    return executeQuery(SELECT_BY_WORLD, List.of(worldId, PAGE_SIZE, page * PAGE_SIZE), rs -> {
      List<Plot> items = new ArrayList<>();
      while (rs.next()) {
        items.add(fromRow(rs, populate));
      }
      // FIXME Use count by world instead of total row count
      int count = getCount();
      return new Page<>(page, PAGE_SIZE, count, count / PAGE_SIZE, items);
    });
  }

  public List<Plot> getByOwner(UUID owner, boolean populate) {
    return executeQuery(SELECT_BY_OWNER, List.of(owner), rs -> {
      List<Plot> plots = new ArrayList<>();
      while (rs.next()) {
        plots.add(fromRow(rs, populate));
      }
      plots.sort(Comparator.comparingInt(Plot::ownerPlotId));
      return plots;
    });
  }

  @Nullable
  public Plot getOneWithOwner(UUID owner, int localId, boolean populate) {
    return executeQuery(SELECT_BY_OWNER_AND_ID, List.of(owner, localId), rs -> {
      if (rs.next()) {
        return fromRow(rs, populate);
      }
      return null;
    });
  }

  public int getLastPlotNumber(UUID worldId) {
    return executeQuery(SELECT_LAST_PLOT_NUMBER, List.of(worldId), rs -> {
      if (rs.next()) {
        return rs.getInt(1);
      }
      return 0;
    });
  }

  public void forEach(Consumer<Plot> consumer, boolean populate) {
    executeQuery(SELECT_ALL, List.of(), rs -> {
      while (rs.next()) {
        Plot plot = fromRow(rs, populate);
        consumer.accept(plot);
      }
      return null;
    });
  }

  public void forEachOwner(Consumer<UUID> consumer) {
    executeQuery(SELECT_ALL_OWNERS, List.of(), rs -> {
      while (rs.next()) {
        UUID ownerId = UUID.fromString(rs.getString(1));
        consumer.accept(ownerId);
      }
      return null;
    });
  }

  public void insert(Plot plot) {
    executeUpdate(INSERT, List.of(plot.world(), plot.worldPlotId(), plot.owner(), plot.ownerPlotId()));
    if (plot.members() != null) {
      plot.members().forEach(memberId -> memberRepo.addMember(plot.world(), plot.worldPlotId(), memberId));
    }
    if (plot.flags() != null && !plot.flags().isEmpty()) {
      plot.flags().forEach(flag -> plotFlagsRepo.insert(plot.world(), plot.ownerPlotId(), flag.name(), flag.value()));
    }
  }

  public void updateOwner(UUID worldId, int worldPlotId, UUID newOwner) {
    Plot plot = get(worldId, worldPlotId, false);
    if (plot != null) {
      executeUpdate(UPDATE_OWNER, List.of(newOwner, worldId, worldPlotId));
      memberRepo.removeMember(worldId, worldPlotId, newOwner);
      memberRepo.addMember(worldId, worldPlotId, plot.owner());
    }
  }

  public void updateFlags(PlotId plotId, @Nullable Iterable<Flag> flagsToAdd, @Nullable Iterable<String> flagsToRemove) {
    if (flagsToAdd != null) {
      flagsToAdd.forEach(flag -> plotFlagsRepo.insert(plotId.world(), plotId.plot(), flag.name(), flag.value()));
    }
    if (flagsToRemove != null) {
      flagsToRemove.forEach(flag -> plotFlagsRepo.removePlotFlag(plotId.world(), plotId.plot(), flag));
    }
  }

  public void updateFlags(PlotId plotId, Iterable<Flag> flags) {
    plotFlagsRepo.removePlot(plotId.world(), plotId.plot());
    flags.forEach(flag -> plotFlagsRepo.insert(plotId.world(), plotId.plot(), flag.name(), flag.value()));
  }

  public void remove(UUID worldId, int worldPlotId) {
    memberRepo.removePlot(worldId, worldPlotId);
    plotFlagsRepo.removePlot(worldId, worldPlotId);
    executeUpdate(DELETE, List.of(worldId, worldPlotId));
  }

  public void removeByWorld(UUID worldId) {
    memberRepo.removeWorld(worldId);
    plotFlagsRepo.removeWorld(worldId);
    int rows = executeUpdate(DELETE_BY_WORLD, List.of(worldId));
    if (rows > 0) {
      InfiniPlots.getInstance().getLogger().log(Level.INFO, "Deleted {} rows from plots table while deleting world {}", new Object[] {rows, worldId});
    }
  }

  private Plot fromRow(ResultSet rs, boolean populate) throws SQLException {
    UUID world = UUID.fromString(rs.getString(1));
    int worldPlotId = rs.getInt(2);
    UUID owner = UUID.fromString(rs.getString(3));
    int ownerPlotId = rs.getByte(4);
    Set<UUID> members;
    Flags flags;
    if (populate) {
      members = Set.copyOf(memberRepo.getMembers(world, worldPlotId));
      List<Flag> flagsList = plotFlagsRepo.getFlags(world, worldPlotId);
      flags = new FlagsManager();
      ((FlagsManager) flags).set(flagsList);
    } else {
      members = Set.of();
      flags = Flags.EMPTY;
    }
    return new Plot(world, worldPlotId, owner, ownerPlotId, members, flags);
  }

}
