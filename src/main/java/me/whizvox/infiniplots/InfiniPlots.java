package me.whizvox.infiniplots;

import me.whizvox.infiniplots.command.InfPlotsAdminCommandExecutor;
import me.whizvox.infiniplots.command.InfPlotsCommandExecutor;
import me.whizvox.infiniplots.event.CheckEntityPlotBoundsTask;
import me.whizvox.infiniplots.event.GriefPreventionEventsListener;
import me.whizvox.infiniplots.plot.PlotManager;
import me.whizvox.infiniplots.util.PlotOwnerTiers;
import me.whizvox.infiniplots.worldgen.PlotWorldGeneratorRegistry;
import me.whizvox.infiniplots.worldgen.PlotWorldPlainGenerator;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public final class InfiniPlots extends JavaPlugin {

  private static InfiniPlots instance = null;

  public static InfiniPlots getInstance() {
    return instance;
  }

  private Connection conn = null;
  private PlotManager plotManager = null;
  private Map<String, Integer> ownerTiers = null;
  private PlotWorldGeneratorRegistry plotGenRegistry = null;
  private int checkEntityPlotsTask = -1;

  public PlotManager getPlotManager() {
    return plotManager;
  }

  public Map<String, Integer> getOwnerTiers() {
    if (ownerTiers == null) {
      return Map.of();
    }
    return Collections.unmodifiableMap(ownerTiers);
  }

  public PlotWorldGeneratorRegistry getPlotGenRegistry() {
    return plotGenRegistry;
  }

  @Override
  public void onEnable() {
    instance = this;
    ConfigurationSerialization.registerClass(PlotOwnerTiers.class);
    getDataFolder().mkdirs();

    getConfig().addDefault("defaultPlotWorld", "world");
    getConfig().addDefault("defaultMaxPlots", 1);
    getConfig().addDefault("plotOwnerTiers", PlotOwnerTiers.DEFAULT);
    getConfig().addDefault("defaultPlotWorldGenerator", "plains2");
    getConfig().addDefault("checkEntityPlotBounds", 4);
    //getConfig().addDefault("useWorldGuardIfLoaded", true);

    getConfig().options().copyDefaults(true);

    getConfig().setComments("defaultPlotWorld", List.of("The default plot world assigned when a player wants to claim a new plot"));
    getConfig().setComments("defaultMaxPlots", List.of("Maximum number of plots one can own by default"));
    getConfig().setComments("plotOwnerTiers", List.of("List of tiers that are available to players regarding how many plots they can own"));
    getConfig().setComments("defaultPlotWorldGenerator", List.of("Default plot world generator to be used"));
    getConfig().setComments("checkEntityPlotBounds", List.of(
        "Interval (in ticks) in which entities from plot worlds are removed if they go outside the bounds of a plot",
        "This is used to prevent griefing and lag from mobile entities that could move outside of their origin plots",
        "For example: a value of 2 means this is checked every 2 ticks, or 10 times per second",
        "If a value of 0 is passed, then this check is disabled entirely"
    ));
    /*getConfig().setComments("useWorldGuardIfLoaded", List.of(
        "Use WorldGuard regions to handle plot world interactions",
        "If false or if WorldGuard is not loaded, InfiniPlots will use its own event handlers"
    ));*/
    saveConfig();

    ownerTiers = new HashMap<>();
    ownerTiers.putAll(getConfig().getSerializable("plotOwnerTiers", PlotOwnerTiers.class).tiers);

    plotGenRegistry = new PlotWorldGeneratorRegistry();
    for (int i = 1; i <= 4; i++) {
      plotGenRegistry.register(getName(), "plains" + i, new PlotWorldPlainGenerator(i));
    }

    try {
      conn = DriverManager.getConnection("jdbc:sqlite:" + new File(getDataFolder(), "infiniplots.db").getAbsolutePath());
      plotManager = new PlotManager(conn);
      plotManager.sync();
      getLogger().info("Loaded plots database");
    } catch (SQLException e) {
      throw new RuntimeException("Could not use SQLite database", e);
    }

    getCommand("infiniplotsadmin").setExecutor(new InfPlotsAdminCommandExecutor());
    getCommand("infiniplots").setExecutor(new InfPlotsCommandExecutor());

    getServer().getPluginManager().registerEvents(new GriefPreventionEventsListener(), this);
    int entityCheckInterval = getConfig().getInt("checkEntityPlotBounds");
    if (entityCheckInterval < 1) {
      getLogger().info("Entity plot bounds checking has been disabled");
    } else {
      getLogger().info("Entity plot bounds checking has been enabled every " + entityCheckInterval + " ticks");
      checkEntityPlotsTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new CheckEntityPlotBoundsTask(), 0, entityCheckInterval);
    }
  }

  @Override
  public void onDisable() {
    if (conn != null) {
      try {
        conn.close();
        conn = null;
      } catch (SQLException e) {
        getLogger().log(Level.WARNING, "Could not close connection to SQLite database", e);
      }
    }
    if (checkEntityPlotsTask != -1) {
      Bukkit.getScheduler().cancelTask(checkEntityPlotsTask);
      checkEntityPlotsTask = -1;
    }
  }

}
