package me.whizvox.infiniplots.command.lib;

import me.whizvox.infiniplots.InfiniPlots;
import me.whizvox.infiniplots.command.ArgumentHelper;
import me.whizvox.infiniplots.command.CommandContext;
import me.whizvox.infiniplots.exception.InterruptCommandException;
import me.whizvox.infiniplots.exception.MissingArgumentException;
import me.whizvox.infiniplots.plot.Plot;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.List;

public class UnclaimForCommandHandler extends AbstractUnclaimCommandHandler {

  private static final List<String> MANUAL = List.of(
      "Unclaims a plot from another player. Run &b/infiniplots list plots <player>&r to get a list of a player's " +
      "plots.",
      "Examples:",
      "- &b/infiniplots unclaimfor Bob123&r : If a player only has 1 owned plot, unclaim it",
      "- &b/infiniplots unclaimfor Bob123 4&r : Unclaim a player's 4th plot",
      "See also:",
      "- &b/infiniplots manual unclaim"
  );

  @Override
  public List<String> getManual() {
    return MANUAL;
  }

  @Override
  public String getUsageArguments() {
    return "<owner> [<owner plot ID>]";
  }

  @Override
  public boolean hasPermission(CommandSender sender) {
    return sender.hasPermission("infiniplots.unclaimfor");
  }

  @Override
  protected Arguments getArguments(CommandContext context) throws InterruptCommandException {
    OfflinePlayer player = ArgumentHelper.getOfflinePlayer(context, 0);
    int oid;
    if (context.args().size() == 1) {
      List<Plot> plots = InfiniPlots.getInstance().getPlotManager().getPlots(player.getUniqueId(), false);
      if (plots.isEmpty()) {
        throw new InterruptCommandException("Target player has no owned plots");
      }
      if (plots.size() > 1) {
        throw new MissingArgumentException("Must specify owner plot ID if target has more than 1 owned plot");
      }
      Plot plot = plots.get(0);
      oid = plot.ownerPlotId();
    } else {
      oid = ArgumentHelper.getInt(context, 1, 1, Integer.MAX_VALUE);
      Plot plot = InfiniPlots.getInstance().getPlotManager().getPlot(player.getUniqueId(), oid, false);
      if (plot == null) {
        throw new InterruptCommandException("No plot found with that ID");
      }
    }
    return new Arguments(player, oid);
  }

}
