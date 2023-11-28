package me.whizvox.infiniplots.plot;

import me.whizvox.infiniplots.flag.Flags;
import me.whizvox.infiniplots.flag.FlagsManager;
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
  public final FlagsManager worldFlags;

  private final Map<Integer, Set<UUID>> allEditors;
  private final Map<Integer, Flags> plotFlags;

  public PlotWorld(String name, PlotWorldGenerator generator, World world) {
    this.name = name;
    this.generator = generator;
    this.world = world;
    nextPlotNumber = 1;
    worldFlags = new FlagsManager();
    worldFlags.setDefaults();

    allEditors = new HashMap<>();
    plotFlags = new HashMap<>();
  }

  public Map<Integer, Set<UUID>> getAllEditors() {
    return Collections.unmodifiableMap(allEditors);
  }

  public Flags getWorldFlags() {
    return worldFlags;
  }

  public Flags getPlotFlags(int plotNumber) {
    return Objects.requireNonNullElse(plotFlags.get(plotNumber), Flags.EMPTY);
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

  public boolean isPlotEditor(Player player, int plotNumber) {
    if (plotNumber > 0) {
      Set<UUID> plotEditors = allEditors.get(plotNumber);
      if (plotEditors == null) {
        return false;
      }
      return plotEditors.contains(player.getUniqueId());
    }
    return false;
  }

  public boolean isPlotEditor(Player player, Location location) {
    int plotNum = generator.getPlotNumber(new ChunkPos(location));
    return isPlotEditor(player, plotNum);
  }

  public void add(Plot plot) {
    Set<UUID> editors = new HashSet<>(plot.members());
    editors.add(plot.owner());
    allEditors.put(plot.worldPlotId(), Collections.unmodifiableSet(editors));
    if (!plot.flags().isEmpty()) {
      plotFlags.put(plot.worldPlotId(), plot.flags());
    }
  }

  public void remove(int wid) {
    allEditors.remove(wid);
    plotFlags.remove(wid);
  }

}
