package me.whizvox.infiniplots.worldgen;

import org.bukkit.GameRule;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;

import java.util.Random;

public class PlotWorldPlainGenerator extends PlotWorldGenerator {

  public PlotWorldPlainGenerator(int plotSize) {
    super(plotSize, plotSize, 1, 1);
  }

  @Override
  public WorldCreator getWorldCreator(String name) {
    return new WorldCreator(name)
        .generateStructures(false)
        .keepSpawnInMemory(false)
        .biomeProvider(ALL_PLAINS)
        .generator(new PlotWorldChunkGenerator() {
          @Override
          public void generateSurface(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
            chunkData.setRegion(0, worldInfo.getMinHeight() + 1, 0, 16, -1, 16, Material.DIRT);
            chunkData.setRegion(0, -1, 0, 16, 0, 16, Material.GRASS_BLOCK);
            boolean inPlotX = inPlotX(chunkX);
            boolean inPlotZ = inPlotZ(chunkZ);
            if (!inPlotX && !inPlotZ) {
              chunkData.setBlock(0, 0, 0, Material.SMOOTH_STONE_SLAB);
              chunkData.setBlock(15, 0, 0, Material.SMOOTH_STONE_SLAB);
              chunkData.setBlock(0, 0, 15, Material.SMOOTH_STONE_SLAB);
              chunkData.setBlock(15, 0, 15, Material.SMOOTH_STONE_SLAB);
            } else if (inPlotX && !inPlotZ) {
              chunkData.setRegion(0, 0, 0, 16, 1, 1, Material.SMOOTH_STONE_SLAB);
              chunkData.setRegion(0, 0, 15, 16, 1, 16, Material.SMOOTH_STONE_SLAB);
            } else if (!inPlotX) {
              chunkData.setRegion(0, 0, 0, 1, 1, 16, Material.SMOOTH_STONE_SLAB);
              chunkData.setRegion(15, 0, 0, 16, 1, 16, Material.SMOOTH_STONE_SLAB);
            }
          }
        });
  }

  @Override
  protected void configureWorld(World world) {
    world.setSpawnLocation(-8, 0, -8);
    world.setSpawnFlags(false, false);
    world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
    world.setGameRule(GameRule.DISABLE_RAIDS, false);
    world.setGameRule(GameRule.DO_INSOMNIA, false);
    world.setGameRule(GameRule.DO_FIRE_TICK, false);
    world.setGameRule(GameRule.DO_MOB_LOOT, false);
    world.setGameRule(GameRule.DO_TRADER_SPAWNING, false);
    world.setGameRule(GameRule.DO_VINES_SPREAD, false);
    world.setGameRule(GameRule.DO_WARDEN_SPAWNING, false);
    world.setGameRule(GameRule.FALL_DAMAGE, false);
    world.setGameRule(GameRule.MOB_GRIEFING, false);
    world.setGameRule(GameRule.SHOW_DEATH_MESSAGES, false);
    world.setTime(6000);
  }

  private static final BiomeProvider ALL_PLAINS = new SingleBiomeProvider(Biome.PLAINS);

}
