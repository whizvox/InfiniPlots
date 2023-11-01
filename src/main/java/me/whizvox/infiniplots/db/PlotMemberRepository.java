package me.whizvox.infiniplots.db;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlotMemberRepository extends Repository {

  private static final String
      CREATE_TABLE = "CREATE TABLE IF NOT EXISTS plot_members(" +
          "plot CHAR(36) NOT NULL, " +
          "member CHAR(36) NOT NULL, " +
          "world CHAR(36) NOT NULL, " +
          "PRIMARY KEY (plot, member)" +
      ")",
      SELECT_MEMBERS = "SELECT member FROM plot_members WHERE plot=?",
      INSERT = "INSERT INTO plot_members (plot,member) VALUES (?,?)",
      DELETE_MEMBER = "DELETE FROM plot_members WHERE plot=? AND member=?",
      DELETE_PLOT = "DELETE FROM plot_members WHERE plot=?",
      DELETE_WORLD = "DELETE FROM plot_members WHERE world=?";

  public PlotMemberRepository(Connection conn) {
    super(conn);
  }

  @Override
  public void initialize() {
    execute(CREATE_TABLE);
  }

  public List<UUID> getMembers(UUID plotId) {
    return executeQuery(SELECT_MEMBERS, List.of(plotId), rs -> {
      List<UUID> members = new ArrayList<>();
      while (rs.next()) {
        members.add(UUID.fromString(rs.getString(1)));
      }
      return members;
    });
  }

  public void addMember(UUID plotId, UUID memberId) {
    executeUpdate(INSERT, List.of(plotId, memberId));
  }

  public void removeMember(UUID plotId, UUID memberId) {
    executeUpdate(DELETE_MEMBER, List.of(plotId, memberId));
  }

  public void removePlot(UUID plotId) {
    executeUpdate(DELETE_PLOT, List.of(plotId));
  }

  public void removeWorld(UUID worldId) {
    executeUpdate(DELETE_WORLD, List.of(worldId));
  }

}
