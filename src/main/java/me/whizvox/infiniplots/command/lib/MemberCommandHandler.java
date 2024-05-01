package me.whizvox.infiniplots.command.lib;

import me.whizvox.infiniplots.InfiniPlots;
import me.whizvox.infiniplots.command.ArgumentHelper;
import me.whizvox.infiniplots.command.CommandContext;
import me.whizvox.infiniplots.command.CommandHandler;
import me.whizvox.infiniplots.command.SuggestionHelper;
import me.whizvox.infiniplots.exception.InterruptCommandException;
import me.whizvox.infiniplots.exception.InvalidCommandArgumentException;
import me.whizvox.infiniplots.exception.NotEnoughPermissionException;
import me.whizvox.infiniplots.plot.Plot;
import me.whizvox.infiniplots.util.CommandHelper;
import me.whizvox.infiniplots.util.PlayerUtils;
import me.whizvox.infiniplots.util.PlotId;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class MemberCommandHandler extends CommandHandler {

  private static final String
      PERMISSION_BASE = "infiniplots.member",
      PERMISSION_LIST = PERMISSION_BASE + ".list",
      PERMISSION_LIST_OTHERS = PERMISSION_LIST + ".others",
      PERMISSION_MODIFY = PERMISSION_BASE + ".modify",
      PERMISSION_MODIFY_OTHERS = PERMISSION_MODIFY + ".others";

  private static final List<String> MANUAL = List.of(
      "Manages members for your plot. Members have the same build permissions as the owner of a plot.",
      "Examples:",
      "- &b/infiniplots member list&r : List all members for the one plot you own",
      "- &b/infiniplots member add Bob123&r : Add a player to the members list of the plot you're standing in",
      "- &b/infiniplots member remove Bob123 3&r : Remove a member from your 3rd plot"
  );
  private static final List<String> ACTIONS = List.of("list", "add", "remove", "clear");

  @Override
  public String getUsageArguments() {
    return "list [<plot>] | add <player> [<plot>] | remove <player> [<plot>] | clear [<plot>]";
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
        switch (context.arg(0)) {
          case "list", "clear" -> {
            return SuggestionHelper.ownerPlotNumbers(context.arg(1), context.getPlayerOrException().getUniqueId());
          }
          case "add", "remove" -> {
            return SuggestionHelper.offlinePlayerNames(context.arg(1));
          }
        }
      }
      case 3 -> {
        if (context.arg(0).equals("add") || context.arg(0).equals("remove")) {
          return SuggestionHelper.ownerPlotNumbers(context.arg(2), context.getPlayerOrException().getUniqueId());
        }
      }
    }
    return super.listSuggestions(context);
  }

  @Override
  public boolean hasPermission(CommandSender sender) {
    return sender.hasPermission("infiniplots.member");
  }

  @Override
  public void execute(CommandContext context) throws InterruptCommandException {
    String action = ArgumentHelper.getInSet(context, 0, ACTIONS);
    switch (action) {
      case "list" -> {
        PlotId plotId = ArgumentHelper.getPlotIdOrFirstOwned(context, 1);
        Plot plot = CommandHelper.getPlot(plotId, true);
        if (!(context.sender() instanceof Player player) || !plot.owner().equals(player.getUniqueId())) {
          NotEnoughPermissionException.check(context.sender(), PERMISSION_LIST_OTHERS);
        } else {
          NotEnoughPermissionException.check(context.sender(), PERMISSION_LIST);
        }
        String membersStr;
        if (plot.members().isEmpty()) {
          membersStr = "&b&o<none>";
        } else {
          membersStr = plot.members().stream()
              .map(id -> "&b" + PlayerUtils.getOfflinePlayerName(id))
              .sorted()
              .collect(Collectors.joining("&r, "));
        }
        context.sendMessage("Members for &e%s&r: %s", plotId.getFriendlyString(), membersStr);
      }
      case "add", "remove" -> {
        PlotId plotId = ArgumentHelper.getPlotIdOrFirstOwned(context, 2);
        Plot plot = CommandHelper.getPlot(plotId, true);
        if (!(context.sender() instanceof Player player) || !plot.owner().equals(player.getUniqueId())) {
          NotEnoughPermissionException.check(context.sender(), PERMISSION_MODIFY_OTHERS);
        } else {
          NotEnoughPermissionException.check(context.sender(), PERMISSION_MODIFY);
        }
        OfflinePlayer member = ArgumentHelper.getOfflinePlayer(context, 1);
        if (member == context.sender()) {
          throw new InterruptCommandException("Cannot add or remove yourself");
        }
        boolean shouldAdd = action.equals("add");
        boolean alreadyContains = plot.members().contains(member.getUniqueId());
        if (shouldAdd && alreadyContains) {
          throw new InterruptCommandException("That player is already a member");
        } else if (!shouldAdd && !alreadyContains) {
          throw new InterruptCommandException("That player is not a member of that plot");
        }
        if (shouldAdd) {
          InfiniPlots.getInstance().getPlotManager().addMember(plotId, member.getUniqueId());
          context.sendMessage("&aSuccessfully added &b%s&a to &e%s", member.getName(), plotId.getFriendlyString());
        } else {
          InfiniPlots.getInstance().getPlotManager().removeMember(plotId, member.getUniqueId());
          context.sendMessage("&aSuccessfully removed &b%s&a from &e%s", member.getName(), plotId.getFriendlyString());
        }
      }
      case "clear" -> {
        PlotId plotId = ArgumentHelper.getPlotIdOrFirstOwned(context, 1);
        Plot plot = CommandHelper.getPlot(plotId, true);
        if (!(context.sender() instanceof Player player) || !plot.owner().equals(player.getUniqueId())) {
          NotEnoughPermissionException.check(context.sender(), PERMISSION_MODIFY_OTHERS);
        } else {
          NotEnoughPermissionException.check(context.sender(), PERMISSION_MODIFY);
        }
        if (plot.members().isEmpty()) {
          context.sendMessage("&cYou have not added any members to this plot");
        } else if (plot.members().size() == 1) {
          clearMembers(context, plotId);
        } else {
          InfiniPlots.getInstance().getConfirmationManager().add(context.sender(), () -> clearMembers(context, plotId));
        }
      }
      default -> throw new InvalidCommandArgumentException("Unknown action: " + action);
    }
  }

  private void clearMembers(CommandContext context, PlotId plotId) {
    InfiniPlots.getInstance().getPlotManager().clearMembers(plotId);
    context.sendMessage("&aSuccessfully cleared out all members from &b%s", plotId.getFriendlyString());
  }

}
