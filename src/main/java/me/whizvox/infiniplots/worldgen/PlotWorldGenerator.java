package me.whizvox.infiniplots.worldgen;

import me.whizvox.infiniplots.util.ChunkPos;
import me.whizvox.infiniplots.util.SquarePlotUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.jetbrains.annotations.Nullable;

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
    return (chunkX < 0 ? Math.abs(chunkX - regionWidth - 2) : chunkX) % regionWidth < plotWidth;
  }

  public boolean inPlotZ(int chunkZ) {
    return (chunkZ < 0 ? Math.abs(chunkZ - regionDepth - 2) : chunkZ) % regionDepth < plotDepth;
  }

  public boolean inPlot(int chunkX, int chunkZ) {
    return inPlotX(chunkX) && inPlotZ(chunkZ);
  }

  public int getPlotNumber(ChunkPos pos) {
    if (!inPlot(pos.x(), pos.z())) {
      return 0;
    }
    return SquarePlotUtils.getPlotNumber(pos, regionWidth, regionDepth);
  }

  @Nullable
  public ChunkPos getPosition(int plot) {
    if (plot < 1) {
      return null;
    }
    return SquarePlotUtils.getChunkPos(plot, regionWidth, regionDepth);
  }

  @Nullable
  public Location getTeleportLocation(World world, int plot) {
    ChunkPos pos = getPosition(plot);
    if (pos == null) {
      return null;
    }
    return new Location(world, pos.x() * 16 + 7.5, 0, pos.z() * 16 - 3.5, 0, 0);
  }

  public int getMaxClaims() {
    // 1,875,000 = 30,000,000 (max world size) / 16 (blocks in chunk)
    return (int) Math.min((1875000L / regionWidth) * (1875000L / regionDepth), Integer.MAX_VALUE);
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
