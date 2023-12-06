package me.whizvox.infiniplots.flag;

import me.whizvox.infiniplots.plot.PlotWorld;
import me.whizvox.infiniplots.util.ChunkPos;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class FlagHelper {

  public static boolean allowPlayerAction(PlotWorld plotWorld, Player player, Location location, String flag) {
    if (!player.hasPermission("infiniplots.bypass." + flag)) {
      boolean allowed;
      int plotNumber = plotWorld.generator.getPlotNumber(new ChunkPos(location));
      if (plotNumber < 1) {
        // not in a plot
        allowed = false;
      } else {
        boolean isEditor = plotWorld.isPlotEditor(player, plotNumber);
        Flags flags = plotWorld.getPlotFlags(plotNumber);
        // override potential world flag protection
        if (flags.contains(flag)) {
          allowed = flags.getValue(flag).isAllowed(isEditor);
        } else {
          allowed = plotWorld.worldFlags.getValue(flag).isAllowed(isEditor);
        }
      }
      return allowed;
    }
    return true;
  }

  public static boolean allowNaturalAction(PlotWorld plotWorld, Location location, String flag) {
    ChunkPos pos = new ChunkPos(location);
    boolean inPlot = plotWorld.generator.inPlot(pos.x(), pos.z());
    boolean worldFlagAllowed = plotWorld.worldFlags.getValue(flag).isAllowed();
    if (inPlot) {
      int plotNumber = plotWorld.generator.getPlotNumber(pos);
      Flags flags = plotWorld.getPlotFlags(plotNumber);
      if (flags.contains(flag)) {
        return flags.getValue(flag).isAllowed();
      } else {
        return worldFlagAllowed;
      }
    }
    return worldFlagAllowed;
  }

}
