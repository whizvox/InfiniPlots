package me.whizvox.infiniplots.util;

import me.whizvox.infiniplots.InfiniPlots;

import java.io.File;

public class InfPlotUtils {

  public static File getWorldFolder(String worldName) {
    return new File(InfiniPlots.getInstance().getServer().getWorldContainer(), worldName);
  }

  public static boolean isWorldFolder(File worldFolder) {
    return new File(worldFolder, "level.dat").exists();
  }

}
