package me.whizvox.infiniplots.util;

import me.whizvox.infiniplots.InfiniPlots;
import me.whizvox.infiniplots.exception.InterruptCommandException;
import me.whizvox.infiniplots.plot.Plot;
import me.whizvox.infiniplots.plot.PlotWorld;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class CommandHelper {

  public static PlotWorld getPlotWorld(String plotWorldName) {
    World world = Bukkit.getWorld(plotWorldName);
    if (world == null) {
      throw new InterruptCommandException("World " + plotWorldName + " does not exist");
    }
    PlotWorld plotWorld = InfiniPlots.getInstance().getPlotManager().getPlotWorld(world.getUID());
    if (plotWorld == null) {
      throw new InterruptCommandException("World " + plotWorldName + " is not a plot world");
    }
    return plotWorld;
  }

  public static PlotWorld getPlotWorld(Player player) {
    PlotWorld plotWorld = WorldUtils.getPlotWorldOrDefault(player.getWorld().getUID());
    if (plotWorld == null) {
      throw new InterruptCommandException("Default plot world is not set up");
    }
    return plotWorld;
  }

  public static PlotWorld getDefaultPlotWorld() {
    PlotWorld plotWorld = InfiniPlots.getInstance().getPlotManager().getDefaultWorld();
    if (plotWorld == null) {
      throw new InterruptCommandException("Default plot world is not set up");
    }
    return plotWorld;
  }

  public static int getWorldNumber(PlotWorld plotWorld, Player player) {
    int worldNum = plotWorld.generator.getWorldNumber(new ChunkPos(player.getLocation()));
    if (worldNum < 1) {
      throw new InterruptCommandException("Not standing in a plot");
    }
    return worldNum;
  }

  public static OfflinePlayer getOfflinePlayer(String playerName) {
    OfflinePlayer player = PlayerUtils.getOfflinePlayer(playerName);
    if (player == null) {
      throw new InterruptCommandException("Could not find player " + playerName);
    }
    return player;
  }

  public static Plot getPlot(PlotId plotId, boolean populate) {
    Plot plot = InfiniPlots.getInstance().getPlotManager().getPlot(plotId, populate);
    if (plot == null) {
      throw new InterruptCommandException("Could not find plot");
    }
    return plot;
  }

}
