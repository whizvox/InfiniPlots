package me.whizvox.infiniplots.command.lib;

import me.whizvox.infiniplots.command.ArgumentHelper;
import me.whizvox.infiniplots.command.CommandContext;
import me.whizvox.infiniplots.command.SuggestionHelper;
import me.whizvox.infiniplots.plot.PlotWorld;
import org.bukkit.entity.Player;

import java.util.List;

public class ClaimCommandHandler extends AbstractClaimCommandHandler {

  private static final List<String> MANUAL = List.of(
      "Will claim a plot for yourself. Optionally can provide arguments to specify the exact plot number and world.",
      "The claim will only go through if you have permission to claim any plots at all, if you haven't yet reached " +
      "the maximum number of claimed plots, and if the arguments specified don't conflict with any preexisting plots.",
      "Examples:",
      "- &b/infiniplots claim&r : Claims the next available plot in this world or the default world",
      "- &b/infiniplots claim 42&r : Claims the 42nd plot in this world or the default world",
      "- &b/infiniplots claim 57 bigplots&r : Claims the 57th plot in the &ebigplots&r world",
      "See also:",
      "- &b/infiniplots manual claimfor",
      "- &b/infiniplots manual claimhere",
      "- &b/infiniplots manual unclaim"
  );

  @Override
  public String getUsageArguments() {
    return "[<plot number> [<world>]]";
  }

  @Override
  public List<String> getManual() {
    return MANUAL;
  }

  @Override
  public List<String> listSuggestions(CommandContext context) {
    if (context.args().size() == 2) {
      return SuggestionHelper.plotWorlds(context.args().get(1));
    }
    return super.listSuggestions(context);
  }

  @Override
  protected ClaimCommandArguments getArguments(CommandContext context) {
    Player player = context.getPlayerOrException();
    PlotWorld plotWorld = ArgumentHelper.getPlotWorld(context, 1);
    int plotNumber = ArgumentHelper.getPlotNumber(context, 0, () -> {
      plotWorld.nextPlotNumber = plotWorld.calculateNextUnclaimedPlot();
      return plotWorld.nextPlotNumber;
    });
    return new ClaimCommandArguments(player, plotWorld, plotNumber, false);
  }

}
