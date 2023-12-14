package me.whizvox.infiniplots.event;

import me.whizvox.infiniplots.InfiniPlots;
import me.whizvox.infiniplots.flag.DefaultFlags;
import me.whizvox.infiniplots.flag.FlagHelper;
import org.bukkit.entity.ExperienceOrb;

// way to get around Spigot bug https://hub.spigotmc.org/jira/browse/SPIGOT-7523
public class CheckExperienceOrbsTask implements Runnable {

  @Override
  public void run() {
    InfiniPlots.getInstance().getPlotManager().plotWorlds().forEach(plotWorld -> {
      plotWorld.world.getEntitiesByClass(ExperienceOrb.class).forEach(orb -> {
        if (!FlagHelper.allowNaturalAction(plotWorld, orb.getLocation(), DefaultFlags.EXP_DROPS.name())) {
          orb.remove();
        }
      });
    });
  }

}
