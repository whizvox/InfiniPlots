package me.whizvox.infiniplots.command;

import me.whizvox.infiniplots.InfiniPlots;
import me.whizvox.infiniplots.worldgen.PlotWorldChunkGenerator;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;

import java.util.Arrays;
import java.util.List;

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
        .biomeProvider(new BiomeProvider() {
          @Override
          public Biome getBiome(WorldInfo worldInfo, int x, int y, int z) {
            return Biome.PLAINS;
          }
          @Override
          public List<Biome> getBiomes(WorldInfo worldInfo) {
            return List.of(Biome.PLAINS);
          }
        })
        .generateStructures(false)
        .createWorld();
    world.setSpawnLocation(-8, -8, 0);
    world.setSpawnFlags(false, false);
    world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
    world.setGameRule(GameRule.DISABLE_RAIDS, false);
    world.setGameRule(GameRule.DO_INSOMNIA, false);
    world.setGameRule(GameRule.DO_FIRE_TICK, false);
    world.setGameRule(GameRule.DO_MOB_LOOT, false);
    world.setGameRule(GameRule.DO_TRADER_SPAWNING, false);
    world.setGameRule(GameRule.DO_VINES_SPREAD, false);
    world.setGameRule(GameRule.DO_WARDEN_SPAWNING, false);
    world.setGameRule(GameRule.FALL_DAMAGE, false);
    world.setGameRule(GameRule.MOB_GRIEFING, false);
    world.setGameRule(GameRule.SHOW_DEATH_MESSAGES, false);
    world.setTime(6000);
    InfiniPlots.getInstance().getPlots().importWorld(world);
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
