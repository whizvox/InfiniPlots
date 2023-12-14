package me.whizvox.infiniplots.command.lib;

import me.whizvox.infiniplots.InfiniPlots;
import me.whizvox.infiniplots.command.ArgumentHelper;
import me.whizvox.infiniplots.command.CommandContext;
import me.whizvox.infiniplots.command.CommandHandler;
import me.whizvox.infiniplots.command.SuggestionHelper;
import me.whizvox.infiniplots.exception.InterruptCommandException;
import me.whizvox.infiniplots.exception.InvalidCommandArgumentException;
import me.whizvox.infiniplots.exception.NotEnoughPermissionException;
import me.whizvox.infiniplots.flag.Flag;
import me.whizvox.infiniplots.flag.FlagValue;
import me.whizvox.infiniplots.plot.PlotWorld;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class WorldFlagCommandHandler extends CommandHandler {

  private static final List<String> MANUAL = List.of(
      "Manage protection flags for a world, allowing the setting flags for specific worlds and clearing flags from " +
      "worlds. If a flag is not set for a world, the default value will be used if defined via " +
      "&edefault_flags.json&r. If not defined there, the feature will be denied.",
      "If you want to see all of the flags currently set for a world, run &b/infiniplots worldinfo&r.",
      "Examples:",
      "- &b/infiniplots flag set interact allow&r : Set the &einteract&r flag to your world or the default world",
      "- &b/infiniplots flag set interact allow bigplots&r : Add the &einteract&r flag to the &ebigplots&r world",
      "- &b/infiniplots flag clear ride&r : Clear the &eride&r flag from your world or the default world",
      "- &b/infiniplots flag clear ride bigplots&r : Clear the &eride&r flag from the &ebigplots&r world",
      "See also:",
      "- &b/infiniplots manual worldinfo",
      "- &b/infiniplots flag list"
  );

  private static final List<String> ACTIONS = List.of("get", "set", "clear");

  @Override
  public String getUsageArguments() {
    return "get <flag> [<world>] | set <flag> <value> [<world>] | clear <flag> [<world>]";
  }

  @Override
  public List<String> getManual() {
    return MANUAL;
  }

  @Override
  public List<String> listSuggestions(CommandContext context) {
    if (context.args().size() == 1) {
      return SuggestionHelper.fromCollection(ACTIONS, context.arg(0));
    } else if (context.args().size() == 2) {
      switch (context.arg(0)) {
        case "get", "set", "clear" -> {
          return SuggestionHelper.flags(context.arg(1));
        }
      }
    } else if (context.args().size() == 3) {
      switch (context.arg(0)) {
        case "get", "clear" -> {
          return SuggestionHelper.plotWorlds(context.arg(2));
        }
        case "set" -> {
          return SuggestionHelper.flagValues(context.arg(2));
        }
      }
    } else if (context.args().size() == 4) {
      if (context.arg(0).equals("set")) {
        return SuggestionHelper.plotWorlds(context.arg(3));
      }
    }
    return super.listSuggestions(context);
  }

  @Override
  public boolean hasPermission(CommandSender sender) {
    return sender.hasPermission("infiniplots.flag");
  }

  @Override
  public void execute(CommandContext context) throws InterruptCommandException {
    String action = ArgumentHelper.getInSet(context, 0, ACTIONS);
    switch (action) {
      case "get" -> {
        NotEnoughPermissionException.check(context.sender(), "infiniplots.worldflag.get");
        PlotWorld plotWorld;
        if (context.sender() instanceof Player player) {
          plotWorld = InfiniPlots.getInstance().getPlotManager().getPlotWorld(player);
        } else {
          plotWorld = InfiniPlots.getInstance().getPlotManager().getDefaultWorld();
        }
        if (plotWorld == null) {
          throw new InterruptCommandException("Default world is not set up");
        }
        String flag = ArgumentHelper.getString(context, 1);
        boolean isSet = plotWorld.worldFlags.contains(flag);
        context.sendMessage("World Flag &b%s&r: &e%s%s", flag, plotWorld.worldFlags.getValue(flag).friendlyName, isSet ? "" : " &a(default)");
      }
      case "set" -> {
        String flag = ArgumentHelper.getString(context, 1);
        NotEnoughPermissionException.check(context.sender(), "infiniplots.worldflag.modify." + flag);
        FlagValue value = ArgumentHelper.getFlagValue(context, 2);
        PlotWorld plotWorld = ArgumentHelper.getPlotWorld(context, 3);
        plotWorld.worldFlags.set(new Flag(flag, value));
        InfiniPlots.getInstance().getPlotManager().getWorldFlagsRepository().insertOrUpdate(plotWorld.world.getUID(), flag, value);
        context.sendMessage("Successfully set the &b%s&r flag for &e%s", flag, plotWorld.name);
      }
      case "clear" -> {
        String flag = ArgumentHelper.getString(context, 1);
        NotEnoughPermissionException.check(context.sender(), "infiniplots.worldflag.modify." + flag);
        PlotWorld plotWorld = ArgumentHelper.getPlotWorld(context, 2);
        plotWorld.worldFlags.clear(flag);
        InfiniPlots.getInstance().getPlotManager().getWorldFlagsRepository().remove(plotWorld.world.getUID(), flag);
        context.sendMessage("Successfully cleared the &b%s&r flag from &e%s", flag, plotWorld.name);
      }
      default -> throw new InvalidCommandArgumentException("Unknown action: " + action);
    }
  }

}
