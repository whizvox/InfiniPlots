package me.whizvox.infiniplots.worldgen;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public class PlotWorldGeneratorRegistry {

  private final Map<String, PlotWorldGenerator> generators;
  private final Map<PlotWorldGenerator, String> keyLookup;

  public PlotWorldGeneratorRegistry() {
    generators = new HashMap<>();
    keyLookup = new HashMap<>();
  }

  private static String getResourceLocation(@Nullable String namespace, String path) {
    return namespace == null ? "infiniplots" : namespace.toLowerCase() + ":" + path;
  }

  public void forEach(BiConsumer<String, PlotWorldGenerator> consumer) {
    generators.forEach(consumer);
  }

  public Stream<Map.Entry<String, PlotWorldGenerator>> generators() {
    return generators.entrySet().stream();
  }

  @Nullable
  public PlotWorldGenerator getGenerator(String key) {
    if (key.indexOf(':') == -1) {
      key = "infiniplots:" + key;
    }
    return generators.get(key);
  }

  @Nullable
  public PlotWorldGenerator getGenerator(@Nullable String pluginName, String generatorName) {
    return getGenerator(getResourceLocation(pluginName, generatorName));
  }

  @Nullable
  public String getKey(PlotWorldGenerator generator) {
    return keyLookup.get(generator);
  }

  public void register(String pluginName, String generatorName, PlotWorldGenerator generator) {
    String key = getResourceLocation(pluginName, generatorName);
    generators.put(key, generator);
    keyLookup.put(generator, key);
  }

  public void clear() {
    generators.clear();
    keyLookup.clear();
  }

}
