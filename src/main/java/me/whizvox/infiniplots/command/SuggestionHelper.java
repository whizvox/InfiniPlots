package me.whizvox.infiniplots.command;

import me.whizvox.infiniplots.InfiniPlots;
import me.whizvox.infiniplots.flag.DefaultFlags;
import me.whizvox.infiniplots.flag.FlagValue;
import me.whizvox.infiniplots.util.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.generator.WorldInfo;

import java.util.*;
import java.util.stream.Stream;

public class SuggestionHelper {

  public static List<String> fromCollection(Collection<String> collection, String query) {
    return fromCollection(collection, query, true);
  }

  public static List<String> fromCollection(Collection<String> collection, String query, boolean sort) {
    return fromStream(collection.stream(), query, sort);
  }

  public static List<String> fromStream(Stream<String> stream, String query) {
    return fromStream(stream, query, true);
  }

  public static List<String> fromStream(Stream<String> stream, String query, boolean sort) {
    Stream<String> intermediate = stream
        .filter(str -> str.startsWith(query));
    if (sort) {
      return intermediate.sorted().toList();
    }
    return intermediate.toList();
  }

  public static List<String> onlinePlayerNames(String query) {
    return fromStream(Bukkit.getOnlinePlayers().stream().map(Player::getName), query);
  }

  public static List<String> offlinePlayerNames(String query) {
    return fromStream(Arrays.stream(Bukkit.getOfflinePlayers()).map(OfflinePlayer::getName), query);
  }

  public static List<String> worlds(String query) {
    return fromStream(Bukkit.getWorlds().stream().map(WorldInfo::getName), query, true);
  }

  public static List<String> plotWorlds(String query) {
    return fromStream(InfiniPlots.getInstance().getPlotManager().plotWorlds().map(pw -> pw.name), query);
  }

  public static List<String> plotOwners(String query) {
    List<UUID> allOwnerIds = new ArrayList<>();
    InfiniPlots.getInstance().getPlotManager().getPlotRepository().forEachOwner(allOwnerIds::add);
    return fromStream(allOwnerIds.stream().map(PlayerUtils::getOfflinePlayerName), query);
  }

  public static List<String> flags(String query) {
    return fromStream(DefaultFlags.ALL_FLAGS.keySet().stream(), query);
  }

  public static List<String> flagValues(String query) {
    return fromStream(FlagValue.VALUES_MAP.keySet().stream(), query);
  }

  public static List<String> generators(String query) {
    return fromStream(InfiniPlots.getInstance().getPlotGenRegistry().generators().map(Map.Entry::getKey), query);
  }

  public static List<String> ownerPlotNumbers(String query, UUID owner) {
    return fromStream(InfiniPlots.getInstance().getPlotManager().getPlotRepository().getOwnedPlots(owner).stream()
        .map(String::valueOf), query, false
    );
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
