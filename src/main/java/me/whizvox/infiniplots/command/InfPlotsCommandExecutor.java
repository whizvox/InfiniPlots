package me.whizvox.infiniplots.command;

import me.whizvox.infiniplots.InfiniPlots;
import me.whizvox.infiniplots.plot.Plot;
import me.whizvox.infiniplots.plot.PlotId;
import me.whizvox.infiniplots.plot.PlotWorld;
import me.whizvox.infiniplots.util.*;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class InfPlotsCommandExecutor implements CommandExecutor {

  public static final String
      PERMISSION_TPPLOT = PermissionUtils.buildPermission("tp"),
      PERMISSION_TPWORLD = PermissionUtils.buildPermission("tpworld"),
      PERMISSION_CLAIM = PermissionUtils.buildPermission("claim"),
      PERMISSION_CLAIM_INF = PERMISSION_CLAIM + ".inf",
      PERMISSION_INFO = PermissionUtils.buildPermission("info"),

      USAGE_TP = ChatUtils.buildUsage("tp <plot number> [<world>]"),
      USAGE_TP_OWNER = ChatUtils.buildUsage("tpo <owner> [<owner plot number>]"),
      USAGE_TPWORLD = ChatUtils.buildUsage("tpw <world>"),
      USAGE_CLAIM = ChatUtils.buildUsage("claim [<plot number> [<world>]]"),
      USAGE_INFO = ChatUtils.buildUsage("info [<plot number> [<world>]]");

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
    if (!(sender instanceof Player player)) {
      sender.sendMessage(ChatUtils.altColors("&cCommand sender must be a player"));
      return true;
    }
    if (args.length == 0) {
      return false;
    }
    String[] remainingArgs = Arrays.copyOfRange(args, 1, args.length);
    switch (args[0]) {
      case "tp" -> teleportToPlotWithPlotNumber(player, remainingArgs);
      case "tpo" -> teleportToPlotWithOwner(player, remainingArgs);
      case "tpw" -> teleportToWorld(player, remainingArgs);
      case "claim" -> claim(player, false, remainingArgs);
      case "claimhere" -> claim(player, true, remainingArgs);
      case "info" -> getInfo(player, remainingArgs);
      default -> {
        return false;
      }
    }
    return true;
  }

  private void teleportToWorld(Player player, String[] args) {
    if (!player.hasPermission(PERMISSION_TPWORLD)) {
      ChatUtils.notPermitted(player);
      return;
    }
    if (args.length == 0) {
      player.sendMessage(USAGE_TPWORLD);
      return;
    }
    String worldName = args[0];
    World world = Bukkit.getWorld(worldName);
    if (world == null) {
      player.sendMessage(ChatUtils.altColorsf("World &b%s&r does not exist", worldName));
      return;
    }
    player.sendMessage(ChatUtils.altColorsf("Teleporting to &b%s&r...", worldName));
    player.teleport(world.getSpawnLocation(), PlayerTeleportEvent.TeleportCause.COMMAND);
  }

  private void teleportToPlotWithPlotNumber(Player player, String[] args) {
    if (args.length < 1) {
      player.sendMessage(USAGE_TP);
      return;
    }
    int plotNumber;
    try {
      plotNumber = Integer.parseInt(args[0]);
      if (plotNumber < 1) {
        throw new NumberFormatException();
      }
    } catch (NumberFormatException e) {
      player.sendMessage(ChatUtils.altColorsf("&cInvalid plot number: &b%s", args[0]));
      return;
    }
    World world;
    if (args.length > 2) {
      world = Bukkit.getWorld(args[2]);
      if (world == null) {
        player.sendMessage(ChatUtils.altColorsf("&cUnknown world name: &b%s", args[2]));
        return;
      }
    } else {
      world = player.getWorld();
    }
    PlotWorld plotWorld = InfiniPlots.getInstance().getPlotManager().getPlotWorld(world.getUID());
    if (plotWorld == null) {
      plotWorld = InfiniPlots.getInstance().getPlotManager().getDefaultWorld();
      if (plotWorld == null) {
        player.sendMessage(ChatUtils.altColorsf("&cDefault plot world is not set up. Please report this to an admin!", world.getName()));
        return;
      }
    }
    Location location = plotWorld.generator.getTeleportLocation(world, plotNumber);
    if (location == null) {
      player.sendMessage(ChatUtils.altColorsf("&cCould not teleport to plot"));
      return;
    }
    player.teleport(location, PlayerTeleportEvent.TeleportCause.COMMAND);
  }

  private void teleportToPlotWithOwner(Player player, String[] args) {
    if (!player.hasPermission(PERMISSION_TPPLOT)) {
      ChatUtils.notPermitted(player);
      return;
    }
    if (args.length == 0) {
      player.sendMessage(USAGE_TP_OWNER);
      return;
    }
    UUID ownerId = null;
    String ownerName = args[0];
    // doing this instead of Bukkit.getOfflinePlayer(args[0]) because that method apparently creates a blocking web
    // request
    for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
      if (ownerName.equalsIgnoreCase(offlinePlayer.getName())) {
        ownerId = offlinePlayer.getUniqueId();
        ownerName = offlinePlayer.getName();
        break;
      }
    }
    if (ownerId == null) {
      player.sendMessage(ChatUtils.altColorsf("&cNo players with the username &b%s&c have joined this server", ownerName));
      return;
    }
    int localId;
    if (args.length > 1) {
      try {
        localId = Integer.parseInt(args[1]);
      } catch (NumberFormatException e) {
        player.sendMessage(ChatUtils.altColorsf("&cInvalid local ID value: &b%s", args[1]));
        return;
      }
    } else {
      localId = 1;
    }
    Plot plot = InfiniPlots.getInstance().getPlotManager().getPlot(ownerId, localId, false);
    if (plot == null) {
      if (args.length > 1) {
        player.sendMessage(ChatUtils.altColorsf("&cNo plot found with owner &b%s&c and local ID &b%s", ownerName, localId));
      } else {
        player.sendMessage(ChatUtils.altColorsf("&cNo plot found with owner &b%s&c", ownerName));
      }
      return;
    }
    World world = Bukkit.getWorld(plot.world());
    if (world == null) {
      player.sendMessage(ChatUtils.altColorsf("&cNo world found with ID &b%s", plot.world()));
      return;
    }
    PlotWorld plotWorld = InfiniPlots.getInstance().getPlotManager().getPlotWorld(world.getUID());
    if (plotWorld == null) {
      player.sendMessage(ChatUtils.altColors("&cInvalid world ID. Please report this to an admin!"));
      return;
    }
    Location location = plotWorld.generator.getTeleportLocation(world, plot.worldPlotId());
    if (location == null) {
      player.sendMessage(ChatUtils.altColors("&cCould not teleport to plot. Please report this to an admin!"));
      return;
    }
    player.teleport(location, PlayerTeleportEvent.TeleportCause.COMMAND);
  }

  private void claim(Player player, boolean here, String[] args) {
    if (!player.hasPermission(PERMISSION_CLAIM)) {
      ChatUtils.notPermitted(player);
      return;
    }
    World world;
    if (args.length > 1 && !here) {
      world = Bukkit.getWorld(args[1]);
      if (world == null) {
        player.sendMessage(ChatUtils.altColorsf("&cNo world was found with name of &b%s", args[0]));
        return;
      }
    } else {
      world = player.getWorld();
    }
    PlotWorld plotWorld = InfiniPlots.getInstance().getPlotManager().getPlotWorld(world.getUID());
    if (plotWorld == null) {
      if (args.length == 0 && !here) {
        plotWorld = InfiniPlots.getInstance().getPlotManager().getDefaultWorld();
        if (plotWorld == null) {
          player.sendMessage(ChatUtils.altColorsf("&cDefault plot world is not set up. Please tell an admin about this!"));
          return;
        }
      } else {
        player.sendMessage(ChatUtils.altColorsf("&cWorld &b%s&c is not a plot world"));
        return;
      }
    }
    int plotNumber;
    if (args.length > 0) {
      String plotNumberStr = args[0];
      try {
        plotNumber = Integer.parseInt(plotNumberStr);
        if (plotNumber < 1) {
          throw new NumberFormatException();
        }
      } catch (NumberFormatException e) {
        player.sendMessage(ChatUtils.altColorsf("&cInvalid plot number: &b%s", plotNumberStr));
        return;
      }
    } else {
      plotNumber = plotWorld.calculateNextUnclaimedPlot();
      if (plotNumber < 1) {
        player.sendMessage(ChatUtils.altColors("&cMaximum number of plots has been claimed"));
        return;
      }
    }

    int max = 0;
    if (player.hasPermission(PERMISSION_CLAIM_INF)) {
      max = Integer.MAX_VALUE;
    } else {
      // it's possible for a player to have multiple permissions pertaining to multiple different tiers. while this is
      // extremely ill-advised, there's nothing preventing this from happening. so if this happens, just find the tier
      // with the max count and use that.
      for (var entry : InfiniPlots.getInstance().getOwnerTiers().entrySet()) {
        if (player.hasPermission(PERMISSION_CLAIM + "." + entry.getKey()) && entry.getValue() > max) {
          max = entry.getValue();
        }
      }
      if (max == 0) {
        player.sendMessage(ChatUtils.altColors("&cNot allowed to own any plots"));
        return;
      }
    }
    List<Plot> plots = InfiniPlots.getInstance().getPlotManager().getPlots(player.getUniqueId(), false);
    if (plots.size() >= max) {
      player.sendMessage(ChatUtils.altColorsf("&cPlot count limit reached. Can only own a max of &b%s&c plots", max));
      return;
    }
    int ownerPlotId = 1;
    // finds the first available local ID for this plot
    for (Plot plot : plots) {
      if (plot.ownerPlotId() == ownerPlotId) {
        ownerPlotId++;
      } else {
        break;
      }
    }
    Location location = plotWorld.generator.getTeleportLocation(world, plotNumber);
    if (location == null) {
      player.sendMessage(ChatUtils.altColors("&cCould not determine plot location"));
      return;
    }
    InfiniPlots.getInstance().getPlotManager().addPlot(world.getUID(), plotNumber, player.getUniqueId(), ownerPlotId);
    plotWorld.nextPlotNumber = plotWorld.calculateNextUnclaimedPlot();
    player.sendMessage(ChatUtils.altColorsf("Successfully claimed plot #&b%s&r in &e%s&r (OID: &a%d&r)", ownerPlotId, world.getName(), ownerPlotId));
    player.teleport(location);
  }

  private void getInfo(CommandSender sender, String[] args) {
    if (!sender.hasPermission(PERMISSION_INFO)) {
      ChatUtils.notPermitted(sender);
      return;
    }
    World world;
    PlotWorld plotWorld;
    int worldPlotId;
    Plot plot;
    if (args.length == 0) {
      if (!(sender instanceof Player player)) {
        ChatUtils.onlyPlayer(sender);
        return;
      }
      world = player.getWorld();
      plotWorld = InfiniPlots.getInstance().getPlotManager().getPlotWorld(world.getUID());
      if (plotWorld == null) {
        player.sendMessage(ChatUtils.altColors("&cNo plot found"));
        return;
      }
      worldPlotId = plotWorld.generator.getPlotNumber(new ChunkPos(player.getLocation()));
      if (worldPlotId < 1) {
        player.sendMessage(ChatUtils.altColors("&cNo plot found"));
        return;
      }
    } else {
      if (args.length > 1) {
        String worldName = args[2];
        world = Bukkit.getWorld(worldName);
        if (world == null) {
          sender.sendMessage(ChatUtils.altColorsf("&cWorld %s not found", worldName));
          return;
        }
      } else {
        if (!(sender instanceof Player player)) {
          ChatUtils.onlyPlayer(sender);
          return;
        }
        world = player.getWorld();
      }
      plotWorld = InfPlotUtils.getPlotWorldOrDefault(world.getUID());
      if (plotWorld == null) {
        sender.sendMessage(ChatUtils.altColors("&cCould not get plot world"));
        return;
      }
      world = plotWorld.world;
      try {
        worldPlotId = Integer.parseInt(args[0]);
        if (worldPlotId < 1 || worldPlotId > plotWorld.generator.getMaxClaims()) {
          throw new NumberFormatException();
        }
      } catch (NumberFormatException e) {
        sender.sendMessage(ChatUtils.altColorsf("&cInvalid plot number: %s", args[0]));
        return;
      }
    }
    plot = InfiniPlots.getInstance().getPlotManager().getPlot(new PlotId(world.getUID(), worldPlotId), true);
    String[] messages;
    if (plot == null) {
      messages = new String[2];
      messages[1] = ChatUtils.altColors("- &7&oThis plot is unclaimed");
    } else {
      messages = new String[7];
      messages[1] = ChatUtils.altColorsf("- &7World: &b%s", world.getName());
      messages[2] = ChatUtils.altColorsf("- &7Plot Number&r: &b%s", plot.worldPlotId());
      messages[3] = ChatUtils.altColorsf("- &7Owner&r: &b%s&r (&a%s&r)", PlayerUtils.getOfflinePlayerName(plot.owner()), plot.owner());
      messages[4] = ChatUtils.altColorsf("- &7Owner ID&r: &b%d", plot.ownerPlotId());
      String membersString;
      if (plot.members().isEmpty()) {
        membersString = "&b&o<none>&r";
      } else {
        membersString = plot.members().stream().map(memberId -> "&b" + PlayerUtils.getOfflinePlayerName(memberId) + "&r").collect(Collectors.joining(", "));
      }
      messages[5] = ChatUtils.altColorsf("- &7Members&r: %s", membersString);
      String flagsString;
      if (plot.flags().isEmpty()) {
        flagsString = "&b&o<none>&r";
      } else {
        flagsString = plot.flags().stream().map(flag -> "&b" + flag + "&r").collect(Collectors.joining(", "));
      }
      messages[6] = ChatUtils.altColorsf("- &7Flags&r: %s", flagsString);
    }
    messages[0] = ChatUtils.altColorsf("&7===&r Information for Plot #&b%s&r in &b%s&r &7===", worldPlotId, world.getName());
    sender.sendMessage(messages);
  }

}
