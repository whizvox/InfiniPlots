package me.whizvox.infiniplots.command.lib;

import me.whizvox.infiniplots.command.ArgumentHelper;
import me.whizvox.infiniplots.command.CommandContext;
import me.whizvox.infiniplots.command.CommandHandler;
import me.whizvox.infiniplots.command.SuggestionHelper;
import me.whizvox.infiniplots.exception.InterruptCommandException;
import me.whizvox.infiniplots.plot.PlotWorld;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.List;

public class TeleportByWorldCommandHandler extends CommandHandler {

  private static final List<String> MANUAL = List.of(
      "Teleport to a plot by specifying its plot number and, optionally, the world it's in.",
      "Examples:",
      "- &b/infiniplots tp 42&r : Teleport to plot #42 in your world or the default world",
      "- &b/infiniplots tp 42 bigplots&r : Teleport to plot #42 in the bigplots world",
      "See also:",
      "- &b/infiniplots manual tpowner",
      "- &b/infiniplots manual tpworld"
  );

  @Override
  public String getUsageArguments() {
    return "<plot number> [<world>]";
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
  public boolean hasPermission(CommandSender sender) {
    return sender.hasPermission("infiniplots.tpplot");
  }

  @Override
  public void execute(CommandContext context) throws InterruptCommandException {
    Player player = context.getPlayerOrException();
    int wid = ArgumentHelper.getInt(context, 0, 1, Integer.MAX_VALUE);
    PlotWorld plotWorld = ArgumentHelper.getPlotWorld(context, 1);
    Location location = plotWorld.generator.getTeleportLocation(plotWorld.world, wid);
    if (location == null) {
      throw new InterruptCommandException("Could not get location");
    }
    context.sendMessage("Teleporting to plot #&b%s&r in &e%s&r...", wid, plotWorld.world.getName());
    player.teleport(location, PlayerTeleportEvent.TeleportCause.COMMAND);
  }

}
