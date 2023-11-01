package me.whizvox.infiniplots.worldgen;

import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;

import java.util.Random;

public class PlotWorldChunkGenerator extends ChunkGenerator {

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
    chunkData.setRegion(0, worldInfo.getMinHeight() + 1, 0, 16, 0, 16, Material.DIRT);
    chunkData.setRegion(0, 0, 0, 16, 1, 16, Material.GRASS);
    chunkData.setRegion(0, 0, 0, 16, 1, 1, Material.SMOOTH_STONE_SLAB);
    chunkData.setRegion(0, 0, 15, 16, 1, 16, Material.SMOOTH_STONE_SLAB);
    chunkData.setRegion(0, 0, 1, 1, 1, 15, Material.SMOOTH_STONE_SLAB);
    chunkData.setRegion(15, 0, 1, 15, 1, 15, Material.SMOOTH_STONE_SLAB);
  }

}
