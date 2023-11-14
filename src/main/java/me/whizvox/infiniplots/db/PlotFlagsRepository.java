package me.whizvox.infiniplots.db;

import me.whizvox.infiniplots.flag.PlotProtectionFlag;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class PlotFlagsRepository extends Repository {

  private static final String
      CREATE = "CREATE TABLE IF NOT EXISTS plot_flags(world CHAR(36) NOT NULL, plot INT NOT NULL, flag VARCHAR(63) NOT NULL, UNIQUE (world,plot,flag))",
      SELECT_ALL = "SELECT world,plot,flag FROM plot_flags",
      SELECT_BY_PLOT = "SELECT flag FROM plot_flags WHERE world=? AND plot=?",
      INSERT = "INSERT INTO plot_flags (world,plot,flag) VALUES (?,?,?)",
      DELETE_ONE = "DELETE FROM plot_flags WHERE world=? AND plot=? AND flag=?",
      DELETE_BY_PLOT = "DELETE FROM plot_flags WHERE world=? AND plot=?",
      DELETE_BY_WORLD = "DELETE FROM plot_flags WHERE world=?";

  public PlotFlagsRepository(Connection conn) {
    super(conn);
  }

  @Override
  public void initialize() {
    execute(CREATE);
  }

  public void forEach(Consumer<PlotProtectionFlag> consumer) {
    executeQuery(SELECT_ALL, List.of(), rs -> {
      while (rs.next()) {
        UUID worldId = UUID.fromString(rs.getString(1));
        int plotId = rs.getInt(2);
        String flag = rs.getString(3);
        consumer.accept(new PlotProtectionFlag(worldId, plotId, flag));
      }
      return null;
    });
  }

  public List<String> getFlags(UUID worldId, int plotId) {
    return executeQuery(SELECT_BY_PLOT, List.of(worldId, plotId), rs -> {
      List<String> flags = new ArrayList<>();
      while (rs.next()) {
        flags.add(rs.getString(1));
      }
      return flags;
    });
  }

  public void insert(UUID worldId, int plotId, String flag) {
    executeUpdate(INSERT, List.of(worldId, plotId, flag));
  }

  public void removePlotFlag(UUID worldId, int plotId, String flag) {
    executeUpdate(DELETE_ONE, List.of(worldId, plotId, flag));
  }

  public void removePlot(UUID worldId, int plotId) {
    executeUpdate(DELETE_BY_PLOT, List.of(worldId, plotId));
  }

  public void removeWorld(UUID worldId) {
    executeUpdate(DELETE_BY_WORLD, List.of(worldId));
  }

}
