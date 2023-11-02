package me.whizvox.infiniplots.worldgen;

import org.bukkit.World;
import org.bukkit.WorldCreator;

public abstract class PlotWorldGenerator {

  public final int plotWidth;
  public final int plotDepth;
  public final int xPadding;
  public final int zPadding;
  public final int regionWidth;
  public final int regionDepth;

  public PlotWorldGenerator(int plotWidth, int plotDepth, int xPadding, int zPadding) {
    this.plotWidth = plotWidth;
    this.plotDepth = plotDepth;
    this.xPadding = xPadding;
    this.zPadding = zPadding;
    regionWidth = xPadding + plotWidth;
    regionDepth = zPadding + plotDepth;
  }

  public boolean inPlotX(int chunkX) {
    return (chunkX < 0 ? Math.abs(chunkX - 1) : chunkX) % regionWidth < plotWidth;
  }

  public boolean inPlotZ(int chunkZ) {
    return (chunkZ < 0 ? Math.abs(chunkZ - 1) : chunkZ) % regionDepth < plotDepth;
  }

  public boolean inPlot(int chunkX, int chunkZ) {
    return inPlotX(chunkX) && inPlotX(chunkZ);
  }

  public abstract WorldCreator getWorldCreator(String name);

  protected abstract void configureWorld(World world);

  public World createWorld(String name) {
    WorldCreator creator = getWorldCreator(name);
    World world = creator.createWorld();
    configureWorld(world);
    return world;
  }

}
