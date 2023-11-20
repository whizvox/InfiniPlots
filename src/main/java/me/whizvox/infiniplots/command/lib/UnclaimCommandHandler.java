package me.whizvox.infiniplots.command.lib;

import me.whizvox.infiniplots.InfiniPlots;
import me.whizvox.infiniplots.command.ArgumentHelper;
import me.whizvox.infiniplots.command.CommandContext;
import me.whizvox.infiniplots.exception.InterruptCommandException;
import me.whizvox.infiniplots.plot.Plot;
import me.whizvox.infiniplots.plot.PlotId;
import me.whizvox.infiniplots.plot.PlotWorld;
import me.whizvox.infiniplots.util.ChunkPos;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class UnclaimCommandHandler extends AbstractUnclaimCommandHandler {

  private static final List<String> MANUAL = List.of(
      "Unclaim one of your own plots. Run &b/infiniplots list&r to list all of your owned plots.",
      "Examples:",
      "- &b/infiniplots unclaim&r : If standing in an owned plot, unclaim it",
      "- &b/infiniplots unclaim 2&r : Unclaim your 2nd owned plot",
      "See also:",
      "- &b/infiniplots manual unclaimfor"
  );

  @Override
  public List<String> listSuggestions(CommandContext context) {
    if (context.sender() instanceof Player player) {
      List<String> suggestions = new ArrayList<>();
      InfiniPlots.getInstance().getPlotManager().getPlots(player.getUniqueId(), false)
          .forEach(plot -> suggestions.add(String.valueOf(plot.ownerPlotId())));
      return suggestions;
    }
    return super.listSuggestions(context);
  }

  @Override
  public String getUsageArguments() {
    return "[<owner plot number>]";
  }

  @Override
  public List<String> getManual() {
    return MANUAL;
  }

  @Override
  protected Arguments getArguments(CommandContext context) throws InterruptCommandException {
    Player player = context.getPlayerOrException();
    PlotWorld plotWorld;
    int oid;
    if (context.args().isEmpty()) {
      plotWorld = InfiniPlots.getInstance().getPlotManager().getPlotWorld(player.getWorld().getUID());
      if (plotWorld == null) {
        throw new InterruptCommandException("Not in a plot");
      }
      int wid = plotWorld.generator.getPlotNumber(new ChunkPos(player.getLocation()));
      if (wid < 1) {
        throw new InterruptCommandException("Not in a plot");
      }
      Plot plot = InfiniPlots.getInstance().getPlotManager().getPlot(new PlotId(plotWorld.world.getUID(), wid), false);
      if (plot == null || !plot.owner().equals(player.getUniqueId())) {
        throw new InterruptCommandException("You do not own this plot");
      }
      oid = plot.ownerPlotId();
    } else {
      oid = ArgumentHelper.getInt(context, 0, 1, Integer.MAX_VALUE);
      Plot plot = InfiniPlots.getInstance().getPlotManager().getPlot(player.getUniqueId(), oid, false);
      if (plot == null) {
        throw new InterruptCommandException("No plot found with OID of " + oid);
      }
    }
    return new Arguments(player, oid);
  }

}
