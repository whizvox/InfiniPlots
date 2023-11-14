package me.whizvox.infiniplots.db;

import me.whizvox.infiniplots.flag.WorldProtectionFlag;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public class WorldFlagsRepository extends Repository {

  private static final String
      CREATE = "CREATE TABLE IF NOT EXISTS world_flags(" +
        "world CHAR(36) NOT NULL, " +
        "flag VARCHAR(255) NOT NULL, " +
        "UNIQUE (world, flag)" +
      ")",
      SELECT_ALL = "SELECT world,flag FROM world_flags",
      SELECT_BY_WORLD = "SELECT flag FROM world_flags WHERE world=?",
      INSERT = "INSERT INTO world_flags (world,flag) VALUES (?,?)",
      DELETE_FLAG = "DELETE FROM world_flags WHERE world=? AND flag=?",
      DELETE_WORLD = "DELETE FROM world_flags WHERE world=?";

  public WorldFlagsRepository(Connection conn) {
    super(conn);
  }

  @Override
  public void initialize() {
    executeUpdate(CREATE);
  }

  public void forEach(Consumer<WorldProtectionFlag> consumer) {
    executeQuery(SELECT_ALL, List.of(), rs -> {
      while (rs.next()) {
        UUID worldId = UUID.fromString(rs.getString(1));
        String flag = rs.getString(2);
        consumer.accept(new WorldProtectionFlag(worldId, flag));
      }
      return null;
    });
  }

  public List<String> getFlags(UUID worldId) {
    return executeQuery(SELECT_BY_WORLD, List.of(worldId), rs -> {
      List<String> flags = new ArrayList<>();
      while (rs.next()) {
        flags.add(rs.getString(1));
      }
      return flags;
    });
  }

  public void insertAll(UUID worldId, Set<String> flags) {
    try (PreparedStatement stmt = conn.prepareStatement(INSERT)) {
      for (String flag : flags) {
        stmt.setString(1, worldId.toString());
        stmt.setString(2, flag);
        stmt.addBatch();
      }
      stmt.executeBatch();
    } catch (SQLException e) {
      throw new RuntimeException("Could not insert into database", e);
    }
  }

  public void insert(UUID worldId, String flag) {
    executeUpdate(INSERT, List.of(worldId, flag));
  }

  public void remove(UUID worldId, String flag) {
    executeUpdate(DELETE_FLAG, List.of(worldId, flag));
  }

  public void removeWorld(UUID worldId) {
    executeUpdate(DELETE_WORLD, List.of(worldId));
  }

}
