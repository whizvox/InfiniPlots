package me.whizvox.infiniplots.util;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

public class PlayerUtils {

  public static String getOfflinePlayerName(UUID id) {
    return Objects.requireNonNullElse(Bukkit.getOfflinePlayer(id).getName(), "<unknown>");
  }

  @Nullable
  public static OfflinePlayer getOfflinePlayer(String name) {
    for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
      if (name.equalsIgnoreCase(player.getName())) {
        return player;
      }
    }
    return null;
  }

}
