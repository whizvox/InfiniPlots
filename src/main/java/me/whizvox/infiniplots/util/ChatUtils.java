package me.whizvox.infiniplots.util;

import me.whizvox.infiniplots.InfiniPlots;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ChatUtils {

  public static void notPermitted(CommandSender sender) {
    sender.sendMessage(ChatColor.RED + "You do not have permission to run this command");
  }

  public static void onlyPlayer(CommandSender sender) {
    sender.sendMessage("Can only run this command as a player");
  }

  public static void worldDoesNotExist(CommandSender sender, String worldName) {
    sender.sendMessage(altColorsf("&cWorld &b%s&c does not exist", worldName));
  }

  public static void notPlotWorld(CommandSender sender, String worldName) {
    sender.sendMessage(altColorsf("&cWorld &b%s&c is not a plot world", worldName));
  }

  public static void defaultWorldNotSetup(CommandSender sender) {
    sender.sendMessage(altColorsf("The default plot world &b%s&c does not exist", InfiniPlots.getInstance().getConfig().getString(InfiniPlots.CFG_DEFAULT_PLOT_WORLD)));
  }

  public static String altColors(String s) {
    return ChatColor.translateAlternateColorCodes('&', s);
  }

  public static String altColorsf(String format, Object... args) {
    return altColors(String.format(format, args));
  }

  public static String buildUsage(String args) {
    StringBuilder sb = new StringBuilder();
    for (char c : args.toCharArray()) {
      if (c == '<' || c == '>') {
        sb.append(ChatColor.LIGHT_PURPLE);
      } else if (c == '[' || c == ']') {
        sb.append(ChatColor.GREEN);
      } else if (c == '|') {
        sb.append(ChatColor.YELLOW);
      }
      sb.append(c);
      if (c == '>' || c == '[' || c == ']' || c == '|') {
        sb.append(ChatColor.RESET);
      }
    }
    return sb.toString();
  }

  public static void listPages(List<String> entries, int page, int pageSize, List<String> message) {
    int start = (page - 1) * pageSize;
    int end = start + pageSize;
    int totalPages = (int) Math.ceil((float) entries.size() / pageSize);
    message.add("&7=== &6InfiniPlots Permissions&r (&b%d&r/&b%d&r) &7===".formatted(page, totalPages));
    for (int i = start; i < end && i < entries.size(); i++) {
      message.add("- " + entries.get(i));
    }
  }

  public static String header(String title) {
    return "&7=== &6" + title + " &7===";
  }

}
