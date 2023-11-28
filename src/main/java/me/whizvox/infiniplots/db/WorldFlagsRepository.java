package me.whizvox.infiniplots.db;

import me.whizvox.infiniplots.flag.Flag;
import me.whizvox.infiniplots.flag.FlagValue;
import me.whizvox.infiniplots.flag.WorldFlag;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class WorldFlagsRepository extends Repository {

  private static final String
      CREATE = "CREATE TABLE IF NOT EXISTS world_flags(" +
        "world CHAR(36) NOT NULL, " +
        "flag VARCHAR(255) NOT NULL, " +
        "value TINYINT NOT NULL, " +
        "UNIQUE (world, flag)" +
      ")",
      SELECT_ALL = "SELECT world,flag,value FROM world_flags",
      SELECT_BY_WORLD = "SELECT world,flag,value FROM world_flags WHERE world=?",
      INSERT = "INSERT INTO world_flags (world,flag,value) VALUES (?,?,?)",
      DELETE_FLAG = "DELETE FROM world_flags WHERE world=? AND flag=?",
      DELETE_WORLD = "DELETE FROM world_flags WHERE world=?";

  public WorldFlagsRepository(Connection conn) {
    super(conn);
  }

  @Override
  public void initialize() {
    executeUpdate(CREATE);
  }

  public void forEach(Consumer<WorldFlag> consumer) {
    executeQuery(SELECT_ALL, List.of(), rs -> {
      while (rs.next()) {
        consumer.accept(fromRow(rs));
      }
      return null;
    });
  }

  public List<Flag> getFlags(UUID worldId) {
    return executeQuery(SELECT_BY_WORLD, List.of(worldId), rs -> {
      List<Flag> flags = new ArrayList<>();
      while (rs.next()) {
        flags.add(fromRow(rs).flag());
      }
      return flags;
    });
  }

  public void insertAll(UUID worldId, Iterable<Flag> flags) {
    try (PreparedStatement stmt = conn.prepareStatement(INSERT)) {
      for (Flag flag : flags) {
        stmt.setString(1, worldId.toString());
        stmt.setString(2, flag.name());
        stmt.setByte(3, (byte) flag.value().ordinal());
        stmt.addBatch();
      }
      stmt.executeBatch();
    } catch (SQLException e) {
      throw new RuntimeException("Could not insert into database", e);
    }
  }

  public void insert(UUID worldId, String flag, FlagValue value) {
    executeUpdate(INSERT, List.of(worldId, flag, value));
  }

  public void remove(UUID worldId, String flag) {
    executeUpdate(DELETE_FLAG, List.of(worldId, flag));
  }

  public void removeWorld(UUID worldId) {
    executeUpdate(DELETE_WORLD, List.of(worldId));
  }

  private static WorldFlag fromRow(ResultSet rs) throws SQLException {
    UUID worldId = UUID.fromString(rs.getString(1));
    String flag = rs.getString(2);
    FlagValue value = FlagValue.from(rs.getByte(3));
    return new WorldFlag(worldId, new Flag(flag, value));
  }

}
