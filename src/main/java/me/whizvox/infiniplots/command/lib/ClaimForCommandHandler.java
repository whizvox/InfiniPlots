package me.whizvox.infiniplots.command.lib;

import me.whizvox.infiniplots.command.ArgumentHelper;
import me.whizvox.infiniplots.command.CommandContext;
import me.whizvox.infiniplots.command.SuggestionHelper;
import me.whizvox.infiniplots.plot.PlotWorld;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class ClaimForCommandHandler extends AbstractClaimCommandHandler {

  private static final List<String> MANUAL = List.of(
      "Claims a plot for another online player. The target player must already have permission to claim plots. This " +
      "restriction can be overridden if specified and you have permission to do so.",
      "Examples:",
      "- &b/infiniplots claimfor Gamer72&r : Claims the next available plot for the target player in their world or the default world",
      "- &b/infiniplots claimfor Gamer72 41&r : Claims the 41st plot for the target player in their world or the default world",
      "- &b/infiniplots claimfor Gamer72 32 bigplots&r : Claims the 32nd plot in the &ebigplots&r world for the target player",
      "See also:",
      "- &b/infiniplots manual claim",
      "- &b/infiniplots manual claimhere",
      "- &b/infiniplots manual unclaimfor"
  );

  @Override
  public String getUsageArguments() {
    return "<player> [<plot number> [<world>]]";
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
      default -> super.listSuggestions(context);
    };
  }

  @Override
  protected ClaimCommandArguments getArguments(CommandContext context) {
    Player player = ArgumentHelper.getOnlinePlayer(context, 0);
    PlotWorld plotWorld = ArgumentHelper.getPlotWorld(context, 2);
    int plotNumber = ArgumentHelper.getPlotNumber(context, 1, () -> {
      plotWorld.nextPlotNumber = plotWorld.calculateNextUnclaimedPlot();
      return plotWorld.nextPlotNumber;
    });
    return new ClaimCommandArguments(player, plotWorld, plotNumber);
  }

  @Override
  public boolean hasPermission(CommandSender sender) {
    return sender.hasPermission("infiniplots.claimfor");
  }

}
