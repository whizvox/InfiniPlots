package me.whizvox.infiniplots.command.lib;

import me.whizvox.infiniplots.InfiniPlots;
import me.whizvox.infiniplots.command.ArgumentHelper;
import me.whizvox.infiniplots.command.CommandContext;
import me.whizvox.infiniplots.command.CommandHandler;
import me.whizvox.infiniplots.command.SuggestionHelper;
import me.whizvox.infiniplots.exception.InterruptCommandException;
import me.whizvox.infiniplots.exception.NotEnoughPermissionException;
import me.whizvox.infiniplots.flag.DefaultFlags;
import me.whizvox.infiniplots.flag.Flag;
import me.whizvox.infiniplots.flag.FlagHelper;
import me.whizvox.infiniplots.flag.Flags;
import me.whizvox.infiniplots.plot.PlotWorld;
import me.whizvox.infiniplots.util.ChunkPos;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class FlagCommandHandler extends CommandHandler {

  private static final List<String> MANUAL = List.of(
      "Display information about flags.",
      "Examples:",
      "- &b/infiniplots flag list&r : List all flags and their default values",
      "- &b/infiniplots flag test interact&r : Test the &einteract&r flag for your location"
  );

  private static final List<String> ACTIONS = List.of("list", "test");

  @Override
  public String getUsageArguments() {
    return "list | test <flag>";
  }

  @Override
  public List<String> getManual() {
    return MANUAL;
  }

  @Override
  public List<String> listSuggestions(CommandContext context) {
    if (context.args().size() == 1) {
      return SuggestionHelper.fromStream(
          ACTIONS.stream().filter(action -> context.sender().hasPermission("infiniplots.flag." + action)),
          context.arg(0)
      );
    } else if (context.args().size() == 2) {
      if (context.sender().hasPermission("infiniplots.flag.test") && context.arg(0).equals("test")) {
        return SuggestionHelper.flags(context.arg(1));
      }
    } else if (context.args().size() == 3) {
      if (context.sender().hasPermission("infiniplots.flag.test") && context.arg(0).equals("test")) {
        return SuggestionHelper.plotWorlds(context.arg(2));
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
    switch (ArgumentHelper.getInSet(context, 0, ACTIONS)) {
      case "list" -> {
        NotEnoughPermissionException.check(context.sender(), "infiniplots.flag.list");
        List<String> message = new ArrayList<>();
        message.add("&7=== &rList of All Flags (&bflag&r:&edefault&r) &7===");
        message.add(DefaultFlags.ALL_FLAGS.values().stream()
            .sorted(Comparator.comparing(Flag::name))
            .map(flag -> "&b%s&r:&e%s&r".formatted(flag.name(), flag.value().friendlyName()))
            .collect(Collectors.joining(", "))
        );
        context.sendMessage(message);
      }
      case "test" -> {
        NotEnoughPermissionException.check(context.sender(), "infiniplots.flag.test");
        String flag = ArgumentHelper.getString(context, 1);
        Player player = context.getPlayerOrException();
        PlotWorld plotWorld = InfiniPlots.getInstance().getPlotManager().getPlotWorld(player);
        if (plotWorld == null) {
          throw new InterruptCommandException("Default world is not set up");
        }
        context.sendMessage("&7=== &rTesting Flag &b%s &7===", flag);
        if (!DefaultFlags.ALL_FLAGS.containsKey(flag)) {
          context.sendMessage("- &7&oFlag is either not a valid flag or is not in the default list", flag);
        }
        context.sendMessage("- &7World Value&r: &e%s&r%s", plotWorld.worldFlags.getValue(flag).friendlyName(), plotWorld.worldFlags.contains(flag) ? "" : " &a(default)");
        int plotNumber = plotWorld.generator.getPlotNumber(new ChunkPos(player.getLocation()));
        if (plotNumber > 0) {
          Flags flags = plotWorld.getPlotFlags(plotNumber);
          if (flags.contains(flag)) {
            context.sendMessage("- &7Plot Value&r: &e%s", flags.getValue(flag).friendlyName());
          } else {
            context.sendMessage("- &7Plot Value&r: &e&o<unset>");
          }
        }
        context.sendMessage("- &7Player Test: %s", FlagHelper.allowPlayerAction(plotWorld, player, player.getLocation(), flag) ? "&aALLOWED" : "&cDENIED");
        context.sendMessage("- &7Natural Test: %s", FlagHelper.allowNaturalAction(plotWorld, player.getLocation(), flag) ? "&aALLOWED" : "&cDENIED");
      }
    }
  }

}
