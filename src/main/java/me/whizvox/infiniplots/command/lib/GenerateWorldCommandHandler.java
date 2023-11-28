package me.whizvox.infiniplots.command.lib;

import me.whizvox.infiniplots.InfiniPlots;
import me.whizvox.infiniplots.command.ArgumentHelper;
import me.whizvox.infiniplots.command.CommandContext;
import me.whizvox.infiniplots.command.CommandHandler;
import me.whizvox.infiniplots.exception.InterruptCommandException;
import me.whizvox.infiniplots.util.InfPlotUtils;
import me.whizvox.infiniplots.worldgen.PlotWorldGenerator;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Map;

public class GenerateWorldCommandHandler extends CommandHandler {

  private static final List<String> MANUAL = List.of(
      "Generates a new plot world.",
      "Examples:",
      "- &b/infiniplots genworld plots&r : Generates a plot world with the default plot world generator",
      "- &b/infiniplots genworld plots plains4&r : Generates a plot world with the &eplains4&r generator",
      "See also:",
      "- &b/infiniplots manual tpworld",
      "- &b/infiniplots list generators"
  );

  @Override
  public String getUsageArguments() {
    return "<world name> [<generator>]";
  }

  @Override
  public List<String> getManual() {
    return MANUAL;
  }

  @Override
  public List<String> listSuggestions(CommandContext context) {
    if (context.args().size() == 2) {
      return InfiniPlots.getInstance().getPlotGenRegistry().generators()
          .filter(entry -> entry.getKey().startsWith(context.args().get(1)))
          .map(Map.Entry::getKey)
          .sorted()
          .toList();
    }
    return super.listSuggestions(context);
  }

  @Override
  public boolean hasPermission(CommandSender sender) {
    return sender.hasPermission("infiniplots.genworld");
  }

  @Override
  public void execute(CommandContext context) throws InterruptCommandException {
    String worldName = ArgumentHelper.getString(context, 0);
    String generator = ArgumentHelper.getString(context, 1,
        () -> InfiniPlots.getInstance().getConfig().getString(InfiniPlots.CFG_DEFAULT_PLOT_WORLD_GENERATOR));
    if (Bukkit.getWorld(worldName) != null) {
      throw new InterruptCommandException("World " + worldName + " already exists");
    }
    if (InfPlotUtils.isWorldFolder(InfPlotUtils.getWorldFolder(worldName))) {
      throw new InterruptCommandException("World folder " + worldName + " already exists");
    }
    PlotWorldGenerator gen = InfiniPlots.getInstance().getPlotGenRegistry().getGenerator(generator);
    if (gen == null) {
      throw new InterruptCommandException("Generator " + generator + " does not exist");
    }
    context.sendMessage("Creating world &b%s&r (this might take a few seconds)...", worldName);
    World world = gen.createWorld(worldName);
    InfiniPlots.getInstance().getPlotManager().importWorld(world, gen, true);
    context.sendMessage("&aWorld &b%s&a has been created!", worldName);
  }

}
