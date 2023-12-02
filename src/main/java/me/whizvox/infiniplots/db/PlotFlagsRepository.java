package me.whizvox.infiniplots.db;

import me.whizvox.infiniplots.flag.Flag;
import me.whizvox.infiniplots.flag.FlagValue;
import me.whizvox.infiniplots.flag.PlotFlag;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class PlotFlagsRepository extends Repository {

  private static final String
      CREATE = "CREATE TABLE IF NOT EXISTS plot_flags(" +
        "world CHAR(36) NOT NULL, " +
        "plot INT NOT NULL, " +
        "flag VARCHAR(63) NOT NULL, " +
        "value TINYINT NOT NULL, " +
        "UNIQUE (world,plot,flag)" +
      ")",
      SELECT_ALL = "SELECT world,plot,flag,value FROM plot_flags",
      SELECT_BY_PLOT = "SELECT world,plot,flag,value FROM plot_flags WHERE world=? AND plot=?",
      SELECT_FLAG = "SELECT value FROM plot_flags WHERE world=? AND plot=? AND flag=?",
      INSERT = "INSERT INTO plot_flags (world,plot,flag,value) VALUES (?,?,?,?)",
      UPDATE = "UPDATE plot_flags SET value=? WHERE world=? AND plot=? AND flag=?",
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

  public void forEach(Consumer<PlotFlag> consumer) {
    executeQuery(SELECT_ALL, List.of(), rs -> {
      while (rs.next()) {
        consumer.accept(fromRow(rs));
      }
      return null;
    });
  }

  public List<Flag> getFlags(UUID worldId, int plotId) {
    return executeQuery(SELECT_BY_PLOT, List.of(worldId, plotId), rs -> {
      List<Flag> flags = new ArrayList<>();
      while (rs.next()) {
        flags.add(fromRow(rs).flag());
      }
      return flags;
    });
  }

  @Nullable
  public FlagValue getFlagValue(UUID worldId, int plotId, String flag) {
    return executeQuery(SELECT_FLAG, List.of(worldId, plotId, flag), rs -> {
      if (rs.next()) {
        return FlagValue.from(rs.getByte(1));
      }
      return null;
    });
  }

  public void insert(UUID worldId, int plotId, String flag, FlagValue value) {
    executeUpdate(INSERT, List.of(worldId, plotId, flag, value));
  }

  public void update(UUID worldId, int plotId, String flag, FlagValue value) {
    executeUpdate(UPDATE, List.of(value, worldId, plotId, flag));
  }

  public void insertOrUpdate(UUID worldId, int plotId, String flag, FlagValue value) {
    if (getFlagValue(worldId, plotId, flag) == null) {
      insert(worldId, plotId, flag, value);
    } else {
      update(worldId, plotId, flag, value);
    }
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

  private static PlotFlag fromRow(ResultSet rs) throws SQLException {
    UUID worldId = UUID.fromString(rs.getString(1));
    int plotNumber = rs.getInt(2);
    String flag = rs.getString(3);
    FlagValue value = FlagValue.from(rs.getByte(4));
    return new PlotFlag(worldId, plotNumber, new Flag(flag, value));
  }

}
