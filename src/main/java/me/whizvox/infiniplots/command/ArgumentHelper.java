package me.whizvox.infiniplots.command;

import me.whizvox.infiniplots.InfiniPlots;
import me.whizvox.infiniplots.exception.InterruptCommandException;
import me.whizvox.infiniplots.exception.InvalidCommandArgumentException;
import me.whizvox.infiniplots.exception.MissingArgumentException;
import me.whizvox.infiniplots.plot.PlotWorld;
import me.whizvox.infiniplots.util.ChunkPos;
import me.whizvox.infiniplots.util.InfPlotUtils;
import me.whizvox.infiniplots.util.Pair;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;

public class ArgumentHelper {

  public static <T> T getArgument(CommandContext context, int index, Supplier<T> defaultValue, Function<String, T> parser) {
    if (index >= 0 && index < context.args().size()) {
      return parser.apply(context.args().get(index));
    }
    return defaultValue.get();
  }

  public static <T> T getArgument(CommandContext context, int index, Function<String, T> parser) {
    return getArgument(context, index, MissingArgumentException::fail, parser);
  }

  public static int getInt(CommandContext context, int index, Supplier<Integer> defaultValue, int min, int max) {
    try {
      int value = getArgument(context, index, defaultValue, Integer::parseInt);
      if (value < min || value > max) {
        throw new InvalidCommandArgumentException("Integer out of range: [" + min + "," + max + "]");
      }
      return value;
    } catch (NumberFormatException e) {
      throw new InvalidCommandArgumentException("Invalid integer");
    }
  }

  public static int getInt(CommandContext context, int index, int min, int max) {
    return getInt(context, index, MissingArgumentException::fail, min, max);
  }

  public static World getWorld(CommandContext context, int index, Supplier<World> defaultValue) {
    return getArgument(context, index, defaultValue, worldName -> {
      World world = Bukkit.getWorld(worldName);
      if (world == null) {
        throw new InterruptCommandException("World " + worldName + "does not exist");
      }
      return world;
    });
  }

  public static World getWorld(CommandContext context, int index) {
    return getWorld(context, index, MissingArgumentException::fail);
  }

  public static PlotWorld getPlotWorld(CommandContext context, int index, Supplier<PlotWorld> defaultValue) {
    return getArgument(context, index, defaultValue, worldName -> {
      World world = Bukkit.getWorld(worldName);
      if (world == null) {
        throw new InterruptCommandException("World " + worldName + " does not exist");
      }
      PlotWorld plotWorld = InfiniPlots.getInstance().getPlotManager().getPlotWorld(world.getUID());
      if (plotWorld == null) {
        throw new InterruptCommandException("World " + worldName + " is not a plot world");
      }
      return plotWorld;
    });
  }

  public static PlotWorld getPlotWorld(CommandContext context, int index) {
    return getPlotWorld(context, index, () -> {
      PlotWorld plotWorld = null;
      if (context.sender() instanceof Player player) {
        plotWorld = InfiniPlots.getInstance().getPlotManager().getPlotWorld(player.getWorld().getUID());
      }
      if (plotWorld == null) {
        plotWorld = InfiniPlots.getInstance().getPlotManager().getDefaultWorld();
      }
      if (plotWorld == null) {
        throw new InterruptCommandException("Default plot world " + InfiniPlots.getInstance().getPlotManager().getDefaultWorldName() + " does not exist");
      }
      return plotWorld;
    });
  }

  public static Player getOnlinePlayer(CommandContext context, int index, Supplier<Player> defaultValue) {
    return getArgument(context, index, defaultValue, Bukkit::getPlayer);
  }

  public static Player getOnlinePlayer(CommandContext context, int index) {
    return getOnlinePlayer(context, index, MissingArgumentException::fail);
  }

  public static OfflinePlayer getOfflinePlayer(CommandContext context, int index, Supplier<OfflinePlayer> defaultValue) {
    return getArgument(context, index, defaultValue, playerName -> {
      for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
        if (playerName.equalsIgnoreCase(offlinePlayer.getName())) {
          return offlinePlayer;
        }
      }
      throw new InterruptCommandException("Player " + playerName + " has not joined this server");
    });
  }

  public static OfflinePlayer getOfflinePlayer(CommandContext context, int index) {
    return getOfflinePlayer(context, index, MissingArgumentException::fail);
  }

  public static int getPlotNumber(CommandContext context, int index, Supplier<Integer> defaultValue) {
    return getInt(context, index, defaultValue, 1, Integer.MAX_VALUE);
  }

  public static int getPlotNumber(CommandContext context, int index) {
    return getPlotNumber(context, index, () -> {
      Player player = context.getPlayerOrException();
      PlotWorld plotWorld = InfPlotUtils.getPlotWorldOrDefault(player.getWorld().getUID());
      if (plotWorld == null) {
        throw new InterruptCommandException("Default plot world " + InfiniPlots.getInstance().getPlotManager().getDefaultWorldName() + " does not exist");
      }
      int plotNumber = plotWorld.generator.getPlotNumber(new ChunkPos(player.getLocation()));
      if (plotNumber < 1) {
        throw new InterruptCommandException("No plot found");
      }
      return plotNumber;
    });
  }

  public static Pair<PlotWorld, Integer> getWorldAndPlotNumber(CommandContext context, int index) {
    PlotWorld plotWorld = getPlotWorld(context, index + 1);
    int plotNumber = getPlotNumber(context, index, () -> {
      int res = plotWorld.generator.getPlotNumber(new ChunkPos(context.getPlayerOrException().getLocation()));
      if (res < 1) {
        throw new InterruptCommandException("Not in a plot");
      }
      return res;
    });
    return Pair.of(plotWorld, plotNumber);
  }

  public static String getInSet(CommandContext context, int index, Supplier<String> defaultValue, Collection<String> possibleValues) {
    return getArgument(context, index, defaultValue, s -> {
      if (possibleValues.contains(s)) {
        return s;
      }
      throw new InvalidCommandArgumentException("Must be one of these values: [" + String.join(", ", possibleValues) + "]");
    });
  }

  public static String getInSet(CommandContext context, int index, Collection<String> possibleValues) {
    return getInSet(context, index, MissingArgumentException::fail, possibleValues);
  }

}