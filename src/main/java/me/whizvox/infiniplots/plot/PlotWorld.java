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
  public int nextPlotNumber;
  public final Set<String> worldFlags;

  private final Map<Integer, Set<UUID>> allEditors;
  private final Map<Integer, Set<String>> allFlags;

  public PlotWorld(String name, PlotWorldGenerator generator, World world) {
    this.name = name;
    this.generator = generator;
    this.world = world;
    nextPlotNumber = 1;
    worldFlags = new HashSet<>();
    allEditors = new HashMap<>();
    allFlags = new HashMap<>();
  }

  public Map<Integer, Set<UUID>> getAllEditors() {
    return Collections.unmodifiableMap(allEditors);
  }

  public Map<Integer, Set<String>> getAllFlags() {
    return Collections.unmodifiableMap(allFlags);
  }

  public boolean isClaimed(int plotNumber) {
    return allEditors.containsKey(plotNumber);
  }

  public int calculateNextUnclaimedPlot() {
    int n = Math.max(1, nextPlotNumber);
    while (isClaimed(n)) {
      n++;
    }
    if (n > generator.getMaxClaims()) {
      return 0;
    }
    return n;
  }

  public boolean canEdit(Player player, Location location) {
    int plotNum = generator.getPlotNumber(new ChunkPos(location));
    if (plotNum > 0) {
      Set<UUID> plotEditors = allEditors.get(plotNum);
      if (plotEditors == null) {
        return false;
      }
      return plotEditors.contains(player.getUniqueId());
    }
    return false;
  }

  public boolean hasFlag(int plotNumber, String flag) {
    if (worldFlags.contains(flag)) {
      return true;
    }
    Set<String> plotFlags = allFlags.get(plotNumber);
    if (plotFlags == null) {
      return false;
    }
    return plotFlags.contains(flag);
  }

  public void add(Plot plot) {
    Set<UUID> editors = new HashSet<>(plot.members());
    editors.add(plot.owner());
    allEditors.put(plot.worldPlotId(), Collections.unmodifiableSet(editors));
    allFlags.put(plot.worldPlotId(), Collections.unmodifiableSet(plot.flags()));
  }

  public void remove(Plot plot) {
    allEditors.remove(plot.worldPlotId());
    allFlags.remove(plot.worldPlotId());
  }

}
