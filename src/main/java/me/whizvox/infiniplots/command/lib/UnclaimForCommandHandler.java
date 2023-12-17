package me.whizvox.infiniplots.command.lib;

import me.whizvox.infiniplots.InfiniPlots;
import me.whizvox.infiniplots.command.ArgumentHelper;
import me.whizvox.infiniplots.command.CommandContext;
import me.whizvox.infiniplots.exception.InterruptCommandException;
import me.whizvox.infiniplots.exception.MissingArgumentException;
import me.whizvox.infiniplots.plot.Plot;
import me.whizvox.infiniplots.util.PlotId;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
  public List<String> listSuggestions(CommandContext context) {
    if (context.args().size() == 1) {
      String query = context.args().get(0);
      return Bukkit.getOnlinePlayers().stream()
          .filter(player -> player != context.sender() && player.getName().startsWith(query))
          .map(Player::getName)
          .sorted()
          .toList();
    } else if (context.args().size() == 2) {
      Player player = Bukkit.getPlayer(context.args().get(0));
      if (player != null) {
        String query = context.args().get(1);
        return InfiniPlots.getInstance().getPlotManager().getPlots(player.getUniqueId(), false).stream()
            .map(plot -> String.valueOf(plot.ownerNumber()))
            .filter(oid -> oid.startsWith(query))
            .toList();
      }
    }
    return super.listSuggestions(context);
  }

  @Override
  public boolean hasPermission(CommandSender sender) {
    return sender.hasPermission("infiniplots.unclaimfor");
  }

  @Override
  protected Arguments getArguments(CommandContext context) throws InterruptCommandException {
    OfflinePlayer player = ArgumentHelper.getOfflinePlayer(context, 0);
    if (player == context.sender()) {
      throw new InterruptCommandException("Cannot select self");
    }
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
      oid = plot.ownerNumber();
    } else {
      oid = ArgumentHelper.getInt(context, 1, 1, Integer.MAX_VALUE);
      Plot plot = InfiniPlots.getInstance().getPlotManager().getPlot(PlotId.fromOwner(player.getUniqueId(), oid), false);
      if (plot == null) {
        throw new InterruptCommandException("No plot found with that ID");
      }
    }
    return new Arguments(player, oid);
  }

}
