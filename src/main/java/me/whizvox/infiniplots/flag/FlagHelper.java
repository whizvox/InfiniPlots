package me.whizvox.infiniplots.flag;

import me.whizvox.infiniplots.plot.PlotWorld;
import me.whizvox.infiniplots.util.ChunkPos;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Set;

public class FlagHelper {

  private static final Set<String> INTERACT_FLAGS = Set.of(
      "interact", "damageMobs", "inventoryAccess", "ride", "itemFrameRotation", "fireworkDamage", "expDrops",
      "potionSplash", "itemPickup", "itemDrop", "portalTeleport", "entityDispense", "itemDispense"
  );

  private static boolean lockdownPrevents(PlotWorld plotWorld, String flag) {
    return switch (plotWorld.lockdownLevel) {
      case OFF -> false;
      case BUILD -> !INTERACT_FLAGS.contains(flag);
      default -> true;
    };
  }

  public static boolean allowPlayerAction(PlotWorld plotWorld, Player player, Location location, String flag) {
    if (!player.hasPermission("infiniplots.bypass." + flag)) {
      boolean bypassLockdown;
      if (INTERACT_FLAGS.contains(flag)) {
        bypassLockdown = player.hasPermission("infiniplots.bypass.lockdown.interact");
      } else {
        bypassLockdown = player.hasPermission("infiniplots.bypass.lockdown.build");
      }
      if (!bypassLockdown && lockdownPrevents(plotWorld, flag)) {
        return false;
      }
      boolean allowed;
      int plotNumber = plotWorld.generator.getWorldNumber(new ChunkPos(location));
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
    if (lockdownPrevents(plotWorld, flag)) {
      return false;
    }
    ChunkPos pos = new ChunkPos(location);
    boolean inPlot = plotWorld.generator.inPlot(pos.x(), pos.z());
    boolean worldFlagAllowed = plotWorld.worldFlags.getValue(flag).isAllowed();
    if (inPlot) {
      int plotNumber = plotWorld.generator.getWorldNumber(pos);
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
