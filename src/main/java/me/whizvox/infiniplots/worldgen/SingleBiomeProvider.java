package me.whizvox.infiniplots.worldgen;

import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;

import java.util.List;

public class SingleBiomeProvider extends BiomeProvider {

  private final Biome biome;
  private final List<Biome> biomes;

  public SingleBiomeProvider(Biome biome) {
    this.biome = biome;
    biomes = List.of(biome);
  }

  @Override
  public Biome getBiome(WorldInfo worldInfo, int x, int y, int z) {
    return biome;
  }

  @Override
  public List<Biome> getBiomes(WorldInfo worldInfo) {
    return biomes;
  }

}
