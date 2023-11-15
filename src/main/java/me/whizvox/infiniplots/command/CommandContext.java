package me.whizvox.infiniplots.command;

import me.whizvox.infiniplots.exception.PlayerOnlyException;
import me.whizvox.infiniplots.util.ChatUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public record CommandContext(CommandSender sender, String label, List<String> args) {

  public boolean isValid() {
    return !label.isEmpty();
  }

  public Player getPlayerOrException() {
    if (sender instanceof Player player) {
      return player;
    }
    throw new PlayerOnlyException();
  }

  public void sendMessage(String message, Object... args) {
    sender.sendMessage(ChatUtils.altColorsf(message, args));
  }

  public void sendMessage(List<String> messages) {
    sender.sendMessage(messages.stream().map(ChatUtils::altColors).toArray(String[]::new));
  }

  public CommandContext copy() {
    return new CommandContext(sender, label, List.copyOf(args));
  }

  public CommandContext subContext(int beginAt) {
    if (beginAt < 0 || beginAt >= args.size()) {
      return invalid(sender);
    }
    return new CommandContext(sender, args.get(beginAt), args.subList(beginAt, args.size()));
  }

  public CommandContext subContext() {
    return subContext(0);
  }

  public static CommandContext invalid(CommandSender sender) {
    return new CommandContext(sender, "", List.of());
  }

}
