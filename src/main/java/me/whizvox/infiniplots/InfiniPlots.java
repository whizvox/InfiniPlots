package me.whizvox.infiniplots;

import me.whizvox.infiniplots.command.CommandDelegator;
import me.whizvox.infiniplots.command.lib.InfiniPlotsCommandDelegator;
import me.whizvox.infiniplots.compat.worldguard.WorldGuardWrapper;
import me.whizvox.infiniplots.event.CheckEntityPlotBoundsTask;
import me.whizvox.infiniplots.event.CheckExperienceOrbsTask;
import me.whizvox.infiniplots.event.GriefPreventionEventsListener;
import me.whizvox.infiniplots.flag.FlagValue;
import me.whizvox.infiniplots.plot.PlotManager;
import me.whizvox.infiniplots.util.PlotOwnerTiers;
import me.whizvox.infiniplots.util.ProtectionFlags;
import me.whizvox.infiniplots.worldgen.PlotWorldGeneratorRegistry;
import me.whizvox.infiniplots.worldgen.PlotWorldPlainGenerator;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

public final class InfiniPlots extends JavaPlugin {

  public static final String
      CFG_DEFAULT_PLOT_WORLD = "defaultPlotWorld",
      CFG_DEFAULT_MAX_PLOTS = "defaultMaxPlots",
      CFG_PLOT_OWNER_TIERS = "plotOwnerTiers",
      CFG_DEFAULT_PLOT_WORLD_GENERATOR = "defaultPlotWorldGenerator",
      CFG_CHECK_PLOT_ENTITY_BOUNDS = "checkPlotEntityBounds",
      CFG_CHECK_EXP_ORBS = "checkExpOrbs",
      CFG_TELEPORT_AFTER_CLAIM = "teleportAfterClaim",
      CFG_DEFAULT_WORLD_FLAGS = "defaultWorldFlags",
      CFG_KICK_DESTINATION_WORLD = "kickDestinationWorld";

  private static InfiniPlots instance = null;

  public static InfiniPlots getInstance() {
    return instance;
  }

  private Connection conn = null;
  private PlotManager plotManager = null;
  private Map<String, Integer> ownerTiers = null;
  private Map<String, FlagValue> defaultFlags = null;
  private PlotWorldGeneratorRegistry plotGenRegistry = null;
  private ConfirmationManager confirmationManager = null;
  private int checkEntityPlotsTask = -1;
  private int checkExpOrbsTask = -1;

  public PlotManager getPlotManager() {
    return plotManager;
  }

  public Map<String, Integer> getOwnerTiers() {
    return Objects.requireNonNullElse(ownerTiers, Map.of());
  }

  public Map<String, FlagValue> getDefaultFlags() {
    return Objects.requireNonNullElse(defaultFlags, Map.of());
  }

  public PlotWorldGeneratorRegistry getPlotGenRegistry() {
    return plotGenRegistry;
  }

  public ConfirmationManager getConfirmationManager() {
    return confirmationManager;
  }

