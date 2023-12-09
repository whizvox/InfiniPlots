package me.whizvox.infiniplots.command.lib;

import me.whizvox.infiniplots.InfiniPlots;
import me.whizvox.infiniplots.command.ArgumentHelper;
import me.whizvox.infiniplots.command.CommandContext;
import me.whizvox.infiniplots.command.SuggestionHelper;
import me.whizvox.infiniplots.exception.InterruptCommandException;
import me.whizvox.infiniplots.plot.Plot;
import me.whizvox.infiniplots.plot.PlotId;
import me.whizvox.infiniplots.plot.PlotWorld;
import me.whizvox.infiniplots.util.ChunkPos;
import me.whizvox.infiniplots.util.WorldUtils;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class ResetCommandHandler extends AbstractRegenerateCommandHandler {

  public record ResetArguments(World world, List<ChunkPos> chunks, boolean keepFlags, Plot plot)
      implements AbstractRegenerateCommandHandler.Arguments {}

  private static final List<String> MANUAL = List.of(
      "Resets one of your plots back to its original state, including its blocks and plot flags.",
      "Examples:",
      "- &b/infiniplots reset&r : Resets the plot you're currently standing on if you own it",
      "- &b/infiniplots reset 3&r : Reset your 3rd plot",
      "- &b/infiniplots reset 3 keepflags&r : Reset your 3rd plot while keeping its plot flags",
      "See also:",
      "- &b/infiniplots manual regen"
  );

  private static final List<String> KEEPFLAGS = List.of("keepflags");

  @Override
  public String getUsageArguments() {
    return "[<owner plot number> [keepflags]]";
  }

  @Override
  public List<String> getManual() {
    return MANUAL;
  }

  @Override
  public List<String> listSuggestions(CommandContext context) {
    if (context.sender() instanceof Player player) {
      if (context.args().size() == 1) {
        return SuggestionHelper.ownerPlotNumbers(context.arg(0), player.getUniqueId());
      } else if (context.args().size() == 2) {
        return SuggestionHelper.fromCollection(KEEPFLAGS, context.arg(1), false);
      }
    }
    return super.listSuggestions(context);
  }

  @Override
  public boolean hasPermission(CommandSender sender) {
    return sender.hasPermission("infiniplots.reset");
  }

  @Override
  protected ResetArguments getArguments(CommandContext context) {
    Player player = context.getPlayerOrException();
    boolean[] oidSet = new boolean[] { true };
    int oid = ArgumentHelper.getInt(context, 0, () -> {
      oidSet[0] = false;
      return 1;
    }, 1, Integer.MAX_VALUE);
    boolean keepFlags = ArgumentHelper.getString(context, 1, () -> "").equals("keepflags");
    Plot plot;
    PlotWorld plotWorld;
    if (oidSet[0]) {
      plot = InfiniPlots.getInstance().getPlotManager().getPlot(player.getUniqueId(), oid, false);
      if (plot == null) {
        throw new InterruptCommandException("You do not own a plot with OID of " + oid);
      }
      plotWorld = InfiniPlots.getInstance().getPlotManager().getPlotWorld(plot.world());
      if (plotWorld == null) {
        throw new InterruptCommandException("Plot world " + plot.world() + " does not exist");
      }
    } else {
      plotWorld = WorldUtils.getPlotWorldOrDefault(player.getWorld().getUID());
      if (plotWorld == null) {
        throw new InterruptCommandException("Default world is not set");
      }
      int plotNumber = plotWorld.generator.getPlotNumber(new ChunkPos(player.getLocation()));
      if (plotNumber < 1) {
        throw new InterruptCommandException("Not standing in a plot");
      }
      plot = InfiniPlots.getInstance().getPlotManager().getPlot(new PlotId(plotWorld.world.getUID(), plotNumber), false);
      if (plot == null) {
        throw new InterruptCommandException("Not standing in a plot");
      }
    }
    return new ResetArguments(plotWorld.world, plotWorld.generator.getPlotChunks(plot.worldPlotId()), keepFlags, plot);
  }

  @Override
  protected void onPostConfirm(CommandContext context, Arguments args) {
    ResetArguments rArgs = (ResetArguments) args;
    if (!rArgs.keepFlags()) {
      InfiniPlots.getInstance().getPlotManager().getPlotFlagsRepository().removePlot(args.world().getUID(), rArgs.plot().worldPlotId());
    }
    context.sendMessage("&aPlot has been reset");
  }

}
