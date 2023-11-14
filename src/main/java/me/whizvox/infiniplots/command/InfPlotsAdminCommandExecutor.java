package me.whizvox.infiniplots.command;

import me.whizvox.infiniplots.InfiniPlots;
import me.whizvox.infiniplots.plot.PlotWorld;
import me.whizvox.infiniplots.util.ChatUtils;
import me.whizvox.infiniplots.util.InfPlotUtils;
import me.whizvox.infiniplots.worldgen.PlotWorldGenerator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class InfPlotsAdminCommandExecutor implements CommandExecutor {

  private static final String
      GENWORLD_USAGE = "Usage: /infiniplotsadmin genworld <name> [<generator>]",
      IMPORT_USAGE = "Usage: /infiniplotsadmin import <name> <generator>";

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
    if (args.length == 0) {
      return false;
    }
    String[] remainingArgs = Arrays.copyOfRange(args, 1, args.length);
    switch (args[0]) {
      case "genworld" -> generateWorld(sender, remainingArgs);
      case "import" -> importWorld(sender, remainingArgs);
      case "listgen" -> listGenerators(sender);
      default -> {
        return false;
      }
    };
    return true;
  }

  private void generateWorld(CommandSender sender, String[] args) {
    if (args.length == 0) {
      sender.sendMessage(GENWORLD_USAGE);
      return;
    }
    String worldName = args[0];
    World world = Bukkit.getWorld(worldName);
    if (world != null) {
      sender.sendMessage("World with name " + ChatColor.AQUA + worldName + ChatColor.RESET + " already exists");
      return;
    }
    File worldFolder = InfPlotUtils.getWorldFolder(worldName);
    if (worldFolder.exists()) {
      if (InfPlotUtils.isWorldFolder(worldFolder)) {
        sender.sendMessage(ChatUtils.altColorsf("&cWorld &b%s&c does exist, but is not loaded. If this is a plot world, use the &e/infpa import&c command."));
      } else {
        sender.sendMessage(ChatUtils.altColorsf("&cThere already exists a folder with the name &b%s", worldName));
      }
      return;
    }
    String generatorKey;
    if (args.length > 1) {
      generatorKey = args[1];
    } else {
      generatorKey = InfiniPlots.getInstance().getConfig().getString("defaultPlotWorldGenerator");
    }
    PlotWorldGenerator generator = InfiniPlots.getInstance().getPlotGenRegistry().getGenerator(generatorKey);
    if (generator == null) {
      if (args.length > 1) {
        sender.sendMessage(ChatUtils.altColorsf("&cNo plot world generator found with key &b%s", generatorKey));
      } else {
        sender.sendMessage(ChatUtils.altColorsf("&cDefault plot world generator &b%s&c does not exist", generatorKey));
      }
      return;
    }
    world = generator.createWorld(worldName);
    InfiniPlots.getInstance().getPlotManager().importWorld(world, generator, true);
    sender.sendMessage(ChatUtils.altColorsf("World &b%s&r (&e%s&r) has been created", world.getName(), world.getUID()));
  }

  private void importWorld(CommandSender sender, String[] args) {
    if (args.length < 2) {
      sender.sendMessage(IMPORT_USAGE);
      return;
    }
    String worldName = args[0];
    World world = Bukkit.getWorld(worldName);
    if (world == null) {
      File worldFolder = InfPlotUtils.getWorldFolder(worldName);
      if (!worldFolder.exists() || !InfPlotUtils.isWorldFolder(worldFolder)) {
        sender.sendMessage(ChatUtils.altColorsf("&cCould not find world with name &b%s", worldName));
        return;
      }
      world = WorldCreator.name(worldName).createWorld();
    }
    //noinspection DataFlowIssue world is guaranteed to be defined
    PlotWorld plotWorld = InfiniPlots.getInstance().getPlotManager().getPlotWorld(world.getUID());
    if (plotWorld != null) {
      sender.sendMessage(ChatUtils.altColorsf("&cWorld &b%s&c is already imported", worldName));
      return;
    }
    String generatorKey = args[1];
    PlotWorldGenerator generator = InfiniPlots.getInstance().getPlotGenRegistry().getGenerator(generatorKey);
    if (generator == null) {
      sender.sendMessage(ChatUtils.altColorsf("&cNo generator with key &b%s&c has been registered", generatorKey));
      return;
    }
    InfiniPlots.getInstance().getPlotManager().importWorld(world, generator, true);
    sender.sendMessage(ChatUtils.altColorsf("World &b%s&r has been imported", world.getName()));
  }

  private void listGenerators(CommandSender sender) {
    List<Map.Entry<String, PlotWorldGenerator>> generators = new ArrayList<>();
    InfiniPlots.getInstance().getPlotGenRegistry().forEach((key, generator) -> generators.add(Map.entry(key, generator)));
    generators.sort(Map.Entry.comparingByKey());
    if (generators.isEmpty()) {
      sender.sendMessage("No generators found!");
      return;
    }
    String[] messages = new String[generators.size() + 1];
    messages[0] = ChatUtils.altColorsf("&7===&r List of Plot World Generators &7===");
    for (int i = 0; i < generators.size(); i++) {
      Map.Entry<String, PlotWorldGenerator> entry = generators.get(i);
      messages[i + 1] = ChatUtils.altColorsf("- &b%s&r : &7Plots&r: &e%d&r,&e%d&r, &7Padding&r: &a%d&r,&a%d", entry.getKey(), entry.getValue().plotWidth, entry.getValue().plotDepth, entry.getValue().xPadding, entry.getValue().zPadding);
    }
    sender.sendMessage(messages);
  }

}
