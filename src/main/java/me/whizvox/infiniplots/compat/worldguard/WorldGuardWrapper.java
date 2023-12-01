package me.whizvox.infiniplots.compat.worldguard;

import me.whizvox.infiniplots.plot.PlotWorld;
import org.jetbrains.annotations.Nullable;

//
// Continue working on this later...
//
public class WorldGuardWrapper {

  public void setFlag(PlotWorld plotWorld, int plotNumber, String flag, @Nullable String value) {
  }

  public void updateEditors(PlotWorld plotWorld, int plotNumber) {
  }

  private static WorldGuardWrapper instance = null;

  public static boolean init() {
    /*Plugin worldGuard = Bukkit.getPluginManager().getPlugin("WorldGuard");
    if (worldGuard == null && InfiniPlots.getInstance().getConfig().getBoolean(InfiniPlots.CFG_USE_WORLDGUARD)) {
      instance = new WorldGuardWrapper();
      return false;
    } else {
      InfiniPlots.getInstance().getLogger().log(Level.INFO, "WorldGuard detected");
      instance = new WorldGuardWrapperImpl();
      return true;
    }*/
    instance = new WorldGuardWrapper();
    return false;
  }

  public static WorldGuardWrapper getInstance() {
    return instance;
  }

}
