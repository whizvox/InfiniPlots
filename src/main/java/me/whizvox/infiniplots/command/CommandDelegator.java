package me.whizvox.infiniplots.command;

import me.whizvox.infiniplots.exception.InterruptCommandException;
import me.whizvox.infiniplots.exception.MissingArgumentException;
import me.whizvox.infiniplots.util.ChatUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CommandDelegator implements CommandExecutor, TabCompleter {

  private final Map<String, String> aliases;
  private final Map<String, CommandHandler> handlers;

  public CommandDelegator() {
    aliases = new HashMap<>();
    handlers = new HashMap<>();
  }

  public Map<String, String> getAliases() {
    return Collections.unmodifiableMap(aliases);
  }

  public Map<String, CommandHandler> getHandlers() {
    return Collections.unmodifiableMap(handlers);
  }

  public CommandHandler getHandler(String label) {
    String commandName = aliases.get(label);
    if (commandName == null) {
      return CommandHandler.EMPTY;
    }
    return handlers.getOrDefault(commandName, CommandHandler.EMPTY);
  }

  public void register(String commandName, @Nullable List<String> aliases, CommandHandler handler) {
    if (aliases != null) {
      aliases.forEach(alias -> this.aliases.put(alias, commandName));
    }
    this.aliases.put(commandName, commandName);
    handlers.put(commandName, handler);
  }

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
    if (args.length == 0) {
      sender.sendMessage(ChatUtils.altColorsf("&cRun &r/%s help&c to get command information", label));
      return true;
    }
    String subCommand = args[0];
    CommandHandler handler = getHandler(subCommand);
    if (handler == CommandHandler.EMPTY) {
      sender.sendMessage(ChatUtils.altColorsf("&cRun &r/%s help&c to get command information", label));
      return true;
    }
    try {
      if (handler.hasPermission(sender)) {
        handler.execute(new CommandContext(sender, subCommand, Arrays.asList(args).subList(1, args.length)));
      } else {
        sender.sendMessage(ChatColor.RED + "Not allowed to do this");
      }
    } catch (MissingArgumentException e) {
      sender.sendMessage(ChatUtils.altColorsf("&cUsage: &b/infiniplots %s&r %s", subCommand, ChatUtils.buildUsage(handler.getUsageArguments())));
    } catch (InterruptCommandException e) {
      sender.sendMessage(ChatColor.RED + e.getMessage());
    }
    return true;
  }

  @Nullable
  @Override
  public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
    String subCommand = args[0];
    List<String> remainingArgs = Arrays.asList(args).subList(1, args.length);
    if (args.length == 1) {
      return aliases.entrySet().stream()
          .filter(entry -> entry.getKey().startsWith(subCommand) && handlers.get(entry.getValue()).hasPermission(sender))
          .map(Map.Entry::getKey)
          .toList();
    } else {
      return getHandler(subCommand).listSuggestions(new CommandContext(sender, subCommand, remainingArgs));
    }
  }

}
