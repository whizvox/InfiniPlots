package me.whizvox.infiniplots.plot;

import java.util.UUID;

public record PlotId(UUID world, int plot) {

  public static PlotId from(UUID worldId, int plotId) {
    return new PlotId(worldId, plotId);
  }

}
