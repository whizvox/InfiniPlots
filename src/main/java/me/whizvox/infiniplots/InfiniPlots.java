package me.whizvox.infiniplots;

import me.whizvox.infiniplots.command.InfPlotsAdminCommandExecutor;
import me.whizvox.infiniplots.command.InfPlotsCommandExecutor;
import me.whizvox.infiniplots.plot.Plots;
import me.whizvox.infiniplots.util.PlotOwnerTiers;
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
  private Plots plots = null;
  private Map<String, Integer> ownerTiers = null;

  public Plots getPlots() {
    return plots;
  }

  public Map<String, Integer> getOwnerTiers() {
    if (ownerTiers == null) {
      return Map.of();
    }
    return Collections.unmodifiableMap(ownerTiers);
  }

  @Override
  public void onEnable() {
    instance = this;
    ConfigurationSerialization.registerClass(PlotOwnerTiers.class);
    getDataFolder().mkdirs();

    getConfig().addDefault("defaultPlotWorld", "world");
    getConfig().addDefault("defaultMaxPlots", 1);
    getConfig().addDefault("plotOwnerTiers", PlotOwnerTiers.DEFAULT);
    getConfig().options().copyDefaults(true);
    getConfig().setComments("defaultPlotWorld", List.of("The default plot world assigned when a player wants to claim a new plot"));
    getConfig().setComments("defaultMaxPlots", List.of("Maximum number of plots one can own by default"));
    getConfig().setComments("plotOwnerTiers", List.of("List of tiers that are available to players regarding how many plots they can own"));
    saveConfig();

    ownerTiers = new HashMap<>();
    ownerTiers.putAll(getConfig().getSerializable("plotOwnerTiers", PlotOwnerTiers.class).tiers);

    try {
      conn = DriverManager.getConnection("jdbc:sqlite:" + new File(getDataFolder(), "infiniplots.db").getAbsolutePath());
      plots = new Plots(conn);
      plots.sync();
      getLogger().info("Loaded plots database");
    } catch (SQLException e) {
      throw new RuntimeException("Could not use SQLite database", e);
    }
    getCommand("infiniplotsadmin").setExecutor(new InfPlotsAdminCommandExecutor());
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
