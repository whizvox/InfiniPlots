package me.whizvox.infiniplots.command;

import me.whizvox.infiniplots.InfiniPlots;
import me.whizvox.infiniplots.plot.Plot;
import me.whizvox.infiniplots.plot.PlotWorld;
import me.whizvox.infiniplots.util.ChatUtils;
import me.whizvox.infiniplots.util.ChunkPos;
import me.whizvox.infiniplots.util.PermissionUtils;
import me.whizvox.infiniplots.util.PlayerUtils;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

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

      USAGE_TP = ChatUtils.buildUsage("tp <x> <z> [<world>]"),
      USAGE_TP_ID = ChatUtils.buildUsage("tpi <plotId>"),
      USAGE_TP_OWNER = ChatUtils.buildUsage("tpo <ownerName> [<localId>]"),
      USAGE_TPWORLD = ChatUtils.buildUsage("tpw <worldName>");

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!(sender instanceof Player player)) {
      sender.sendMessage(ChatUtils.altColors("&cCommand sender must be a player"));
      return true;
    }
    if (args.length == 0) {
      return false;
    }
    String[] remainingArgs = Arrays.copyOfRange(args, 1, args.length);
    switch (args[0]) {
      case "tp" -> teleportToPlotWithPosition(player, remainingArgs);
      case "tpi" -> teleportToPlotWithId(player, remainingArgs);
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

  private void teleportToPlot_do(Player player, World world, ChunkPos pos) {
    Location location = new Location(world, pos.x() * 16 + 7.5, 1, pos.z() * 16 - 3.5, 0, 0);
    player.sendMessage(ChatUtils.altColorsf("Teleporting to plot..."));
    player.teleport(location);
  }

  private void teleportToPlotWithPosition(Player player, String[] args) {
    if (args.length < 1) {
      player.sendMessage(USAGE_TP);
      return;
    }
    int x;
    int z;
    try {
      x = Integer.parseInt(args[0]);
    } catch (NumberFormatException e) {
      player.sendMessage(ChatUtils.altColorsf("&cInvalid X position: &b%s", args[0]));
      return;
    }
    try {
      z = Integer.parseInt(args[1]);
    } catch (NumberFormatException e) {
      player.sendMessage(ChatUtils.altColorsf("&cInvalid Z position: &b%s", args[1]));
      return;
    }
    if (x % 2 != 0 || z % 2 != 0) {
      player.sendMessage(ChatUtils.altColorsf("&cInvalid plot position: &b%d%c,&b%d", x, z));
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
    PlotWorld plotWorld = InfiniPlots.getInstance().getPlots().getPlotWorld(world.getUID());
    if (plotWorld == null) {
      player.sendMessage(ChatUtils.altColorsf("&cWorld &b%s&c is not a plot world", world.getName()));
    }
    teleportToPlot_do(player, world, new ChunkPos(x, z));
  }

  private void teleportToPlotWithId(Player player, String[] args) {
    if (!player.hasPermission(PERMISSION_TPPLOT)) {
      ChatUtils.notPermitted(player);
      return;
    }
    if (args.length == 0) {
      player.sendMessage(USAGE_TP_ID);
    }
    UUID plotId;
    try {
      plotId = UUID.fromString(args[0]);
    } catch (IllegalArgumentException ignored) {
      player.sendMessage(ChatUtils.altColors("&cPlot ID must be a UUID"));
      return;
    }
    Plot plot = InfiniPlots.getInstance().getPlots().getPlot(plotId, false);
    if (plot == null) {
      player.sendMessage(ChatUtils.altColors("&cNo plot found with that ID"));
      return;
    }
    World world = Bukkit.getWorld(plot.world());
    if (world == null) {
      player.sendMessage(ChatUtils.altColorsf("&cUnknown world ID: &b%s", plot.world()));
      return;
    }
    teleportToPlot_do(player, world, plot.pos());
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
    Plot plot = InfiniPlots.getInstance().getPlots().getPlot(ownerId, localId, false);
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
    teleportToPlot_do(player, world, plot.pos());
  }

  private void claim(Player player, boolean here, String[] args) {
    if (!player.hasPermission(PERMISSION_CLAIM)) {
      ChatUtils.notPermitted(player);
      return;
    }
    World world;
    PlotWorld plotWorld;
    if (args.length > 0 && !here) {
      world = Bukkit.getWorld(args[0]);
      if (world == null) {
        player.sendMessage(ChatUtils.altColorsf("&cNo world was found with name of &b%s", args[0]));
        return;
      }
    } else {
      world = player.getWorld();
    }
    plotWorld = InfiniPlots.getInstance().getPlots().getPlotWorld(world.getUID());
    if (plotWorld == null) {
      if (args.length == 0 && !here) {
        String defaultWorldName = InfiniPlots.getInstance().getConfig().getString("defaultPlotWorld");
        world = Bukkit.getWorld(defaultWorldName);
        if (world == null) {
          player.sendMessage(ChatUtils.altColorsf("&cDefault plot world &b%s&c does not exist. Please tell an admin about this!", defaultWorldName));
          return;
        }
        plotWorld = InfiniPlots.getInstance().getPlots().getPlotWorld(world.getUID());
        if (plotWorld == null) {
          player.sendMessage(ChatUtils.altColorsf("&cDefault plot world &b%s&c is not a plot world. Please tell an admin about this!", defaultWorldName));
          return;
        }
      } else {
        player.sendMessage(ChatUtils.altColorsf("&cWorld &b%s&c is not a plot world"));
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
    List<Plot> plots = InfiniPlots.getInstance().getPlots().getPlots(player.getUniqueId(), false);
    if (plots.size() >= max) {
      player.sendMessage(ChatUtils.altColorsf("&cPlot count limit reached. Can only own a max of &b%s&c plots", max));
      return;
    }
    int localId = 1;
    // finds the first available local ID for this plot
    for (Plot plot : plots) {
      if (plot.localId() == localId) {
        localId++;
      } else {
        break;
      }
    }
    ChunkPos pos;
    if (here) {
      Chunk chunk = player.getLocation().getChunk();
      pos = new ChunkPos(chunk.getX(), chunk.getZ());
      if (pos.x() % 2 != 0 || pos.z() % 2 != 0) {
        player.sendMessage(ChatUtils.altColors("&cYou are not in a plot"));
        return;
      }
      if (plotWorld.getAllPositions().containsKey(pos)) {
        player.sendMessage(ChatUtils.altColors("&cThis plot has already been claimed"));
        return;
      }
    } else {
      pos = plotWorld.getNextAvailableChunkPos();
    }
    // TODO Allow claiming of plot at argument-specified chunk position
    InfiniPlots.getInstance().getPlots().addPlot(player, localId, world.getUID(), pos);
    player.sendMessage(ChatUtils.altColorsf("Successfully claimed a plot in &b%s&r at (&e%d&r,&e%d&r). This plot's local ID is &a%d&r", world.getName(), pos.x(), pos.z(), localId));
    teleportToPlot_do(player, world, pos);
  }

  private void getInfo(CommandSender sender, String[] args) {
    if (!sender.hasPermission(PERMISSION_INFO)) {
      ChatUtils.notPermitted(sender);
      return;
    }
    World world;
    ChunkPos pos;
    Plot plot;
    if (args.length == 0) {
      if (!(sender instanceof Player player)) {
        ChatUtils.onlyPlayer(sender);
        return;
      }
      world = player.getWorld();
      PlotWorld plotWorld = InfiniPlots.getInstance().getPlots().getPlotWorld(world.getUID());
      if (plotWorld == null) {
        player.sendMessage(ChatUtils.altColors("&cNo plot found"));
        return;
      }
      Chunk chunk = player.getLocation().getChunk();
      pos = new ChunkPos(chunk.getX(), chunk.getZ());
      if (pos.x() % 2 != 0 || pos.z() % 2 != 0) {
        player.sendMessage(ChatUtils.altColors("&cNo plot found"));
        return;
      }
      UUID plotId = plotWorld.getAllPositions().get(pos);
      if (plotId != null) {
        plot = InfiniPlots.getInstance().getPlots().getPlot(plotId, true);
      } else {
        plot = null;
      }
    } else {
      try {
        UUID plotId = UUID.fromString(args[0]);
        plot = InfiniPlots.getInstance().getPlots().getPlot(plotId, true);
        if (plot == null) {
          throw new IllegalArgumentException();
        }
        world = Bukkit.getWorld(plot.world());
        pos = plot.pos();
      } catch (IllegalArgumentException e) {
        sender.sendMessage(ChatUtils.altColorsf("&cNo plot found with ID of &b%s", args[0]));
        return;
      }
    }
    String[] messages;
    if (plot == null) {
      messages = new String[2];
      messages[1] = ChatUtils.altColors("- &7&oThis plot is unclaimed");
    } else {
      messages = new String[5];
      messages[1] = ChatUtils.altColorsf("- &7ID&r: &b%s", plot.id());
      messages[2] = ChatUtils.altColorsf("- &7Owner&r: &b%s&r (&a%s&r)", PlayerUtils.getOfflinePlayerName(plot.owner()), plot.owner());
      messages[3] = ChatUtils.altColorsf("- &7Local ID&r: &b%d", plot.localId());
      String membersString;
      if (plot.members().isEmpty()) {
        membersString = "&b&o<none>&r";
      } else {
        membersString = plot.members().stream().map(memberId -> "&b" + PlayerUtils.getOfflinePlayerName(memberId) + "&r").collect(Collectors.joining(", "));
      }
      messages[4] = ChatUtils.altColorsf("- &7Members&r: %s", membersString);
    }
    messages[0] = ChatUtils.altColorsf("&7===&r Information for Plot in &b%s&r at &e%d&r,&e%d&r &7===", world.getName(), pos.x(), pos.z());
    sender.sendMessage(messages);
  }

}
