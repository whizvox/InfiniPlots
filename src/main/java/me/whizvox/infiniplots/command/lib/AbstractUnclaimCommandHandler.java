package me.whizvox.infiniplots.command.lib;

import me.whizvox.infiniplots.InfiniPlots;
import me.whizvox.infiniplots.command.CommandContext;
import me.whizvox.infiniplots.command.CommandHandler;
import me.whizvox.infiniplots.exception.InterruptCommandException;
import me.whizvox.infiniplots.util.PlotId;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

public abstract class AbstractUnclaimCommandHandler extends CommandHandler {

  @Override
  public boolean hasPermission(CommandSender sender) {
    return sender.hasPermission("infiniplots.claim");
  }

  @Override
  public void execute(CommandContext context) throws InterruptCommandException {
    Arguments args = getArguments(context);
    InfiniPlots.getInstance().getConfirmationManager().add(context.sender(), () -> {
      InfiniPlots.getInstance().getPlotManager().removePlot(PlotId.fromOwner(args.target.getUniqueId(), args.ownerPlotNumber));
      context.sendMessage("Plot #&b%d&r for &e%s&r has been unclaimed", args.ownerPlotNumber, args.target.getName());
    });
  }

  protected abstract Arguments getArguments(CommandContext context) throws InterruptCommandException;

  public record Arguments(OfflinePlayer target, int ownerPlotNumber) {}

}
