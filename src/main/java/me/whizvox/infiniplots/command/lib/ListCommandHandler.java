package me.whizvox.infiniplots.command.lib;

import me.whizvox.infiniplots.InfiniPlots;
import me.whizvox.infiniplots.command.ArgumentHelper;
import me.whizvox.infiniplots.command.CommandContext;
import me.whizvox.infiniplots.command.CommandHandler;
import me.whizvox.infiniplots.command.SuggestionHelper;
import me.whizvox.infiniplots.exception.InterruptCommandException;
import me.whizvox.infiniplots.exception.NotEnoughPermissionException;
import me.whizvox.infiniplots.plot.Plot;
import me.whizvox.infiniplots.plot.PlotWorld;
import me.whizvox.infiniplots.util.Page;
import me.whizvox.infiniplots.util.PermissionUtils;
import me.whizvox.infiniplots.util.PlayerUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class ListCommandHandler extends CommandHandler {

  private static final Set<String> ENTRIES = Set.of("plots", "worldplots", "worlds", "generators");

  private static final String
      PERMISSION_BASE = PermissionUtils.buildPermission("list"),
      PERMISSION_LIST_PLOTS_BY_PLAYER = PermissionUtils.buildPermission("list.plots"),
      PERMISSION_LIST_PLOTS_IN_WORLD = PermissionUtils.buildPermission("list.worldplots"),
      PERMISSION_LIST_WORLDS = PermissionUtils.buildPermission("list.worlds"),
      PERMISSION_LIST_GENERATORS = PermissionUtils.buildPermission("list.generators");

  private static final List<String> MANUAL = List.of(
      "Display a list of a specified entry. If an entry is not specified, then a list of your plots is displayed.",
      "Some of the entries might not be viewable due to permission restrictions.",
      "Examples:",
      "- &b/infiniplots list plots&r : Display a list of all of your owned plots",
      "- &b/infiniplots list plots Gamer72&r : Display a list of plots owned by the target player",
      "- &b/infiniplots list worldplots&r : Display a list of the plots in your world or the default world",
      "- &b/infiniplots list worldplots 4&r : Display the 4th page of plots from your world or the default world",
      "- &b/infiniplots list worldplots 4 bigplots&r : Display the 4th page of plots from the bigplots world",
      "- &b/infiniplots list worlds&r : List all available plot worlds",
      "- &b/infiniplots list generators&r : List all available plot world generators"
  );

  @Override
  public String getUsageArguments() {
    return "plots [<player>] | worldplots [<world> [<page>]] | worlds | generators";
  }

  @Override
  public List<String> getManual() {
    return MANUAL;
  }

  @Override
  public List<String> listSuggestions(CommandContext context) {
    return switch (context.args().size()) {
      case 1 -> ENTRIES.stream()
          .filter(entry -> context.sender().hasPermission("infiniplots.list." + entry))
          .filter(entry -> entry.startsWith(context.args().get(0)))
          .toList();
      case 2 -> {
        String query = context.args().get(1);
        yield switch (context.args().get(0)) {
          case "plots" -> SuggestionHelper.plotOwners(query);
          case "worldplots" -> SuggestionHelper.plotWorlds(query);
          default -> List.of();
        };
      }
      default -> super.listSuggestions(context);
    };
  }

  @Override
  public boolean hasPermission(CommandSender sender) {
    return sender.hasPermission(PERMISSION_BASE);
  }

  @Override
  public void execute(CommandContext context) throws InterruptCommandException {
    CommandSender sender = context.sender();
    List<String> args = context.args();
    String type = ArgumentHelper.getInSet(context, 0, () -> "plots", ENTRIES);
    List<String> message = new ArrayList<>();
    switch (type) {
      case "plots" -> {
        if (context.args().size() > 1) {
          String playerName = args.get(1);
          boolean isSelf = sender instanceof Player player && playerName.equalsIgnoreCase(player.getName());
          if (!isSelf) {
            NotEnoughPermissionException.check(sender, PERMISSION_LIST_PLOTS_BY_PLAYER);
          }
        }
        OfflinePlayer owner = ArgumentHelper.getOfflinePlayer(context, 1, context::getPlayerOrException);
        List<Plot> plots = InfiniPlots.getInstance().getPlotManager().getPlots(owner.getUniqueId(), false);
        message.add("&7===&r List of &b%s&r's Plots &7===".formatted(owner.getName()));
        if (plots.isEmpty()) {
          message.add("- &7No plots found");
        } else {
          plots.forEach(plot -> {
            PlotWorld plotWorld = InfiniPlots.getInstance().getPlotManager().getPlotWorld(plot.world());
            String worldStr;
            if (plotWorld == null) {
              worldStr = "&o<unknown>";
            } else {
              worldStr = plotWorld.world.getName();
            }
            message.add("- &7World: &b%s&r, &7WNum&r: &e%s&r, &7ONum&r: &e%s".formatted(worldStr, plot.worldNumber(), plot.ownerNumber()));
          });
        }
      }
      case "worldplots" -> {
        NotEnoughPermissionException.check(sender, PERMISSION_LIST_PLOTS_IN_WORLD);
        PlotWorld plotWorld = ArgumentHelper.getPlotWorld(context, 1);
        int page = ArgumentHelper.getInt(context, 2, () -> 1, 1, Integer.MAX_VALUE);
        Page<Plot> plots = InfiniPlots.getInstance().getPlotManager().getPlotRepository().getByWorld(plotWorld.world.getUID(), page - 1, false);
        message.add("&7===&r Plots List for &b%s&r (&e%d&r,&e%d&r) &7===".formatted(plotWorld.world.getName(), page, plots.totalPages()));
        if (plots.items().isEmpty()) {
          message.add("- &7No plots found");
        } else {
          plots.items().forEach(plot -> {
            String ownerName = PlayerUtils.getOfflinePlayerName(plot.owner());
            message.add("- &7WNum: &b%s&r, &7Owner: &e%s&r, &7ONum: &a%s".formatted(plot.worldNumber(), ownerName, plot.ownerNumber()));
          });
        }
      }
      case "worlds" -> {
        NotEnoughPermissionException.check(sender, PERMISSION_LIST_WORLDS);
        message.add("&7===&r List of Plot Worlds &7===");
        InfiniPlots.getInstance().getPlotManager().plotWorlds()
            .sorted(Comparator.comparing(o -> o.world.getName()))
            .map(pw -> "- &b%s&r (&a%s&r)".formatted(pw.world.getName(), pw.world.getUID()))
            .forEach(message::add);
      }
      case "generators" -> {
        NotEnoughPermissionException.check(sender, PERMISSION_LIST_GENERATORS);
        message.add("&7===&r List of Plot World Generators &7===");
        InfiniPlots.getInstance().getPlotGenRegistry().generators()
            .map(Map.Entry::getKey)
            .sorted()
            .forEach(genKey -> message.add("- &b%s".formatted(genKey)));
      }
      default -> message.add("&cUnknown type: &b%s".formatted(type));
    }
    context.sendMessage(message);
  }

}
