package me.whizvox.infiniplots.command.lib;

import me.whizvox.infiniplots.InfiniPlots;
import me.whizvox.infiniplots.command.ArgumentHelper;
import me.whizvox.infiniplots.command.CommandContext;
import me.whizvox.infiniplots.command.CommandHandler;
import me.whizvox.infiniplots.command.SuggestionHelper;
import me.whizvox.infiniplots.exception.InterruptCommandException;
import me.whizvox.infiniplots.util.WorldUtils;
import me.whizvox.infiniplots.worldgen.PlotWorldGenerator;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ImportCommandHandler extends CommandHandler {

  private static final List<String> MANUAL = List.of(
      "Imports a world as a plot world. This should ideally only be used if the world was already generated before, " +
      "but for some reason or another was deleted from the database.",
      "Examples:",
      "- &b/infiniplots import plots infiniplots:plains2&r : Imports a specific world using a generator",
      "See also:",
      "- &b/infiniplots list generators"
  );

  @Override
  public String getUsageArguments() {
    return "<world> <generator>";
  }

  @Override
  public List<String> getManual() {
    return MANUAL;
  }

  @Override
  public List<String> listSuggestions(CommandContext context) {
    if (context.args().size() == 2) {
      return SuggestionHelper.generators(context.arg(1));
    }
    return super.listSuggestions(context);
  }

  @Override
  public boolean hasPermission(CommandSender sender) {
    return sender.hasPermission("infiniplots.import");
  }

  @Override
  public void execute(CommandContext context) throws InterruptCommandException {
    PlotWorldGenerator generator = ArgumentHelper.getGenerator(context, 1);
    String worldName = ArgumentHelper.getString(context, 0);
    World world = Bukkit.getWorld(worldName);
    if (world == null) {
      if (WorldUtils.isWorldFolder(WorldUtils.getWorldFolder(worldName))) {
        world = generator.createWorld(worldName);
      } else {
        throw new InterruptCommandException("World " + worldName + " does not exist");
      }
    } else if (InfiniPlots.getInstance().getPlotManager().getPlotWorld(world.getUID()) != null) {
      throw new InterruptCommandException("World " + worldName + " is already a plot world");
    }
    InfiniPlots.getInstance().getPlotManager().importWorld(world, generator, true);
    context.sendMessage("&cWorld &a%s&c has been imported", worldName);
  }

}
