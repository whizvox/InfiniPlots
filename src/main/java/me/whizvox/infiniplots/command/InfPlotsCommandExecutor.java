package me.whizvox.infiniplots.command;

import me.whizvox.infiniplots.InfiniPlots;
import me.whizvox.infiniplots.plot.Plot;
import me.whizvox.infiniplots.plot.PlotId;
import me.whizvox.infiniplots.plot.PlotWorld;
import me.whizvox.infiniplots.util.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class InfPlotsCommandExecutor implements CommandExecutor {

  public static final String
      PERMISSION_TPPLOT = PermissionUtils.buildPermission("tp"),
      PERMISSION_TPWORLD = PermissionUtils.buildPermission("tpworld"),
      PERMISSION_CLAIM = PermissionUtils.buildPermission("claim"),
      PERMISSION_CLAIM_INF = PERMISSION_CLAIM + ".inf",
      PERMISSION_INFO = PermissionUtils.buildPermission("info"),
      PERMISSION_LIST_PLOTS_IN_WORLD = PermissionUtils.buildPermission("list.plots.world"),
      PERMISSION_LIST_PLOTS_BY_PLAYER = PermissionUtils.buildPermission("list.plots.player"),
      PERMISSION_LIST_WORLDS = PermissionUtils.buildPermission("list.worlds"),
      PERMISSION_LIST_GENERATORS = PermissionUtils.buildPermission("list.generators"),

      USAGE_TP = ChatUtils.buildUsage("tp <plot number> [<world>]"),
      USAGE_TP_OWNER = ChatUtils.buildUsage("tpo <owner> [<owner plot number>]"),
      USAGE_TPWORLD = ChatUtils.buildUsage("tpw <world>"),
      USAGE_CLAIM = ChatUtils.buildUsage("claim [<plot number> [<world>]]"),
      USAGE_CLAIMHERE = ChatUtils.buildUsage("claimhere"),
      USAGE_INFO = ChatUtils.buildUsage("info [<plot number> [<world>]]"),
      USAGE_WORLDINFO = ChatUtils.buildUsage("worldinfo [<world>]"),
      USAGE_LIST = ChatUtils.buildUsage("list [plots [<owner>] | worldplots [<world>] | worlds | generators]");

  private static final List<String> USAGES;

  static {
    List<String> usagesTemp = new ArrayList<>();
    Collections.addAll(usagesTemp, USAGE_TP, USAGE_TP_OWNER, USAGE_TPWORLD, USAGE_CLAIM, USAGE_CLAIMHERE, USAGE_INFO, USAGE_WORLDINFO, USAGE_LIST);
    usagesTemp.sort(Comparator.naturalOrder());
    USAGES = Collections.unmodifiableList(usagesTemp);
  }

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
    if (args.length == 0) {
      return false;
    }
    String[] remainingArgs = Arrays.copyOfRange(args, 1, args.length);
    switch (args[0]) {
      case "help", "?" -> help(sender, remainingArgs);
      case "tp" -> teleportToPlotWithPlotNumber(sender, remainingArgs);
      case "tpo" -> teleportToPlotWithOwner(sender, remainingArgs);
      case "tpw" -> teleportToWorld(sender, remainingArgs);
      case "claim" -> claim(sender, false, remainingArgs);
      case "claimhere" -> claim(sender, true, remainingArgs);
      case "info" -> getInfo(sender, remainingArgs);
      case "list" -> list(sender, remainingArgs);
      default -> {
        return false;
      }
    }
    return true;
  }

  private void help(CommandSender sender, String[] args) {
    if (args.length == 0) {
      sender.sendMessage(USAGES.toArray(String[]::new));
    } else {
      String arg = args[0];
      /*String msg = switch (arg) {
        case "tp" -> USAGE_TP;
        case "tpo" -> USAGE_TP_OWNER;
        case "tpw" -> USAGE_TPWORLD;
        case "claim" -> USAGE_CLAIM;
        case "claimhere" -> USAGE_CLAIMHERE;
        case "info" -> USAGE_INFO;
        case "worldinfo" -> USAGE_WORLDINFO;
        case "list" -> USAGE_LIST;
      };*/
    }
  }

  private void teleportToWorld(CommandSender sender, String[] args) {
    if (!(sender instanceof Player player)) {
      ChatUtils.onlyPlayer(sender);
      return;
    }
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

  private void teleportToPlotWithPlotNumber(CommandSender sender, String[] args) {
    if (!(sender instanceof Player player)) {
      ChatUtils.onlyPlayer(sender);
      return;
    }
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

  private void teleportToPlotWithOwner(CommandSender sender, String[] args) {
    if (!(sender instanceof Player player)) {
      ChatUtils.onlyPlayer(sender);
      return;
    }
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

  private void claim(CommandSender sender, boolean here, String[] args) {
    if (!(sender instanceof Player player)) {
      ChatUtils.onlyPlayer(sender);
      return;
    }
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

  }

  private void list(CommandSender sender, String[] args) {
    String type;
    if (args.length == 0) {
      type = "plots";
    } else {
      type = args[0];

    }
    List<String> messages = new ArrayList<>();
    switch (type) {
      case "plots" -> {
        UUID ownerId;
        if (args.length > 1) {
          if (!sender.hasPermission(PERMISSION_LIST_PLOTS_BY_PLAYER)) {
            ChatUtils.notPermitted(sender);
            return;
          }
          OfflinePlayer owner = PlayerUtils.getOfflinePlayer(args[1]);
          if (owner == null) {
            sender.sendMessage(ChatUtils.altColors("&cThat player could not be found"));
            return;
          }
          ownerId = owner.getUniqueId();
        } else {
          if (!(sender instanceof Player player)) {
            ChatUtils.onlyPlayer(sender);
            return;
          }
          ownerId = player.getUniqueId();
        }
        List<Plot> plots = InfiniPlots.getInstance().getPlotManager().getPlots(ownerId, false);
        messages.add(ChatUtils.altColorsf("&7=== List of &b%s&r's Plots &7==="));
        if (plots.isEmpty()) {
          messages.add(ChatUtils.altColors("- &7No plots found"));
        } else {
          plots.forEach(plot -> {
            PlotWorld plotWorld = InfiniPlots.getInstance().getPlotManager().getPlotWorld(plot.world());
            String worldStr;
            if (plotWorld == null) {
              worldStr = ChatUtils.altColorsf("&o%s", "<unknown>");
            } else {
              worldStr = plotWorld.world.getName();
            }
            messages.add(ChatUtils.altColorsf("- &7World: &b%s&r, &7WID&r: &e%s&r, &7OID&r: &e%s", worldStr, plot.worldPlotId(), plot.ownerPlotId()));
          });
        }
      }
      case "worldplots" -> {
        if (!sender.hasPermission(PERMISSION_LIST_PLOTS_IN_WORLD)) {
          ChatUtils.notPermitted(sender);
          return;
        }
        PlotWorld plotWorld;
        if (args.length > 1) {
          String worldName = args[1];
          World world = Bukkit.getWorld(worldName);
          if (world == null) {
            ChatUtils.worldDoesNotExist(sender, worldName);
            return;
          }
          plotWorld = InfiniPlots.getInstance().getPlotManager().getPlotWorld(world.getUID());
          if (plotWorld == null) {
            ChatUtils.notPlotWorld(sender, worldName);
            return;
          }
        } else {
          if (!(sender instanceof Player player)) {
            ChatUtils.onlyPlayer(sender);
            return;
          }
          plotWorld = InfPlotUtils.getPlotWorldOrDefault(player.getWorld().getUID());
          if (plotWorld == null) {
            ChatUtils.defaultWorldNotSetup(sender);
            return;
          }
        }
        int page;
        if (args.length > 2) {
          String pageStr = args[2];
          try {
            page = Integer.parseInt(pageStr) - 1;
            if (page < 0) {
              throw new NumberFormatException();
            }
          } catch (NumberFormatException e) {
            sender.sendMessage(ChatUtils.altColorsf("&cInvalid page number: &b%s", pageStr));
            return;
          }
        } else {
          page = 0;
        }
        Page<Plot> plots = InfiniPlots.getInstance().getPlotManager().getPlotRepository().getByWorld(plotWorld.world.getUID(), page, false);
        messages.add(ChatUtils.altColorsf("&7=== Plots List for &b%s&r &7===", plotWorld.world.getName()));
        if (plots.items().isEmpty()) {
          messages.add(ChatUtils.altColors("- &7This world has no claimed plots"));
        } else {
          plots.items().forEach(plot -> {
            String ownerName = PlayerUtils.getOfflinePlayerName(plot.owner());
            messages.add(ChatUtils.altColorsf("- &7WID: &b%s&r, &7Owner: &e%s&r, &7OID: &a%s", plot.worldPlotId(), ownerName, plot.ownerPlotId()));
          });
        }
      }
      case "worlds" -> {
        if (!sender.hasPermission(PERMISSION_LIST_WORLDS)) {
          ChatUtils.notPermitted(sender);
          return;
        }
        messages.add(ChatUtils.altColors("&7===&r List of Plot Worlds &7==="));
        InfiniPlots.getInstance().getPlotManager().plotWorlds()
            .sorted(Comparator.comparing(o -> o.world.getName()))
            .map(pw -> ChatUtils.altColorsf("- &b%s%r (&a%s&r)", pw.world.getName(), pw.world.getUID()))
            .forEach(messages::add);
      }
      case "generators" -> {
        if (!sender.hasPermission(PERMISSION_LIST_GENERATORS)) {
          ChatUtils.notPermitted(sender);
          return;
        }
        messages.add(ChatUtils.altColors("&7===&r List of Plot World Generators &7==="));
        InfiniPlots.getInstance().getPlotGenRegistry().generators()
            .map(Map.Entry::getKey)
            .sorted()
            .forEach(genKey -> messages.add(ChatUtils.altColorsf("- &b%s", genKey)));
      }
      default -> messages.add(ChatUtils.altColorsf("&cUnknown type: &b%s", type));
    }
    sender.sendMessage(messages.toArray(String[]::new));
  }

}
