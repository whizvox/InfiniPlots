package me.whizvox.infiniplots.util;

import me.whizvox.infiniplots.InfiniPlots;
import me.whizvox.infiniplots.plot.PlotWorld;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.util.BoundingBox;

import java.io.File;
import java.util.Random;
import java.util.UUID;

public class WorldUtils {

  public static File getWorldFolder(String worldName) {
    return new File(InfiniPlots.getInstance().getServer().getWorldContainer(), worldName);
  }

  public static boolean isWorldFolder(File worldFolder) {
    return new File(worldFolder, "level.dat").exists();
  }

  public static PlotWorld getPlotWorldOrDefault(UUID worldId) {
    PlotWorld plotWorld = InfiniPlots.getInstance().getPlotManager().getPlotWorld(worldId);
    if (plotWorld == null) {
      return InfiniPlots.getInstance().getPlotManager().getDefaultWorld();
    }
    return plotWorld;
  }

}
