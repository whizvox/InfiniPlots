package me.whizvox.infiniplots.command.lib;

import me.whizvox.infiniplots.InfiniPlots;
import me.whizvox.infiniplots.command.ArgumentHelper;
import me.whizvox.infiniplots.command.CommandContext;
import me.whizvox.infiniplots.command.CommandHandler;
import me.whizvox.infiniplots.exception.InterruptCommandException;
import me.whizvox.infiniplots.flag.Flag;
import me.whizvox.infiniplots.plot.Plot;
import me.whizvox.infiniplots.plot.PlotId;
import me.whizvox.infiniplots.plot.PlotWorld;
import me.whizvox.infiniplots.util.PlayerUtils;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class InfoCommandHandler extends CommandHandler {

  private static final String USAGE = "[<plot number> [<world>]]";

  private static final List<String> MANUAL = List.of(
      "Display information about a plot.",
      "Examples:",
      "- &b/infiniplots info&r : Display information about the plot you're standing in",
      "- &b/infiniplots info 42&r : Display information about plot #42 plot in your world or the default world",
      "- &b/infiniplots info 42 bigplots&r : Display information about plot #42 in the bigplots worlds"
  );

  @Override
  public String getUsageArguments() {
    return USAGE;
  }

  @Override
  public List<String> getManual() {
    return MANUAL;
  }

  @Override
  public List<String> listSuggestions(CommandContext context) {
    if (context.args().size() == 2) {
      String worldNameQuery = context.args().get(1);
      return InfiniPlots.getInstance().getPlotManager().plotWorlds()
          .map(pw -> pw.world.getName())
          .filter(worldName -> worldName.startsWith(worldNameQuery))
          .sorted()
          .toList();
    }
    return super.listSuggestions(context);
  }

  @Override
  public boolean hasPermission(CommandSender sender) {
    return sender.hasPermission("infiniplots.info");
  }

  @Override
  public void execute(CommandContext context) throws InterruptCommandException {
    var pair = ArgumentHelper.getWorldAndPlotNumber(context, 2);
    PlotWorld plotWorld = pair.left();
    int plotNumber = pair.right();
    Plot plot = InfiniPlots.getInstance().getPlotManager().getPlot(new PlotId(plotWorld.world.getUID(), plotNumber), true);
    List<String> messages = new ArrayList<>();
    messages.add("&7===&r Information for Plot #&b%s&r in &b%s&r &7===".formatted(plotNumber, plotWorld.world.getName()));
    if (plot == null) {
      messages.add("- &7&oThis plot is unclaimed");
    } else {
      messages.add(("- &7World: &b%s".formatted(plotWorld.world.getName())));
      messages.add("- &7Plot Number&r: &b%s".formatted(plot.worldPlotId()));
      messages.add("- &7Owner&r: &b%s&r (&a%s&r)".formatted(PlayerUtils.getOfflinePlayerName(plot.owner()), plot.owner()));
      messages.add("- &7Owner ID&r: &b%d".formatted(plot.ownerPlotId()));
      String membersString;
      if (plot.members().isEmpty()) {
        membersString = "&b&o<none>&r";
      } else {
        membersString = plot.members().stream()
            .map(memberId -> "&b%s&r".formatted(PlayerUtils.getOfflinePlayerName(memberId)))
            .collect(Collectors.joining(", "));
      }
      messages.add("- &7Members&r: %s".formatted(membersString));
      String flagsString;
      if (plot.flags().isEmpty()) {
        flagsString = "&b&o<none>&r";
      } else {
        flagsString = StreamSupport.stream(plot.flags().spliterator(), false)
            .sorted(Comparator.comparing(Flag::name))
            .map(flag -> "&b%s&r:&e%s&r".formatted(flag.name(), flag.value().friendlyName))
            .collect(Collectors.joining(", "));
      }
      messages.add("- &7Flags&r: %s".formatted(flagsString));
    }
    context.sendMessage(messages);
  }

}
