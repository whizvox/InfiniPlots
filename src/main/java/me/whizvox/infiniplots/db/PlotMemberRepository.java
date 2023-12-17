package me.whizvox.infiniplots.db;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlotMemberRepository extends Repository {

  private static final String
      CREATE_TABLE = "CREATE TABLE IF NOT EXISTS plot_members(" +
          "world CHAR(36) NOT NULL, " +
          "world_number INT NOT NULL, " +
          "member CHAR(36) NOT NULL, " +
          "UNIQUE (world, world_number, member)" +
      ")",
      SELECT_MEMBERS = "SELECT member FROM plot_members WHERE world=? AND world_number=?",
      INSERT = "INSERT INTO plot_members (world,world_number,member) VALUES (?,?,?)",
      DELETE_MEMBER = "DELETE FROM plot_members WHERE world=? AND world_number=? AND member=?",
      DELETE_PLOT = "DELETE FROM plot_members WHERE world=? AND world_number=?",
      DELETE_WORLD = "DELETE FROM plot_members WHERE world=?";

  public PlotMemberRepository(Connection conn) {
    super(conn);
  }

  @Override
  public void initialize() {
    execute(CREATE_TABLE);
  }

  public List<UUID> getMembers(UUID worldId, int worldNumber) {
    return executeQuery(SELECT_MEMBERS, List.of(worldId, worldNumber), rs -> {
      List<UUID> members = new ArrayList<>();
      while (rs.next()) {
        members.add(UUID.fromString(rs.getString(1)));
      }
      return members;
    });
  }

  public void addMember(UUID worldId, int worldNumber, UUID memberId) {
    executeUpdate(INSERT, List.of(worldId, worldNumber, memberId));
  }

  public void removeMember(UUID worldId, int worldNumber, UUID memberId) {
    executeUpdate(DELETE_MEMBER, List.of(worldId, worldNumber, memberId));
  }

  public void removePlot(UUID worldId, int worldNumber) {
    executeUpdate(DELETE_PLOT, List.of(worldId, worldNumber));
  }

  public void removeWorld(UUID worldId) {
    executeUpdate(DELETE_WORLD, List.of(worldId));
  }

}
