package me.whizvox.infiniplots.db;

import me.whizvox.infiniplots.InfiniPlots;
import me.whizvox.infiniplots.plot.Plot;
import me.whizvox.infiniplots.util.ChunkPos;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;

public class PlotRepository extends Repository {

  private static final String
      CREATE_TABLE = "CREATE TABLE IF NOT EXISTS plots(id CHAR(36) PRIMARY KEY, owner CHAR(36) NOT NULL, local_id TINYINT NOT NULL, world CHAR(36) NOT NULL, x INT NOT NULL, z INT NOT NULL, UNIQUE (owner,local_id))",
      SELECT_ALL = "SELECT id,owner,local_id,world,x,z FROM plots",
      SELECT_ONE = SELECT_ALL + " WHERE id=?",
      SELECT_BY_OWNER = SELECT_ALL + " WHERE owner=?",
      SELECT_BY_OWNER_AND_LOCAL_ID = SELECT_ALL + " WHERE owner=? AND local_id=?",
      SELECT_LATEST_LOCAL_ID = "SELECT local_id WHERE owner=? ORDER BY local_id LIMIT 1",
      INSERT = "INSERT INTO plots id,owner,local_id,world,x,z VALUES ?,?,?,?,?,?",
      UPDATE_FORMAT = "UPDATE plots SET %s WHERE id=?",
      UPDATE_OWNER = UPDATE_FORMAT.formatted("owner=?"),
      DELETE = "DELETE FROM plots WHERE id=?",
      DELETE_BY_WORLD = "DELETE FROM plots WHERE world=?";

  private final PlotMemberRepository memberRepo;

  public PlotRepository(Connection conn, PlotMemberRepository memberRepo) {
    super(conn);
    this.memberRepo = memberRepo;
  }

  @Override
  public void initialize() {
    execute(CREATE_TABLE);
  }

  @Nullable
  public Plot getById(UUID id, boolean populate) {
    return executeQuery(SELECT_ONE, List.of(id), rs -> {
      if (rs.next()) {
        return fromRow(rs, populate);
      } else {
        return null;
      }
    });
  }

  public List<Plot> getByOwner(UUID owner, boolean populate) {
    return executeQuery(SELECT_BY_OWNER, List.of(owner), rs -> {
      List<Plot> plots = new ArrayList<>();
      while (rs.next()) {
        plots.add(fromRow(rs, populate));
      }
      plots.sort(Comparator.comparingInt(Plot::localId));
      return plots;
    });
  }

  @Nullable
  public Plot getByOwner(UUID owner, int localId, boolean populate) {
    return executeQuery(SELECT_BY_OWNER_AND_LOCAL_ID, List.of(owner, localId), rs -> {
      if (rs.next()) {
        return fromRow(rs, populate);
      }
      return null;
    });
  }

  /**
   * Fetch the latest local ID of an owner's collection of plots.
   * @param owner The unique ID of the owner
   * @return The latest local ID if the owner has at least 1 plot, or 0 if the owner has no plots
   * @see Plot#localId()
   */
  public int getLatestLocalId(UUID owner) {
    return executeQuery(SELECT_LATEST_LOCAL_ID, List.of(owner), rs -> {
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

  public void insert(Plot plot) {
    executeUpdate(INSERT, List.of(plot.id(), plot.owner(), plot.localId(), plot.world(), plot.pos().x(), plot.pos().z()));
    if (plot.members() != null && !plot.members().isEmpty()) {
      plot.members().forEach(memberId -> memberRepo.addMember(plot.id(), memberId));
    }
  }

  public void updateOwner(UUID plotId, UUID newOwner) {
    Plot plot = getById(plotId, false);
    executeUpdate(UPDATE_OWNER, List.of(newOwner, plotId));
    memberRepo.removeMember(plotId, newOwner);
    memberRepo.addMember(plotId, plot.owner());
  }

  public void remove(UUID id) {
    memberRepo.removePlot(id);
    executeUpdate(DELETE, List.of(id));
  }

  public void removeByWorld(UUID worldId) {
    memberRepo.removeWorld(worldId);
    int rows = executeUpdate(DELETE_BY_WORLD, List.of(worldId));
    if (rows > 0) {
      InfiniPlots.getInstance().getLogger().log(Level.INFO, "Deleted {} rows from plots table while deleting world {}", new Object[] {rows, worldId});
    }
  }

  private Plot fromRow(ResultSet rs, boolean populate) throws SQLException {
    UUID id = UUID.fromString(rs.getString(1));
    UUID owner = UUID.fromString(rs.getString(2));
    int localId = rs.getByte(3);
    UUID world = UUID.fromString(rs.getString(4));
    int x = rs.getInt(5);
    int z = rs.getInt(6);
    Set<UUID> members;
    if (populate) {
      members = new HashSet<>(memberRepo.getMembers(id));
    } else {
      members = Set.of();
    }
    return new Plot(id, owner, localId, Collections.unmodifiableSet(members), world, new ChunkPos(x, z));
  }

}
