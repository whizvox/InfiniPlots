package me.whizvox.infiniplots.util;

public record ChunkRegion(ChunkPos p1, ChunkPos p2) {

  public ChunkRegion(ChunkPos p1, ChunkPos p2) {
    int x1, x2, z1, z2;
    if (p1.x() < p2.x()) {
      x1 = p1.x();
      x2 = p2.x();
    } else {
      x1 = p2.x();
      x2 = p1.x();
    }
    if (p1.z() < p2.z()) {
      z1 = p1.z();
      z2 = p2.z();
    } else {
      z1 = p2.z();
      z2 = p1.z();
    }
    this.p1 = new ChunkPos(x1, z1);
    this.p2 = new ChunkPos(x2, z2);
  }

  public int firstCornerX() {
    return p1.x() * 16;
  }

  public int firstCornerZ() {
    return p1.z() * 16;
  }

  public int secondCornerX() {
    return p2.x() * 16 + 15;
  }

  public int secondCornerZ() {
    return p2.z() * 16 + 15;
  }

}
