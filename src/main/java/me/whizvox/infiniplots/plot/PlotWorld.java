package me.whizvox.infiniplots.plot;

import me.whizvox.infiniplots.flag.*;
import me.whizvox.infiniplots.util.ChunkPos;
import me.whizvox.infiniplots.worldgen.PlotWorldGenerator;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

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

    allEditors = new HashMap<>();
    plotFlags = new HashMap<>();
  }

  public Map<Integer, Set<UUID>> getAllEditors() {
    return Collections.unmodifiableMap(allEditors);
  }

  public void addEditor(int worldNumber, UUID memberId) {
    Set<UUID> newMembers = new HashSet<>();
    Set<UUID> members = allEditors.get(worldNumber);
    if (members != null) {
      newMembers.addAll(members);
    }
    newMembers.add(memberId);
    allEditors.put(worldNumber, Collections.unmodifiableSet(newMembers));
  }

  public void removeEditor(int worldNumber, UUID memberId) {
    Set<UUID> members = allEditors.get(worldNumber);
    if (members != null) {
      allEditors.put(worldNumber, members.stream().filter(it -> !it.equals(memberId)).collect(Collectors.toSet()));
    }
  }

  public void clearMembers(int worldNumber, UUID ownerId) {
    Set<UUID> members = allEditors.get(worldNumber);
    if (members != null) {
      allEditors.put(worldNumber, Set.of(ownerId));
    }
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
    int plotNum = generator.getWorldNumber(new ChunkPos(location));
    return isPlotEditor(player, plotNum);
  }

  public void add(Plot plot) {
    Set<UUID> editors = new HashSet<>(plot.members());
    editors.add(plot.owner());
    allEditors.put(plot.worldNumber(), Collections.unmodifiableSet(editors));
    if (!plot.flags().isEmpty()) {
      plotFlags.put(plot.worldNumber(), plot.flags());
    }
  }

  public void remove(int wid) {
    allEditors.remove(wid);
    plotFlags.remove(wid);
  }

  public void setPlotFlag(int plotNumber, String flag, @Nullable FlagValue value) {
    Flags flags = plotFlags.get(plotNumber);
    if (flags == null) {
      flags = new FlagsManager();
      if (value != null) {
        ((FlagsManager) flags).set(new Flag(flag, value));
      }
    } else {
      FlagsManager temp = new FlagsManager();
      temp.set(flags);
      if (value == null) {
        temp.clear(flag);
      } else {
        temp.set(new Flag(flag, value));
      }
      flags = new FlagsAdapter(temp);
    }
    plotFlags.put(plotNumber, flags);
  }

}
