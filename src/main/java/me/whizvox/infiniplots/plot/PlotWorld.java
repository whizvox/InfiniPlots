package me.whizvox.infiniplots.plot;

import me.whizvox.infiniplots.util.ChunkPos;
import me.whizvox.infiniplots.worldgen.PlotWorldGenerator;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;

public class PlotWorld {

  public final String name;
  public final PlotWorldGenerator generator;
  public final World world;

  private final Map<ChunkPos, UUID> allPositions;
  private final Map<ChunkPos, Set<UUID>> allEditors;
  private ChunkPos nextPos;

  public PlotWorld(String name, PlotWorldGenerator generator, World world) {
    this.name = name;
    this.generator = generator;
    this.world = world;
    allPositions = new HashMap<>();
    allEditors = new HashMap<>();
    nextPos = new ChunkPos(0, 0);
  }

  public ChunkPos getPlotPos(ChunkPos pos) {
    if (generator.inPlot(pos.x(), pos.z())) {
      int x, z;
      // get the southwest-most corner
      if (pos.x() >= 0) {
        x = pos.x() - (pos.x() % generator.regionWidth);
      } else {
        x = pos.x() - ((pos.x() - 1) % generator.regionWidth) - 1;
      }
      if (pos.z() >= 0) {
        z = pos.z() - (pos.z() % generator.regionDepth);
      } else {
        z = pos.z() - ((pos.z() - 1) % generator.regionDepth) - 1;
      }
      return new ChunkPos(x, z);
    } else {
      return null;
    }
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

  public boolean canEdit(Player player, Location location) {
    ChunkPos pos = getPlotPos(new ChunkPos(location));
    if (pos != null) {
      Set<UUID> editors = allEditors.get(pos);
      if (editors == null) {
        return false;
      }
      return editors.contains(player.getUniqueId());
    }
    return false;
  }

  public ChunkPos getNextAvailableChunkPos() {
    int x = nextPos.x();
    int z = nextPos.z();
    ChunkPos pos = new ChunkPos(x, z);
    while (allPositions.containsKey(pos)) {
      if (x == 0 && z == 0) {
        z = generator.regionDepth;
      } else if (x == -2 && z > 0) {
        x = 0;
        z += generator.regionDepth;
      } else {
        int max = Math.max(Math.abs(x), Math.abs(z));
        if (z == max && x < max) {
          x += generator.regionWidth;
        } else if (z == -max && x > -max) {
          x -= generator.regionWidth;
        } else if (x == max) {
          z -= generator.regionDepth;
        } else {
          z += generator.regionDepth;
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
