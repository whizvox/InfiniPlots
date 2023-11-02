package me.whizvox.infiniplots.worldgen;

import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;

import java.util.Random;

public class PlotWorldChunkDebugGenerator extends ChunkGenerator {

  @Override
  public boolean shouldGenerateSurface() {
    return true;
  }

  @Override
  public void generateBedrock(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
    chunkData.setRegion(0, worldInfo.getMinHeight(), 0, 16, worldInfo.getMinHeight() + 1, 16, Material.BEDROCK);
  }

  @Override
  public void generateSurface(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
    chunkData.setRegion(0, worldInfo.getMinHeight() + 1, 0, 16, -1, 16, Material.DIRT);
    chunkData.setRegion(0, -1, 0, 16, 0, 16, Material.GRASS_BLOCK);
    boolean xAligned = chunkX % 2 == 0;
    boolean zAligned = chunkZ % 2 == 0;
    if (!xAligned && !zAligned) {
      // 4 corners
      chunkData.setBlock(0, 0, 0, Material.SMOOTH_STONE_SLAB);
      chunkData.setBlock(15, 0, 0, Material.SMOOTH_STONE_SLAB);
      chunkData.setBlock(0, 0, 15, Material.SMOOTH_STONE_SLAB);
      chunkData.setBlock(15, 0, 15, Material.SMOOTH_STONE_SLAB);
    } else if (xAligned && !zAligned) {
      // rows across X axis
      chunkData.setRegion(0, 0, 0, 16, 1, 1, Material.SMOOTH_STONE_SLAB);
      chunkData.setRegion(0, 0, 15, 16, 1, 16, Material.SMOOTH_STONE_SLAB);
    } else if (!xAligned) {
      // columns across Z axis
      chunkData.setRegion(0, 0, 0, 1, 1, 16, Material.SMOOTH_STONE_SLAB);
      chunkData.setRegion(15, 0, 0, 16, 1, 16, Material.SMOOTH_STONE_SLAB);
    }
  }

}
