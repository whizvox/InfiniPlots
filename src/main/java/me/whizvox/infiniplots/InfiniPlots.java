package me.whizvox.infiniplots;

import me.whizvox.infiniplots.command.InfPlotsAdminCommandExecutor;
import me.whizvox.infiniplots.command.InfPlotsCommandExecutor;
import me.whizvox.infiniplots.plot.PlotWorld;
import me.whizvox.infiniplots.plot.Plots;
import me.whizvox.infiniplots.util.InfPlotUtils;
import me.whizvox.infiniplots.util.PlotOwnerTiers;
import me.whizvox.infiniplots.worldgen.PlotWorldGeneratorRegistry;
import me.whizvox.infiniplots.worldgen.PlotWorldPlainGenerator;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

public final class InfiniPlots extends JavaPlugin {

  private static InfiniPlots instance = null;

  public static InfiniPlots getInstance() {
    return instance;
  }

  private Connection conn = null;
  private Plots plots = null;
  private Map<String, Integer> ownerTiers = null;
  private PlotWorldGeneratorRegistry plotGenRegistry;

  public Plots getPlots() {
    return plots;
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
    //noinspection ResultOfMethodCallIgnored extremely unlikely for this to fail
    getDataFolder().mkdirs();

    getConfig().addDefault("defaultPlotWorld", "world");
    getConfig().addDefault("defaultMaxPlots", 1);
    getConfig().addDefault("plotOwnerTiers", PlotOwnerTiers.DEFAULT);
    getConfig().addDefault("defaultPlotWorldGenerator", "plains2");
    getConfig().options().copyDefaults(true);
    getConfig().setComments("defaultPlotWorld", List.of("The default plot world assigned when a player wants to claim a new plot"));
    getConfig().setComments("defaultMaxPlots", List.of("Maximum number of plots one can own by default"));
    getConfig().setComments("plotOwnerTiers", List.of("List of tiers that are available to players regarding how many plots they can own"));
    getConfig().setComments("defaultPlotWorldGenerator", List.of("Default plot world generator to be used"));
    saveConfig();

    ownerTiers = new HashMap<>();
    //noinspection DataFlowIssue default value of plotOwnerTiers is set
    ownerTiers.putAll(getConfig().getSerializable("plotOwnerTiers", PlotOwnerTiers.class).tiers);

    plotGenRegistry = new PlotWorldGeneratorRegistry();
    for (int i = 1; i <= 4; i++) {
      plotGenRegistry.register(getName(), "plains" + i, new PlotWorldPlainGenerator(i));
    }

    try {
      conn = DriverManager.getConnection("jdbc:sqlite:" + new File(getDataFolder(), "infiniplots.db").getAbsolutePath());
      plots = new Plots(conn);
      plots.sync();
      getLogger().info("Loaded plots database");
    } catch (SQLException e) {
      throw new RuntimeException("Could not use SQLite database", e);
    }

    plots.plotWorlds().forEach(entry -> {
      UUID worldId = entry.getKey();
      PlotWorld plotWorld = entry.getValue();
      File worldFolder = InfPlotUtils.getWorldFolder(plotWorld.name);
      if (worldFolder.exists() && InfPlotUtils.isWorldFolder(worldFolder)) {
        World world = plotWorld.generator.createWorld(plotWorld.name);
        if (worldId.equals(world.getUID())) {
          plots.importWorld(world, plotWorld.generator, false);
        } else {
          getLogger().log(Level.WARNING, "Could not load plot world %s with unexpected unique ID. Expected: %s, Actual: %s", new Object[] {plotWorld.name, worldId, world.getUID()});
        }
      } else {
        getLogger().log(Level.WARNING, "Could not load plot world %s as its world folder does not exist", new Object[] {plotWorld.name});
      }
    });

    //noinspection DataFlowIssue command is listed in plugin.yml
    getCommand("infiniplotsadmin").setExecutor(new InfPlotsAdminCommandExecutor());
    //noinspection DataFlowIssue command is listed in plugin.yml
    getCommand("infiniplots").setExecutor(new InfPlotsCommandExecutor());
  }

  @Override
  public void onDisable() {
    if (conn != null) {
      try {
        conn.close();
      } catch (SQLException e) {
        getLogger().log(Level.WARNING, "Could not close connection to SQLite database", e);
      }
    }
  }

}
