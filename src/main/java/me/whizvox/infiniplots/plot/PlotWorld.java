package me.whizvox.infiniplots.plot;

import me.whizvox.infiniplots.util.ChunkPos;
import org.bukkit.entity.Player;

import java.util.*;

public class PlotWorld {

  private final Map<ChunkPos, UUID> allPositions;
  private final Map<ChunkPos, Set<UUID>> allEditors;
  private ChunkPos nextPos;

  public PlotWorld() {
    allPositions = new HashMap<>();
    allEditors = new HashMap<>();
    nextPos = new ChunkPos(0, 0);
  }

  public Map<ChunkPos, UUID> getAllPositions() {
    return Collections.unmodifiableMap(allPositions);
  }

  public Map<ChunkPos, Set<UUID>> getAllEditors() {
    return Collections.unmodifiableMap(allEditors);
  }

  public ChunkPos getNextChunkPos() {
    return nextPos;
  }

  public boolean canEdit(Player player) {
    ChunkPos pos = new ChunkPos(player.getLocation());
    Set<UUID> editors = allEditors.get(pos);
    if (editors == null) {
      return false;
    }
    return editors.contains(player.getUniqueId());
  }

  public ChunkPos getNextAvailableChunkPos() {
    int x = nextPos.x();
    int z = nextPos.z();
    ChunkPos pos = new ChunkPos(x, z);
    while (allPositions.containsKey(pos)) {
      if (x == 0 && z == 0) {
        z = 2;
      } else if (x == -2 && z > 0) {
        x = 0;
        z += 2;
      } else {
        int max = Math.max(Math.abs(x), Math.abs(z));
        if (z == max && x < max) {
          x += 2;
        } else if (z == -max && x > -max) {
          x -= 2;
        } else if (x == max) {
          z -= 2;
        } else {
          z += 2;
        }
      }
      pos = new ChunkPos(x, z);
      // since it's possible for someone to manually pick a plot, we need to check if the "auto-assigned" chunk isn't
      // owned by anyone.
    }
    return pos;
  }

  public void setNextChunkPos(ChunkPos newNextPos) {
    nextPos = newNextPos;
  }

  public void add(Plot plot) {
    allPositions.put(plot.pos(), plot.id());
    Set<UUID> editors = new HashSet<>(plot.members());
    editors.add(plot.owner());
    allEditors.put(plot.pos(), Collections.unmodifiableSet(editors));
  }

  public void remove(Plot plot) {
    allPositions.remove(plot.pos());
    allEditors.remove(plot.pos());
  }

}
