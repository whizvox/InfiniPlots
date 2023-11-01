package me.whizvox.infiniplots.util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class ChatUtils {

  public static void notPermitted(CommandSender sender) {
    sender.sendMessage(ChatColor.RED + "You do not have permission to run this command");
  }

  public static void onlyPlayer(CommandSender sender) {
    sender.sendMessage("Can only run this command as a player");
  }

  public static String altColors(String s) {
    return ChatColor.translateAlternateColorCodes('&', s);
  }

  public static String altColorsf(String format, Object... args) {
    return altColors(String.format(format, args));
  }

  public static String buildUsage(String args) {
    return "Usage: " + ChatColor.AQUA + "/infiniplots" + ChatColor.RESET + " " + args;
  }

}
