package me.whizvox.infiniplots.db;

import me.whizvox.infiniplots.InfiniPlots;
import me.whizvox.infiniplots.flag.Flag;
import me.whizvox.infiniplots.flag.Flags;
import me.whizvox.infiniplots.flag.FlagsManager;
import me.whizvox.infiniplots.plot.Plot;
import me.whizvox.infiniplots.util.Page;
import me.whizvox.infiniplots.util.PlotId;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;

public class PlotRepository extends Repository {

  private static final int PAGE_SIZE = 10;
  private static final UUID FALLBACK_UUID = UUID.fromString("9af013b5-fb75-4f4b-ad64-971dc9f493a4");

  private static final String
      CREATE_TABLE = "CREATE TABLE IF NOT EXISTS plots(" +
        "world CHAR(36) NOT NULL, " +
        "world_number INT NOT NULL, " +
        "owner CHAR(36) NOT NULL, " +
        "owner_number TINYINT NOT NULL, " +
        "UNIQUE (world,world_number)," +
        "UNIQUE (owner,owner_number)" +
      ")",
      SELECT_ALL = "SELECT world,world_number,owner,owner_number FROM plots",
      SELECT_BY_WORLD = SELECT_ALL + " WHERE world=? ORDER BY world_number LIMIT ? OFFSET ?",
      SELECT_ONE_BY_WORLD = SELECT_ALL + " WHERE world=? AND world_number=?",
      SELECT_BY_OWNER = SELECT_ALL + " WHERE owner=? ORDER BY owner_number",
      SELECT_OWNER_NUMBER_BY_OWNER = "SELECT owner_number FROM plots WHERE owner=?",
      SELECT_ONE_BY_OWNER = SELECT_ALL + " WHERE owner=? AND owner_number=?",
      SELECT_ALL_OWNERS = "SELECT DISTINCT owner FROM plots",
      SELECT_LAST_WORLD_NUMBER = "SELECT world_number FROM plots WHERE world=? ORDER BY world_number ASC LIMIT 1",
      SELECT_COUNT = "SELECT COUNT(*) FROM plots",
      SELECT_WORLD_ID = "SELECT world,world_number FROM plots WHERE owner=? AND owner_number=?",
      INSERT = "INSERT INTO plots (world,world_number,owner,owner_number) VALUES (?,?,?,?)",
      UPDATE_FORMAT = "UPDATE plots SET %s WHERE world=? AND world_number=?",
      UPDATE_OWNER = UPDATE_FORMAT.formatted("owner=?"),
      DELETE = "DELETE FROM plots WHERE world=? AND world_number=?",
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

  public PlotId getWorldBasedId(PlotId id) {
    id.checkValid();
    if (id.hasWorld()) {
      return id;
    }
    return executeQuery(SELECT_WORLD_ID, List.of(id.owner(), id.ownerNumber()), rs -> {
      if (rs.next()) {
        return PlotId.fromWorld(UUID.fromString(rs.getString(1)), rs.getByte(2));
      }
      return PlotId.fromOwner(FALLBACK_UUID, 0);
    });
  }

  @Nullable
  public Plot get(PlotId id, boolean populate) {
    id.checkValid();
    if (id.hasOwner()) {
      return executeQuery(SELECT_ONE_BY_OWNER, List.of(id.owner(), id.ownerNumber()), rs -> {
        if (rs.next()) {
          return fromRow(rs, populate);
        }
        return null;
      });
    }
    return executeQuery(SELECT_ONE_BY_WORLD, List.of(id.world(), id.worldNumber()), rs -> {
      if (rs.next()) {
        return fromRow(rs, populate);
      }
      return null;
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
      plots.sort(Comparator.comparingInt(Plot::ownerNumber));
      return plots;
    });
  }

  public List<Integer> getOwnedPlots(UUID owner) {
    return executeQuery(SELECT_OWNER_NUMBER_BY_OWNER, List.of(owner), rs -> {
      List<Integer> ownerNums = new ArrayList<>();
      while (rs.next()) {
        // unsigned byte
        ownerNums.add(rs.getByte(1) & 0xFF);
      }
      Collections.sort(ownerNums);
      return ownerNums;
    });
  }

  public int getLastPlotNumber(UUID worldId) {
    return executeQuery(SELECT_LAST_WORLD_NUMBER, List.of(worldId), rs -> {
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
    executeUpdate(INSERT, List.of(plot.world(), plot.worldNumber(), plot.owner(), plot.ownerNumber()));
    if (plot.members() != null) {
      plot.members().forEach(memberId -> memberRepo.addMember(plot.world(), plot.worldNumber(), memberId));
    }
    if (plot.flags() != null && !plot.flags().isEmpty()) {
      plot.flags().forEach(flag -> plotFlagsRepo.insert(plot.world(), plot.ownerNumber(), flag.name(), flag.value()));
    }
  }

  public void updateOwner(PlotId id, UUID newOwner) {
    Plot plot = get(id, false);
    if (plot != null) {
      executeUpdate(UPDATE_OWNER, List.of(newOwner, plot.world(), plot.worldNumber()));
      memberRepo.removeMember(plot.world(), plot.worldNumber(), newOwner);
      memberRepo.addMember(plot.world(), plot.worldNumber(), plot.owner());
    }
  }

  public void updateFlags(PlotId id, @Nullable Iterable<Flag> flagsToAdd, @Nullable Iterable<String> flagsToRemove) {
    PlotId wid = getWorldBasedId(id);
    if (flagsToAdd != null) {
      flagsToAdd.forEach(flag -> plotFlagsRepo.insert(wid.world(), wid.worldNumber(), flag.name(), flag.value()));
    }
    if (flagsToRemove != null) {
      flagsToRemove.forEach(flag -> plotFlagsRepo.removePlotFlag(wid.world(), wid.worldNumber(), flag));
    }
  }

  public void updateFlags(PlotId id, Iterable<Flag> flags) {
    PlotId wid = getWorldBasedId(id);
    plotFlagsRepo.removePlot(id.world(), id.worldNumber());
    flags.forEach(flag -> plotFlagsRepo.insert(wid.world(), wid.worldNumber(), flag.name(), flag.value()));
  }

  public void remove(PlotId id) {
    id = getWorldBasedId(id);
    memberRepo.removePlot(id.world(), id.worldNumber());
    plotFlagsRepo.removePlot(id.world(), id.worldNumber());
    executeUpdate(DELETE, List.of(id.world(), id.worldNumber()));
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
    int worldNumber = rs.getInt(2);
    UUID owner = UUID.fromString(rs.getString(3));
    int ownerNumber = rs.getByte(4);
    Set<UUID> members;
    Flags flags;
    if (populate) {
      members = Set.copyOf(memberRepo.getMembers(world, worldNumber));
      List<Flag> flagsList = plotFlagsRepo.getFlags(world, worldNumber);
      flags = new FlagsManager();
      ((FlagsManager) flags).set(flagsList);
    } else {
      members = Set.of();
      flags = Flags.EMPTY;
    }
    return new Plot(world, worldNumber, owner, ownerNumber, members, flags);
  }

}