  @Override
  public void onEnable() {
    instance = this;
    ConfigurationSerialization.registerClass(PlotOwnerTiers.class);
    ConfigurationSerialization.registerClass(ProtectionFlags.class);
    getDataFolder().mkdirs();

    getConfig().addDefault(CFG_DEFAULT_PLOT_WORLD, "world");
    getConfig().addDefault(CFG_DEFAULT_MAX_PLOTS, 1);
    getConfig().addDefault(CFG_PLOT_OWNER_TIERS, PlotOwnerTiers.DEFAULT);
    getConfig().addDefault(CFG_DEFAULT_PLOT_WORLD_GENERATOR, "plains2");
    getConfig().addDefault(CFG_CHECK_PLOT_ENTITY_BOUNDS, 4);
    getConfig().addDefault(CFG_CHECK_EXP_ORBS, 0);
    getConfig().addDefault(CFG_TELEPORT_AFTER_CLAIM, false);
    getConfig().addDefault(CFG_DEFAULT_WORLD_FLAGS, ProtectionFlags.DEFAULT);
    getConfig().addDefault(CFG_KICK_DESTINATION_WORLD, "world");

    getConfig().options().copyDefaults(true);

    getConfig().setComments(CFG_DEFAULT_PLOT_WORLD, List.of("The default plot world assigned when a player wants to claim a new plot"));
    getConfig().setComments(CFG_DEFAULT_MAX_PLOTS, List.of("Maximum number of plots one can own by default"));
    getConfig().setComments(CFG_PLOT_OWNER_TIERS, List.of("List of tiers that are available to players regarding how many plots they can own"));
    getConfig().setComments(CFG_DEFAULT_PLOT_WORLD_GENERATOR, List.of("Default plot world generator to be used"));
    getConfig().setComments(CFG_CHECK_PLOT_ENTITY_BOUNDS, List.of(
        "Interval (in ticks) in which entities from plot worlds are removed if they go outside the bounds of a plot",
        "This is used to prevent griefing and lag from mobile entities that could move outside their origin plots",
        "For example: a value of 2 means this is checked every 2 ticks, or 10 times per second",
        "If a value of 0 is passed, then this check is disabled"
    ));
    getConfig().setComments(CFG_CHECK_EXP_ORBS, List.of(
        "Interval (in ticks) in which experience orbs are removed from plot worlds if they are disabled according to",
        "that world's flags. If the interval is set to 0, the check is disabled. This specifically is to address a",
        "bug that exists on some servers that doesn't trigger the proper event when an experience orb spawns. If",
        "experience orbs are spawning despite the world flags disabling them, turn this on."
    ));
    getConfig().setComments(CFG_TELEPORT_AFTER_CLAIM, List.of("After claiming a plot, teleport the owner to it"));
    getConfig().setComments(CFG_DEFAULT_WORLD_FLAGS, List.of("Default protection flags used for all worlds"));
    getConfig().setComments(CFG_KICK_DESTINATION_WORLD, List.of("The world to send a player if they kicked from a plot world."));
    saveConfig();

    ownerTiers = getConfig().getSerializable(CFG_PLOT_OWNER_TIERS, PlotOwnerTiers.class).tiers;
    defaultFlags = getConfig().getSerializable(CFG_DEFAULT_WORLD_FLAGS, ProtectionFlags.class).flags;

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

    PluginCommand command = getCommand("infiniplots");
    CommandDelegator delegator = new InfiniPlotsCommandDelegator();
    command.setExecutor(delegator);
    command.setTabCompleter(delegator);

    if (!WorldGuardWrapper.init()) {
      getServer().getPluginManager().registerEvents(new GriefPreventionEventsListener(), this);
    }
    int entityCheckInterval = getConfig().getInt(CFG_CHECK_PLOT_ENTITY_BOUNDS);
    if (entityCheckInterval < 1) {
      getLogger().info("Entity plot bounds checking has been disabled");
    } else {
      getLogger().info("Entity plot bounds checking has been enabled every " + entityCheckInterval + " ticks");
      checkEntityPlotsTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new CheckEntityPlotBoundsTask(), 0, entityCheckInterval);
    }
    int expOrbCheckInterval = getConfig().getInt(CFG_CHECK_EXP_ORBS);
    if (expOrbCheckInterval < 1) {
      getLogger().fine("Experience orb checking has been disabled");
    } else {
      getLogger().info("Experience orb checking has been enabled every " + expOrbCheckInterval + " ticks");
      checkExpOrbsTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new CheckExperienceOrbsTask(), 0, expOrbCheckInterval);
    }

    confirmationManager = new ConfirmationManager();
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
    if (checkExpOrbsTask != -1) {
      Bukkit.getScheduler().cancelTask(checkExpOrbsTask);
      checkExpOrbsTask = -1;
    }
  }

}
