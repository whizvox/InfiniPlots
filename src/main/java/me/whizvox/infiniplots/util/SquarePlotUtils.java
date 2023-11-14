package me.whizvox.infiniplots.util;

public class SquarePlotUtils {

  /**
   * Calculates the bottom-left corner chunk position of a plot given a plot number. Chunk positions are assigned
   * starting with plot #1 -> (0,0), and going in a clockwise direction from the top-right corner of a square.
   * For instance, plot #2 -> (1,1), plot #3 -> (1,0), plot #4 -> (1,-1), plot #9 -> (0,1), plot #10 -> (2,2).
   * @param plotNumber The plot number. Must be greater than or equal to 1.
   * @param regionWidth The width of the region. Should include the plot width and X padding.
   * @param regionDepth The depth of the region. Should include the plot depth and Z padding.
   * @return The chunk position associated with the plot number, multiplied by the region width and height.
   * @throws IllegalArgumentException If <code>plotNumber</code> is less than 1
   */
  public static ChunkPos getChunkPos(int plotNumber, int regionWidth, int regionDepth) {
    if (plotNumber < 1) {
      throw new IllegalArgumentException("Plot number must be greater than 0");
    }
    if (plotNumber == 1) {
      return new ChunkPos(0, 0);
    }
    int radius = (int) Math.ceil(Math.sqrt(plotNumber)) / 2;
    int size = radius * 2 + 1;
    int prevArea = (size - 2) * (size - 2);
    plotNumber -= (prevArea + 1);
    int sectionSize = ((size * size) - prevArea) / 4;
    int x;
    int z;
    // chunk 1 = right column
    if (plotNumber < sectionSize) {
      x = radius;
      z = radius - (plotNumber % sectionSize);
    // chunk 2 = bottom row
    } else if (plotNumber < sectionSize * 2) {
      x = radius - (plotNumber % sectionSize);
      z = -radius;
    // chunk 3 = left column
    } else if (plotNumber < sectionSize * 3) {
      x = -radius;
      z = -radius + (plotNumber % sectionSize);
    // chunk 4 = top row
    } else {
      x = -radius + (plotNumber % sectionSize);
      z = radius;
    }
    return new ChunkPos(x * regionWidth, z * regionDepth);
  }

  /**
   * Calculate the plot number from a chunk position of a plot.
   * @param pos The chunk position of the plot
   * @param regionWidth The width of the region. Should include both the plot width and X padding.
   * @param regionDepth The depth of the region. Should include both the plot depth and Z padding.
   * @return The plot number associated with the plot's chunk position
   * @see #getChunkPos(int, int, int)
   */
  public static int getPlotNumber(ChunkPos pos, int regionWidth, int regionDepth) {
    int x, z;
    if (pos.x() < 0) {
      x = pos.x() - regionWidth + 1;
    } else {
      x = pos.x();
    }
    if (pos.z() < 0) {
      z = pos.z() - regionDepth + 1;
    } else {
      z = pos.z();
    }
    x /= regionWidth;
    z /= regionDepth;
    if (x == 0 && z == 0) {
      return 1;
    }
    int radius = Math.max(Math.abs(x), Math.abs(z));
    int size = radius * 2 + 1;
    int prevArea = (size - 2) * (size - 2);
    int sectionSize = ((size * size) - prevArea) / 4;
    int local;
    if (x == radius) {
      local = (-z + radius);
    } else if (z == -radius) {
      local = sectionSize + (-x + radius);
    } else if (x == -radius) {
      local = sectionSize * 2 + (z + radius);
    } else {
      local = sectionSize * 3 + (x + radius);
    }
    return local + 1 + prevArea;
  }

}
