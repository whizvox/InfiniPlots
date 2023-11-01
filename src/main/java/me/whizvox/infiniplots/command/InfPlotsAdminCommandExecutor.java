package me.whizvox.infiniplots.command;

import me.whizvox.infiniplots.InfiniPlots;
import me.whizvox.infiniplots.worldgen.PlotWorldChunkGenerator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

public class InfPlotsAdminCommandExecutor implements CommandExecutor {

  private static final String
      GENERAL_USAGE = "Usage: /infiniplotsadmin <genworld | import>",
      GENWORLD_USAGE = "Usage: /infiniplotsadmin genworld <name>",
      IMPORT_USAGE = "Usage: /infiniplotsadmin import <name>";

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (args.length == 0) {
      return false;
    }
    String[] remainingArgs = Arrays.copyOfRange(args, 1, args.length);
    switch (args[0]) {
      case "genworld" -> generateWorld(sender, remainingArgs);
      case "import" -> importWorld(sender, remainingArgs);
      default -> {
        return false;
      }
    };
    return true;
  }

  private static void printUsage(CommandSender sender) {
    sender.sendMessage(GENERAL_USAGE);
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
    world = new WorldCreator(worldName)
        .generator(new PlotWorldChunkGenerator())
        .createWorld();
    sender.sendMessage("World %s (%s) created".formatted(ChatColor.AQUA + worldName + ChatColor.RESET, ChatColor.YELLOW + world.getUID().toString() + ChatColor.RESET));
  }

  private void importWorld(CommandSender sender, String[] args) {
    if (args.length == 0) {
      sender.sendMessage(IMPORT_USAGE);
      return;
    }
    String worldName = args[0];
    World world = Bukkit.getWorld(worldName);
    if (world != null) {
      InfiniPlots.getInstance().getPlots().importWorld(world);
    } else {
      sender.sendMessage("Could not find world with name " + ChatColor.AQUA + worldName + ChatColor.RESET);
    }
  }

}
