package me.whizvox.infiniplots.db;

import me.whizvox.infiniplots.plot.LockdownLevel;
import me.whizvox.infiniplots.plot.PlotWorldProperties;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;

public class PlotWorldRepository extends Repository {

  private static final String
      CREATE_TABLE = "CREATE TABLE IF NOT EXISTS worlds(" +
        "id CHAR(36) NOT NULL PRIMARY KEY, " +
        "name VARCHAR(255) NOT NULL UNIQUE, " +
        "generator VARCHAR(255) NOT NULL, " +
        "lockdown TINYINT NOT NULL" +
      ")",
      SELECT_ALL = "SELECT id,name,generator,lockdown FROM worlds",
      SELECT_ONE = SELECT_ALL + " WHERE id=?",
      INSERT = "INSERT INTO worlds (id,name,generator,lockdown) VALUES (?,?,?,?)",
      UPDATE_LOCKDOWN = "UPDATE worlds SET lockdown=? WHERE id=?",
      DELETE = "DELETE FROM worlds WHERE id=?";

  private final WorldFlagsRepository worldFlagsRepo;

  public PlotWorldRepository(Connection conn, WorldFlagsRepository worldFlagsRepo) {
    super(conn);
    this.worldFlagsRepo = worldFlagsRepo;
  }

  @Override
  public void initialize() {
    execute(CREATE_TABLE);
  }

  public void forEach(Consumer<PlotWorldProperties> consumer, boolean populate) {
    executeQuery(SELECT_ALL, List.of(), rs -> {
      while (rs.next()) {
        PlotWorldProperties world = fromRow(rs, populate);
        consumer.accept(world);
      }
      return null;
    });
  }

  @Nullable
  public PlotWorldProperties get(UUID worldId, boolean populate) {
    return executeQuery(SELECT_ONE, List.of(worldId), rs -> {
      if (rs.next()) {
        return fromRow(rs, populate);
      } else {
        return null;
      }
    });
  }

  public void insert(PlotWorldProperties props) {
    executeUpdate(INSERT, List.of(props.id(), props.name(), props.generator(), props.lockdown()));
    worldFlagsRepo.insertAll(props.id(), props.flags());
  }

  public void updateLockdownLevel(UUID worldId, LockdownLevel lockdown) {
    executeUpdate(UPDATE_LOCKDOWN, List.of(worldId, lockdown));
  }

  public void delete(UUID worldId) {
    worldFlagsRepo.removeWorld(worldId);
    executeUpdate(DELETE, List.of(worldId));
  }

  private PlotWorldProperties fromRow(ResultSet rs, boolean populate) throws SQLException {
    UUID id = UUID.fromString(rs.getString(1));
    String worldName = rs.getString(2);
    String generatorName = rs.getString(3);
    LockdownLevel lockdown = LockdownLevel.from(rs.getByte(4));
    if (lockdown == null) {
      lockdown = LockdownLevel.OFF;
    }
    Set<String> flags;
    if (populate) {
      flags = new HashSet<>(worldFlagsRepo.getFlags(id));
    } else {
      flags = Set.of();
    }
    return new PlotWorldProperties(id, worldName, generatorName, lockdown, Collections.unmodifiableSet(flags));
  }

}
