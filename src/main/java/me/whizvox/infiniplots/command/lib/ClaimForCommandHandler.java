package me.whizvox.infiniplots.command.lib;

import me.whizvox.infiniplots.command.ArgumentHelper;
import me.whizvox.infiniplots.command.CommandContext;
import me.whizvox.infiniplots.command.SuggestionHelper;
import me.whizvox.infiniplots.exception.InterruptCommandException;
import me.whizvox.infiniplots.exception.NotEnoughPermissionException;
import me.whizvox.infiniplots.plot.PlotWorld;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class ClaimForCommandHandler extends AbstractClaimCommandHandler {

  private static final String
      OPTION_OVERRIDE = "override",
      PERMISSION_OVERRIDE = "infiniplots.claimfor.override";

  private static final List<String> MANUAL = List.of(
      "Claims a plot for another online player. The target player must already have permission to claim plots. This " +
      "restriction can be overridden if specified and you have permission to do so.",
      "Examples:",
      "- &b/infiniplots claimfor Gamer72&r : Claims the next available plot for the target player in their world or the default world",
      "- &b/infiniplots claimfor Gamer72 41&r : Claims the 41st plot for the target player in their world or the default world",
      "- &b/infiniplots claimfor Gamer72 32 bigplots&r : Claims the 32nd plot in the &ebigplots&r world for the target player",
      "- &b/infiniplots claimfor Gamer72 20 bigplots override&r : Claims a plot for another player while ignoring their permissions",
      "See also:",
      "- &b/infiniplots manual claim",
      "- &b/infiniplots manual claimhere",
      "- &b/infiniplots manual unclaimfor"
  );

  @Override
  public String getUsageArguments() {
    return "<player> [<plot number> [<world> [override]]]";
  }

  @Override
  public List<String> getManual() {
    return MANUAL;
  }

  @Override
  public List<String> listSuggestions(CommandContext context) {
    return switch (context.args().size()) {
      case 1 -> SuggestionHelper.onlinePlayerNames(context.args().get(0));
      case 3 -> SuggestionHelper.plotWorlds(context.args().get(2));
      case 4 -> {
        if (context.sender().hasPermission(PERMISSION_OVERRIDE) && OPTION_OVERRIDE.startsWith(context.args().get(3))) {
          yield List.of(OPTION_OVERRIDE);
        } else {
          yield List.of();
        }
      }
      default -> super.listSuggestions(context);
    };
  }

  @Override
  protected ClaimCommandArguments getArguments(CommandContext context) {
    Player player = ArgumentHelper.getOnlinePlayer(context, 0);
    if (player == context.sender()) {
      throw new InterruptCommandException("Cannot select self");
    }
    PlotWorld plotWorld = ArgumentHelper.getPlotWorld(context, 2);
    int plotNumber = ArgumentHelper.getPlotNumber(context, 1, () -> {
      plotWorld.nextPlotNumber = plotWorld.calculateNextUnclaimedPlot();
      return plotWorld.nextPlotNumber;
    });
    String override = ArgumentHelper.getString(context, 3, () -> null);
    if (override != null && !OPTION_OVERRIDE.equals(override)) {
      throw new InterruptCommandException("Only \"override\" is allowed here");
    }
    if (override != null && !context.sender().hasPermission(PERMISSION_OVERRIDE)) {
      throw new NotEnoughPermissionException("Do not have permission to override target permissions");
    }
    return new ClaimCommandArguments(player, plotWorld, plotNumber, override != null);
  }

  @Override
  public boolean hasPermission(CommandSender sender) {
    return sender.hasPermission("infiniplots.claimfor");
  }

}
