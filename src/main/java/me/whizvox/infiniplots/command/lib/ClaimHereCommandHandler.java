package me.whizvox.infiniplots.command.lib;

import me.whizvox.infiniplots.InfiniPlots;
import me.whizvox.infiniplots.command.CommandContext;
import me.whizvox.infiniplots.exception.InterruptCommandException;
import me.whizvox.infiniplots.plot.PlotWorld;
import me.whizvox.infiniplots.util.ChunkPos;
import org.bukkit.entity.Player;

import java.util.List;

public class ClaimHereCommandHandler extends AbstractClaimCommandHandler {

  private static final List<String> MANUAL = List.of(
      "Claims the plot you're currently standing in. As expected, this will only work in you're in a plot world and " +
      "are standing in a plot area. This has the same permission requirement as the regular &eclaim&r command.",
      "Examples:",
      "- &b/infiniplots claimhere&r : Claims the plot you're standing in",
      "See also:",
      "- &b/infiniplots manual claim",
      "- &b/infiniplots manual claimfor",
      "- &b/infiniplots manual unclaim"
  );

  @Override
  public List<String> getManual() {
    return MANUAL;
  }

  @Override
  protected ClaimCommandArguments getArguments(CommandContext context) {
    Player player = context.getPlayerOrException();
    PlotWorld plotWorld = InfiniPlots.getInstance().getPlotManager().getPlotWorld(player.getWorld().getUID());
    if (plotWorld == null) {
      throw new InterruptCommandException("Not in a plot");
    }
    int plotNumber = plotWorld.generator.getPlotNumber(new ChunkPos(player.getLocation()));
    if (plotNumber < 1) {
      throw new InterruptCommandException("Not in a plot");
    }
    return new ClaimCommandArguments(player, plotWorld, plotNumber);
  }

}
