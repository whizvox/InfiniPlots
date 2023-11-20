package me.whizvox.infiniplots.command;

import me.whizvox.infiniplots.InfiniPlots;
import me.whizvox.infiniplots.util.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SuggestionHelper {

  public static List<String> onlinePlayerNames(String query) {
    return Bukkit.getOnlinePlayers().stream()
        .map(Player::getName)
        .filter(name -> name.startsWith(query))
        .sorted()
        .toList();
  }

  public static List<String> plotWorlds(String query) {
    return InfiniPlots.getInstance().getPlotManager().plotWorlds()
        .map(pw -> pw.world.getName())
        .filter(worldName -> worldName.startsWith(query))
        .sorted()
        .toList();
  }

  public static List<String> plotOwners(String query) {
    List<UUID> allOwnerIds = new ArrayList<>();
    InfiniPlots.getInstance().getPlotManager().getPlotRepository().forEachOwner(allOwnerIds::add);
    return allOwnerIds.stream()
        .map(PlayerUtils::getOfflinePlayerName)
        .filter(playerName -> playerName.startsWith(query))
        .sorted()
        .toList();
  }

  public static List<String> pages(String query, int totalPages) {
    List<String> pages = new ArrayList<>();
    for (int i = 1; i <= totalPages; i++) {
      String page = String.valueOf(i);
      if (page.startsWith(query)) {
        pages.add(page);
      }
    }
    return pages;
  }

  public static List<String> pages(String query, int totalItems, int pageSize) {
    return pages(query, (int) Math.ceil((float) totalItems / pageSize));
  }

}
