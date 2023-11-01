package me.whizvox.infiniplots.db;

import me.whizvox.infiniplots.plot.PlotWorldProperties;
import me.whizvox.infiniplots.util.ChunkPos;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class PlotWorldRepository extends Repository {

  private static final String
      CREATE_TABLE = "CREATE TABLE IF NOT EXISTS worlds(id CHAR(36) NOT NULL, next_x INT NOT NULL, next_z INT NOT NULL)",
      SELECT_ALL = "SELECT id,next_x,next_z FROM worlds",
      SELECT_ONE = SELECT_ALL + " WHERE id=?",
      INSERT = "INSERT INTO worlds (id,next_x,next_z) VALUES (?,?,?)",
      UPDATE_NEXT = "UPDATE worlds SET next_x=?,next_z=? WHERE id=?",
      DELETE = "DELETE FROM worlds WHERE id=?";

  public PlotWorldRepository(Connection conn) {
    super(conn);
  }

  @Override
  public void initialize() {
    execute(CREATE_TABLE);
  }

  public void forEach(Consumer<PlotWorldProperties> consumer) {
    executeQuery(SELECT_ALL, List.of(), rs -> {
      while (rs.next()) {
        PlotWorldProperties world = fromRow(rs);
        consumer.accept(world);
      }
      return null;
    });
  }

  @Nullable
  public PlotWorldProperties get(UUID worldId) {
    return executeQuery(SELECT_ONE, List.of(worldId), rs -> {
      if (rs.next()) {
        return fromRow(rs);
      } else {
        return null;
      }
    });
  }

  public void insert(PlotWorldProperties props) {
    executeUpdate(INSERT, List.of(props.id(), props.nextPos().x(), props.nextPos().z()));
  }

  public void updateNextPos(UUID worldId, ChunkPos nextPos) {
    executeUpdate(UPDATE_NEXT, List.of(nextPos.x(), nextPos.z(), worldId));
  }

  public void delete(UUID worldId) {
    executeUpdate(DELETE, List.of(worldId));
  }

  private static PlotWorldProperties fromRow(ResultSet rs) throws SQLException {
    UUID id = UUID.fromString(rs.getString(1));
    int nextX = rs.getInt(2);
    int nextZ = rs.getInt(3);
    return new PlotWorldProperties(id, new ChunkPos(nextX, nextZ));
  }

}
