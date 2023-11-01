package me.whizvox.infiniplots.util;

import org.bukkit.Bukkit;

import java.util.Objects;
import java.util.UUID;

public class PlayerUtils {

  public static String getOfflinePlayerName(UUID id) {
    return Objects.requireNonNullElse(Bukkit.getOfflinePlayer(id).getName(), "<unknown>");
  }

}
