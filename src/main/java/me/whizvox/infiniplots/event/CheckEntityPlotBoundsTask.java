package me.whizvox.infiniplots.event;

import me.whizvox.infiniplots.InfiniPlots;
import me.whizvox.infiniplots.util.ChunkPos;
import org.bukkit.entity.Player;

/**
 * A runnable for checking the locations of all entities in a plot world, and removing any that are outside the bounds
 * of a plot. This is to ensure that griefing or lag creation via mobile entities cannot happen outside the originating
 * plot.
 */
public class CheckEntityPlotBoundsTask implements Runnable {

  @Override
  public void run() {
    InfiniPlots.getInstance().getPlotManager().plotWorlds().forEach(plotWorld -> {
      plotWorld.world.getEntities().forEach(entity -> {
        if (!(entity instanceof Player)) {
          if (!entity.getVelocity().isZero()) {
            ChunkPos pos = new ChunkPos(entity.getLocation());
            if (!plotWorld.generator.inPlot(pos.x(), pos.z())) {
              entity.remove();
            }
          }
        }
      });
    });
  }

}
