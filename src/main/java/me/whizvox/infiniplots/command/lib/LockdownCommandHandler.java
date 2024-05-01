package me.whizvox.infiniplots.command.lib;

import me.whizvox.infiniplots.InfiniPlots;
import me.whizvox.infiniplots.command.ArgumentHelper;
import me.whizvox.infiniplots.command.CommandContext;
import me.whizvox.infiniplots.command.CommandHandler;
import me.whizvox.infiniplots.command.SuggestionHelper;
import me.whizvox.infiniplots.exception.InterruptCommandException;
import me.whizvox.infiniplots.plot.LockdownLevel;
import me.whizvox.infiniplots.plot.PlotWorld;
import me.whizvox.infiniplots.util.ChatUtils;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LockdownCommandHandler extends CommandHandler {

  private static final String PERMISSION = "infiniplots.lockdown";

  private static final List<String> MANUAL = List.of(
      "Sets the lockdown level of a plot world, which can be used to temporarily limit certain interactions in a " +
      "plot world. Useful if you want to set a time limit on building, or if an exploit is found and damage needs to " +
      "be mitigated.",
      "There are 4 possible lockdown states: &eoff&r, &ebuild&r, &einteract&r, and &eenter&r. &eoff&r means there is " +
      "no lockdown in place. &ebuild&r means building is denied, but block and item interactions are allowed. " +
      "&einteract&r means building and interactions are denied, but players can still enter and move in the plot " +
      "world. And &eenter&r is the highest level of lockdown, preventing players from even entering the plot world.",
      "Examples:",
      "- &b/infiniplots lockdown&r : Prevent building of the world you're in or the default world",
      "- &b/infiniplots lockdown off&r : Turn off lockdown in the world you're in or the default world",
      "- &b/infiniplots lockdown interact bigplots&r : Prevent interactions and building in the &ebigplots&r world",
      "- &b/infiniplots lockdown build *&r : Prevent building in &oall&r plot worlds",
      "See also:",
      "- &b/infiniplots man worldinfo"
  );

  @Override
  public String getUsageArguments() {
    return "[off|build|interact|enter [<world>|*]]";
  }

  @Override
  public List<String> getManual() {
    return MANUAL;
  }

  @Override
  public List<String> listSuggestions(CommandContext context) {
    if (context.args().size() == 1) {
      return SuggestionHelper.fromStream(Arrays.stream(LockdownLevel.values()).map(lvl -> lvl.toString().toLowerCase()), context.arg(0));
    }
    if (context.args().size() == 2) {
      List<String> suggestions = new ArrayList<>(SuggestionHelper.plotWorlds(context.arg(1)));
      suggestions.add("*");
      return suggestions;
    }
    return super.listSuggestions(context);
  }

  @Override
  public boolean hasPermission(CommandSender sender) {
    return sender.hasPermission(PERMISSION);
  }

  @Override
  public void execute(CommandContext context) throws InterruptCommandException {
    LockdownLevel level = ArgumentHelper.getArgument(context, 0, () -> LockdownLevel.INTERACT, s -> LockdownLevel.valueOf(s.toUpperCase()));
    List<PlotWorld> worlds;
    if (context.args().size() < 2) {
      worlds = List.of(ArgumentHelper.getPlotWorld(context, 1));
    } else {
      if (context.arg(1).equals("*")) {
        worlds = InfiniPlots.getInstance().getPlotManager().plotWorlds().toList();
      } else {
        worlds = List.of(ArgumentHelper.getPlotWorld(context, 1));
      }
    }
    worlds.forEach(plotWorld -> {
      setLockdownStatus(plotWorld, level);
      switch (level) {
        case OFF -> context.sendMessage("Lockdown has been lifted in &e%s", plotWorld.world.getName());
        case BUILD -> context.sendMessage("Building has been disabled in &e%s", plotWorld.world.getName());
        case INTERACT -> context.sendMessage("Building and interactions have been disabled in &e%s", plotWorld.world.getName());
        case ENTER -> context.sendMessage("Player entry has been disabled in &e%s", plotWorld.world.getName());
      }
    });
  }

  public static void setLockdownStatus(PlotWorld plotWorld, LockdownLevel level) {
    plotWorld.lockdownLevel = level;
    InfiniPlots.getInstance().getPlotManager().getWorldRepository().updateLockdownLevel(plotWorld.world.getUID(), level);
    if (level == LockdownLevel.ENTER) {
      plotWorld.world.getPlayers().forEach(player -> {
        if (!player.hasPermission("infiniplots.lockdown.bypass.enter")) {
          World destWorld = InfiniPlots.getInstance().getPlotManager().getKickDestinationWorld();
          if (destWorld != null && !destWorld.getUID().equals(plotWorld.world.getUID())) {
            player.sendMessage(ChatUtils.altColorsf("Plot world &e%s&r is in lockdown", plotWorld.name));
            player.teleport(destWorld.getSpawnLocation());
          }
        }
      });
    }
  }

}
