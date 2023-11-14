package me.whizvox.infiniplots.db;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlotMemberRepository extends Repository {

  private static final String
      CREATE_TABLE = "CREATE TABLE IF NOT EXISTS plot_members(" +
          "world CHAR(36) NOT NULL, " +
          "world_plot_id INT NOT NULL, " +
          "member CHAR(36) NOT NULL, " +
          "UNIQUE (world, world_plot_id, member)" +
      ")",
      SELECT_MEMBERS = "SELECT member FROM plot_members WHERE world=? AND world_plot_id=?",
      INSERT = "INSERT INTO plot_members (world,world_plot_id,member) VALUES (?,?,?)",
      DELETE_MEMBER = "DELETE FROM plot_members WHERE world=? AND world_plot_id=? AND member=?",
      DELETE_PLOT = "DELETE FROM plot_members WHERE world=? AND world_plot_id=?",
      DELETE_WORLD = "DELETE FROM plot_members WHERE world=?";

  public PlotMemberRepository(Connection conn) {
    super(conn);
  }

  @Override
  public void initialize() {
    execute(CREATE_TABLE);
  }

  public List<UUID> getMembers(UUID worldId, int worldPlotId) {
    return executeQuery(SELECT_MEMBERS, List.of(worldId, worldPlotId), rs -> {
      List<UUID> members = new ArrayList<>();
      while (rs.next()) {
        members.add(UUID.fromString(rs.getString(1)));
      }
      return members;
    });
  }

  public void addMember(UUID worldId, int worldPlotId, UUID memberId) {
    executeUpdate(INSERT, List.of(worldId, worldPlotId, memberId));
  }

  public void removeMember(UUID worldId, int worldPlotId, UUID memberId) {
    executeUpdate(DELETE_MEMBER, List.of(worldId, worldPlotId, memberId));
  }

  public void removePlot(UUID worldId, int worldPlotId) {
    executeUpdate(DELETE_PLOT, List.of(worldId, worldPlotId));
  }

  public void removeWorld(UUID worldId) {
    executeUpdate(DELETE_WORLD, List.of(worldId));
  }

}
