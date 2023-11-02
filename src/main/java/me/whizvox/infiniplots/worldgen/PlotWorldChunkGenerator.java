package me.whizvox.infiniplots.worldgen;

import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;

import java.util.Random;

public abstract class PlotWorldChunkGenerator extends ChunkGenerator {

  @Override
  public boolean shouldGenerateSurface() {
    return true;
  }

  @Override
  public void generateBedrock(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
    chunkData.setRegion(0, worldInfo.getMinHeight(), 0, 16, worldInfo.getMinHeight() + 1, 16, Material.BEDROCK);
  }

  @Override
  public abstract void generateSurface(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData);

}
