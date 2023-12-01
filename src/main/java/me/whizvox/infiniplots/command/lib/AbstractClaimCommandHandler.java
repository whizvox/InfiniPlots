package me.whizvox.infiniplots.command.lib;

import me.whizvox.infiniplots.InfiniPlots;
import me.whizvox.infiniplots.command.CommandContext;
import me.whizvox.infiniplots.command.CommandHandler;
import me.whizvox.infiniplots.exception.InterruptCommandException;
import me.whizvox.infiniplots.plot.Plot;
import me.whizvox.infiniplots.plot.PlotWorld;
import me.whizvox.infiniplots.util.ChatUtils;
import me.whizvox.infiniplots.util.PermissionUtils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.List;
import java.util.Map;

public abstract class AbstractClaimCommandHandler extends CommandHandler {

  private final String
      PERMISSION_BASE = PermissionUtils.buildPermission("claim"),
      PERMISSION_INF = PERMISSION_BASE + ".inf";

  @Override
  public boolean hasPermission(CommandSender sender) {
    return sender.hasPermission(PERMISSION_BASE);
  }

  protected abstract ClaimCommandArguments getArguments(CommandContext context);

  @Override
  public void execute(CommandContext context) throws InterruptCommandException {
    ClaimCommandArguments args = getArguments(context);
    Player player = args.player;
    PlotWorld plotWorld = args.plotWorld;
    int plotNumber = args.plotNumber;
    int maxPlots;
    if (args.bypassPermissions || player.hasPermission(PERMISSION_INF)) {
      maxPlots = Integer.MAX_VALUE;
    } else {
      maxPlots = 0;
      for (Map.Entry<String, Integer> entry : InfiniPlots.getInstance().getOwnerTiers().entrySet()) {
        if (player.hasPermission("infiniplots.claim." + entry.getKey()) && entry.getValue() > maxPlots) {
          maxPlots = entry.getValue();
        }
      }
      if (maxPlots == 0) {
        throw new InterruptCommandException("Not allowed to own any plots");
      }
    }
    List<Plot> plots = InfiniPlots.getInstance().getPlotManager().getPlots(player.getUniqueId(), false);
    if (plots.size() >= maxPlots) {
      throw new InterruptCommandException("Max plot count reached (" + maxPlots + ")");
    }
    Location location = plotWorld.generator.getTeleportLocation(plotWorld.world, plotNumber);
    if (location == null) {
      throw new InterruptCommandException("Could not determine plot location");
    }
    int ownerPlotId = 1;
    for (Plot plot : plots) {
      if (ownerPlotId == plot.ownerPlotId()) {
        ownerPlotId++;
      }
    }
    InfiniPlots.getInstance().getPlotManager().addPlot(plotWorld.world.getUID(), plotNumber, player.getUniqueId(), ownerPlotId);
    player.teleport(location, PlayerTeleportEvent.TeleportCause.COMMAND);
    if (player == context.sender()) {
      context.sendMessage("Claimed plot #&b%s&r in &e%s", plotNumber, plotWorld.name);
    } else {
      context.sendMessage("Claimed plot #&b%s&r in &e%s&r for &a%s", plotNumber, plotWorld.name, player.getName());
      player.sendMessage(ChatUtils.altColorsf("Plot #&b%s&r in &e%s%r has been claimed on your behalf from &a%s", plotNumber, plotWorld.name, context.sender().getName()));
    }
  }

  public record ClaimCommandArguments(Player player, PlotWorld plotWorld, int plotNumber, boolean bypassPermissions) {
  }

}
