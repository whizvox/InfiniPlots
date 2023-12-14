package me.whizvox.infiniplots.command.lib;

import me.whizvox.infiniplots.InfiniPlots;
import me.whizvox.infiniplots.command.ArgumentHelper;
import me.whizvox.infiniplots.command.CommandContext;
import me.whizvox.infiniplots.command.CommandHandler;
import me.whizvox.infiniplots.command.SuggestionHelper;
import me.whizvox.infiniplots.exception.InterruptCommandException;
import me.whizvox.infiniplots.exception.NotEnoughPermissionException;
import me.whizvox.infiniplots.flag.Flag;
import me.whizvox.infiniplots.flag.FlagValue;
import me.whizvox.infiniplots.plot.PlotWorld;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PlotFlagCommandHandler extends CommandHandler {

  private static final List<String> MANUAL = List.of(
      "Manage protection flags for a specific plot. Plot flags will have a higher priority than world flags. For " +
      "example: if a world flag value is set to &eDENY&r, but the plot flag value is set to &eALLOW&r, then the " +
      "plot's value will be used.",
      "Examples:",
      "- &b/infiniplots plotflag list&r : List all set flags for this plot",
      "- &b/infiniplots plotflag list 5&r : List all set flags for the 5th plot in this world",
      "- &b/infiniplots plotflag list 5 bigplots&r : List all set flags for the 5th plot in a specific world",
      "- &b/infiniplots plotflag set interact allow&r : Set a flag's value in the plot you're standing in",
      "- &b/infiniplots plotflag set interact allow 5&r : Set a flag's value in the 5th plot in the world you're in",
      "- &b/infiniplots plotflag set interact allow 5 bigplots&r : Set a flag's value in the 5th plot of a specific world",
      "- &b/infiniplots plotflag clear interact&r : Clear a flag's value from the plot you're standing in",
      "- &b/infiniplots plotflag clear interact 5&r : Clear a flag's value from the 5th plot in the world you're in",
      "- &b/infiniplots plotflag clear interact 5 bigplots&r : Clear a flag's value from the 5th plot in a specific world",
      "See also:",
      "- &b/infiniplots manual flag",
      "- &b/infiniplots flag list"
  );

  private static final List<String> ACTIONS = List.of("list", "set", "clear");

  @Override
  public String getUsageArguments() {
    return "list [<plot number> [<world>]] | set <flag> <value> [<plot number> [<world>]] | clear <flag> [<plot number> [<world>]]";
  }

  @Override
  public List<String> getManual() {
    return MANUAL;
  }

  @Override
  public List<String> listSuggestions(CommandContext context) {
    switch (context.args().size()) {
      case 1 -> {
        return SuggestionHelper.fromCollection(ACTIONS, context.arg(0));
      }
      case 2 -> {
        String arg0 = context.arg(0);
        if (arg0.equals("set") || arg0.equals("clear")) {
          return SuggestionHelper.flags(context.arg(1));
        }
      }
      case 3 -> {
        switch (context.arg(0)) {
          case "list" -> {
            return SuggestionHelper.plotWorlds(context.arg(2));
          }
          case "set" -> {
            return SuggestionHelper.flagValues(context.arg(2));
          }
        }
      }
      case 4 -> {
        if (context.arg(0).equals("clear")) {
          return SuggestionHelper.plotWorlds(context.arg(3));
        }
      }
      case 5 -> {
        if (context.arg(0).equals("set")) {
          return SuggestionHelper.plotWorlds(context.arg(4));
        }
      }
    }
    return super.listSuggestions(context);
  }

  @Override
  public boolean hasPermission(CommandSender sender) {
    return sender.hasPermission("infiniplots.plotflag");
  }

  @Override
  public void execute(CommandContext context) throws InterruptCommandException {
    String action = ArgumentHelper.getInSet(context, 0, ACTIONS);
    switch (action) {
      case "list" -> {
        NotEnoughPermissionException.check(context.sender(), "infiniplots.plotflag.list");
        var pair = ArgumentHelper.getWorldAndPlotNumber(context, 1);
        PlotWorld plotWorld = pair.left();
        int plotNumber = pair.right();
        List<String> message = new ArrayList<>();
        message.add("&7=== &6Plot Flags for Plot #&b%s&6 in &e%s &7===".formatted(plotNumber, plotWorld.name));
        List<Flag> flags = InfiniPlots.getInstance().getPlotManager().getPlotFlagsRepository().getFlags(plotWorld.world.getUID(), plotNumber);
        if (flags.isEmpty()) {
          message.add("- &7&oThis plot has no specified flags");
        } else {
          flags.stream().sorted(Comparator.comparing(Flag::name)).forEach(flag -> message.add("- &b%s&r: &e%s".formatted(flag.name(), flag.value().friendlyName)));
        }
        context.sendMessage(message);
      }
      case "set" -> {
        String flag = ArgumentHelper.getString(context, 1);
        NotEnoughPermissionException.check(context.sender(), "infiniplots.plotflag.modify." + flag);
        FlagValue value = ArgumentHelper.getFlagValue(context, 2);
        var pair = ArgumentHelper.getWorldAndPlotNumber(context, 3);
        PlotWorld plotWorld = pair.left();
        int plotNumber = pair.right();
        plotWorld.setPlotFlag(plotNumber, flag, value);
        InfiniPlots.getInstance().getPlotManager().getPlotFlagsRepository().insertOrUpdate(plotWorld.world.getUID(), plotNumber, flag, value);
        context.sendMessage("&aFlag has been set");
      }
      case "clear" -> {
        String flag = ArgumentHelper.getString(context, 1);
        NotEnoughPermissionException.check(context.sender(), "infiniplots.plotflag.modify." + flag);
        var pair = ArgumentHelper.getWorldAndPlotNumber(context, 2);
        PlotWorld plotWorld = pair.left();
        int plotNumber = pair.right();
        plotWorld.setPlotFlag(plotNumber, flag, null);
        InfiniPlots.getInstance().getPlotManager().getPlotFlagsRepository().removePlotFlag(plotWorld.world.getUID(), plotNumber, flag);
        context.sendMessage("&aFlag has been cleared");
      }
    }
  }

}
