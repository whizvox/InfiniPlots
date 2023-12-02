package me.whizvox.infiniplots.command.lib;

import me.whizvox.infiniplots.InfiniPlots;
import me.whizvox.infiniplots.command.ArgumentHelper;
import me.whizvox.infiniplots.command.CommandContext;
import me.whizvox.infiniplots.command.CommandHandler;
import me.whizvox.infiniplots.exception.InterruptCommandException;
import me.whizvox.infiniplots.exception.InvalidCommandArgumentException;
import me.whizvox.infiniplots.exception.MissingArgumentException;
import me.whizvox.infiniplots.exception.NotEnoughPermissionException;
import me.whizvox.infiniplots.flag.DefaultFlags;
import me.whizvox.infiniplots.flag.Flag;
import me.whizvox.infiniplots.flag.FlagValue;
import me.whizvox.infiniplots.plot.PlotWorld;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class WorldFlagCommandHandler extends CommandHandler {

  private static final List<String> MANUAL = List.of(
      "Manage protection flags for a world, allowing for the listing of all possible flags, setting flags for " +
      "specific worlds, and clearing flags from worlds. If a flag is not set for a world, the default value will be " +
      "used if defined via &edefault_flags.json&r. If not defined there, the feature will be denied.",
      "If you want to see all of the flags currently set for a world, run &b/infiniplots worldinfo&r.",
      "Examples:",
      "- &b/infiniplots flag list&r : List all possible protection flags",
      "- &b/infiniplots flag set interact allow&r : Set the &einteract&r flag to your world or the default world",
      "- &b/infiniplots flag set interact allow bigplots&r : Add the &einteract&r flag to the &ebigplots&r world",
      "- &b/infiniplots flag clear ride&r : Clear the &eride&r flag from your world or the default world",
      "- &b/infiniplots flag clear ride bigplots&r : Clear the &eride&r flag from the &ebigplots&r world",
      "See also:",
      "- &b/infiniplots manual worldinfo"
  );

  private static final List<String> ACTIONS = List.of("list", "set", "clear");

  @Override
  public String getUsageArguments() {
    return "list | set <flag> <value> [<world>] | clear <flag> [<world>]";
  }

  @Override
  public List<String> getManual() {
    return MANUAL;
  }

  @Override
  public List<String> listSuggestions(CommandContext context) {
    if (context.args().size() == 1) {
      return ACTIONS.stream().filter(s -> s.startsWith(context.args().get(0))).toList();
    } else if (context.args().size() == 2) {
      switch (context.args().get(0)) {
        case "set", "clear" -> {
          return DefaultFlags.ALL_FLAGS.keySet().stream()
              .filter(flag -> flag.startsWith(context.args().get(1)))
              .sorted()
              .toList();
        }
      }
    } else if (context.args().size() == 3) {
      if (context.args().get(0).equals("set")) {
        return FlagValue.VALUES_MAP.keySet().stream()
            .filter(value -> value.startsWith(context.args().get(2)))
            .sorted()
            .toList();
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
    String action = ArgumentHelper.getInSet(context, 0, MissingArgumentException::fail, ACTIONS);
    switch (action) {
      case "list" -> {
        NotEnoughPermissionException.check(context.sender(), "infiniplots.flag.list");
        List<String> message = new ArrayList<>();
        message.add("&7=== &6All Protection Flags &7===");
        message.add(DefaultFlags.ALL_FLAGS.keySet().stream()
            .sorted()
            .map("&b%s&r"::formatted)
            .collect(Collectors.joining(", "))
        );
        context.sendMessage(message);
      }
      case "set" -> {
        NotEnoughPermissionException.check(context.sender(), "infiniplots.flag.world.modify");
        String flag = ArgumentHelper.getInSet(context, 1, DefaultFlags.ALL_FLAGS.keySet());
        FlagValue value = ArgumentHelper.getFlagValue(context, 2);
        PlotWorld plotWorld = ArgumentHelper.getPlotWorld(context, 3);
        if (!plotWorld.worldFlags.contains(flag)) {
          plotWorld.worldFlags.set(new Flag(flag, value));
          InfiniPlots.getInstance().getPlotManager().getWorldFlagsRepository().insert(plotWorld.world.getUID(), flag, value);
          context.sendMessage("Successfully set the &b%s&r flag to &e%s", flag, plotWorld.name);
        } else {
          context.sendMessage("&cThe &b%s&c flag already exists in &e%s&c", flag, plotWorld.name);
        }
      }
      case "clear" -> {
        NotEnoughPermissionException.check(context.sender(), "infiniplots.flag.world.modify");
        String flag = ArgumentHelper.getInSet(context, 1, DefaultFlags.ALL_FLAGS.keySet());
        PlotWorld plotWorld = ArgumentHelper.getPlotWorld(context, 2);
        if (plotWorld.worldFlags.contains(flag)) {
          plotWorld.worldFlags.clear(flag);
          InfiniPlots.getInstance().getPlotManager().getWorldFlagsRepository().remove(plotWorld.world.getUID(), flag);
          context.sendMessage("Successfully cleared the &b%s&r flag from &e%s", flag, plotWorld.name);
        } else {
          context.sendMessage("&cWorld &e%s&c does not contain the &b%s&c flag", plotWorld.name, flag);
        }
      }
      default -> throw new InvalidCommandArgumentException("Unknown action: " + action);
    }
  }

}
