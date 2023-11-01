package me.whizvox.infiniplots.util;

import org.bukkit.Chunk;
import org.bukkit.Location;

public record ChunkPos(int x, int z) {

  public ChunkPos(Chunk chunk) {
    this(chunk.getX(), chunk.getZ());
  }

  public ChunkPos(Location location) {
    this(location.getChunk());
  }

}
