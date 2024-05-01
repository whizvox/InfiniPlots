package me.whizvox.infiniplots.command.lib;

import me.whizvox.infiniplots.InfiniPlots;
import me.whizvox.infiniplots.command.ArgumentHelper;
import me.whizvox.infiniplots.command.CommandContext;
import me.whizvox.infiniplots.command.CommandHandler;
import me.whizvox.infiniplots.exception.InterruptCommandException;
import me.whizvox.infiniplots.flag.Flag;
import me.whizvox.infiniplots.plot.PlotWorld;
import me.whizvox.infiniplots.plot.PlotWorldProperties;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class WorldInfoCommandHandler extends CommandHandler {

  private static final List<String> MANUAL = List.of(
      "Display information about a plot world.",
      "Examples:",
      "- &b/infiniplots worldinfo&r : Displays information about your world or the default world",
      "- &b/infiniplots worldinfo bigplots&r : Displays information about the bigplots world"
  );

  @Override
  public String getUsageArguments() {
    return "[<world>]";
  }

  @Override
  public List<String> getManual() {
    return MANUAL;
  }

  @Override
  public boolean hasPermission(CommandSender sender) {
    return sender.hasPermission("infiniplots.worldinfo");
  }

  @Override
  public void execute(CommandContext context) throws InterruptCommandException {
    PlotWorld plotWorld = ArgumentHelper.getPlotWorld(context, 0);
    PlotWorldProperties props = InfiniPlots.getInstance().getPlotManager().getWorldRepository().get(plotWorld.world.getUID(), true);
    if (props == null) {
      throw new InterruptCommandException("Could not get world properties for " + plotWorld.name);
    }
    List<String> message = new ArrayList<>();
    message.add("&7=== Information for &b%s&r &7===".formatted(props.name()));
    message.add("- &7UID&r: &b%s".formatted(plotWorld.world.getUID()));
    message.add("- &7Generator: &b%s".formatted(props.generator()));
    message.add("- &7Lockdown: &b%s".formatted(props.lockdown()));
    String flagsStr;
    if (props.flags().isEmpty()) {
      flagsStr = "&b&o<none>";
    } else {
      flagsStr = StreamSupport.stream(props.flags().spliterator(), false)
          .sorted(Comparator.comparing(Flag::name))
          .map(flag -> "&b%s&r:&e%s&r".formatted(flag.name(), flag.value().friendlyName))
          .collect(Collectors.joining(", "));
    }
    message.add("- &7Flags: %s".formatted(flagsStr));
    context.sendMessage(message);
  }

}
