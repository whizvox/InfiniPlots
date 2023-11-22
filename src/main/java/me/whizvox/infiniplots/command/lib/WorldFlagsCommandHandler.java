package me.whizvox.infiniplots.command.lib;

import me.whizvox.infiniplots.InfiniPlots;
import me.whizvox.infiniplots.command.ArgumentHelper;
import me.whizvox.infiniplots.command.CommandContext;
import me.whizvox.infiniplots.command.CommandHandler;
import me.whizvox.infiniplots.exception.InterruptCommandException;
import me.whizvox.infiniplots.exception.InvalidCommandArgumentException;
import me.whizvox.infiniplots.exception.MissingArgumentException;
import me.whizvox.infiniplots.exception.NotEnoughPermissionException;
import me.whizvox.infiniplots.flag.StandardProtectionFlags;
import me.whizvox.infiniplots.plot.PlotWorld;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class WorldFlagsCommandHandler extends CommandHandler {

  private static final List<String> MANUAL = List.of(
      "Manage protection flags for a world.",
      "Examples:",
      "- &b/infiniplots flag list&r : List all possible protection flags",
      "- &b/infiniplots flag add interact&r : Add the &einteract&r flag to your world or the default world",
      "- &b/infiniplots flag add interact bigplots&r : Add the &einteract&r flag to the &ebigplots&r world",
      "- &b/infiniplots flag remove ride&r : Remove the &eride&r flag from your world or the default world",
      "- &b/infiniplots flag remove ride bigplots&r : Remove the &eride&r flag from the &ebigplots&r world",
      "See also:",
      "- &b/infiniplots manual plotflag",
      "- &b/infiniplots manual worldinfo"
  );

  private static final List<String> ACTIONS = List.of("list", "add", "remove");

  @Override
  public String getUsageArguments() {
    return "list | add <flag> [<world>] | remove <flag> [<world>]";
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
        case "add", "remove" -> {
          return StandardProtectionFlags.STANDARD_FLAGS.keySet().stream()
              .filter(flag -> flag.startsWith(context.args().get(1)))
              .sorted()
              .toList();
        }
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
        message.add(StandardProtectionFlags.STANDARD_FLAGS.keySet().stream()
            .sorted()
            .map("&b%s&r"::formatted)
            .collect(Collectors.joining(", "))
        );
        context.sendMessage(message);
      }
      case "add" -> {
        NotEnoughPermissionException.check(context.sender(), "infiniplots.flag.world.modify");
        String flag = ArgumentHelper.getInSet(context, 1, StandardProtectionFlags.STANDARD_FLAGS.keySet());
        PlotWorld plotWorld = ArgumentHelper.getPlotWorld(context, 2);
        if (plotWorld.worldFlags.add(flag)) {
          InfiniPlots.getInstance().getPlotManager().getWorldFlagsRepository().insert(plotWorld.world.getUID(), flag);
          context.sendMessage("Successfully added the &b%s&r flag to &e%s", flag, plotWorld.name);
        } else {
          context.sendMessage("&cThe &b%s&c flag already exists in &e%s&c", flag, plotWorld.name);
        }
      }
      case "remove" -> {
        NotEnoughPermissionException.check(context.sender(), "infiniplots.flag.world.modify");
        String flag = ArgumentHelper.getInSet(context, 1, StandardProtectionFlags.STANDARD_FLAGS.keySet());
        PlotWorld plotWorld = ArgumentHelper.getPlotWorld(context, 2);
        if (plotWorld.worldFlags.remove(flag)) {
          InfiniPlots.getInstance().getPlotManager().getWorldFlagsRepository().remove(plotWorld.world.getUID(), flag);
          context.sendMessage("Successfully removed the &b%s&r flag from &e%s", flag, plotWorld.name);
        } else {
          context.sendMessage("&cWorld &e%s&c does not contain the &b%s&c flag", plotWorld.name, flag);
        }
      }
      default -> throw new InvalidCommandArgumentException("Unknown action: " + action);
    }
  }

}
