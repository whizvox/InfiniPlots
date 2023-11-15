package me.whizvox.infiniplots.command.lib;

import me.whizvox.infiniplots.command.ArgumentHelper;
import me.whizvox.infiniplots.command.CommandContext;
import me.whizvox.infiniplots.command.CommandDelegator;
import me.whizvox.infiniplots.command.CommandHandler;
import me.whizvox.infiniplots.exception.InterruptCommandException;
import me.whizvox.infiniplots.exception.MissingArgumentException;
import me.whizvox.infiniplots.exception.NotEnoughPermissionException;
import me.whizvox.infiniplots.util.ChatUtils;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ManualCommandHandler extends CommandHandler {

  private final CommandDelegator delegator;

  public ManualCommandHandler(CommandDelegator delegator) {
    this.delegator = delegator;
  }

  @Override
  public String getUsageArguments() {
    return "<command>";
  }

  @Override
  public List<String> getManual() {
    return List.of(
        "This is a tool to view detailed information about a command. A list of these commands can be found by running &b/infiniplots help&r.",
        "Examples:",
        "    &b/infiniplots manual claim&r - View the manual for the &eclaim&r command",
        "    &b/infiniplots manual list&r - View the manual for the &elist&r command"
    );
  }

  @Override
  public List<String> listSuggestions(CommandContext context) {
    if (context.args().size() == 1) {
      return delegator.getAliases().entrySet().stream()
          .filter(entry -> entry.getKey().startsWith(context.args().get(0)) && delegator.getHandlers().get(entry.getValue()).hasPermission(context.sender()))
          .map(Map.Entry::getValue)
          .toList();
    }
    return super.listSuggestions(context);
  }

  @Override
  public boolean hasPermission(CommandSender sender) {
    return sender.hasPermission("infiniplots.manual");
  }

  @Override
  public void execute(CommandContext context) throws InterruptCommandException {
    String label = ArgumentHelper.getInSet(context, 0, () -> {
      throw new MissingArgumentException("Must include a command");
    }, delegator.getAliases().keySet());
    String command = delegator.getAliases().get(label);
    CommandHandler handler = delegator.getHandler(label);
    if (!handler.hasPermission(context.sender())) {
      throw new InterruptCommandException("Unknown command: " + label);
    }
    List<String> manual = handler.getManual();
    String usage = ChatUtils.buildUsage(handler.getUsageArguments());
    List<String> aliases = delegator.getAliases().entrySet().stream()
        .filter(entry -> entry.getValue().equals(command) && !entry.getKey().equals(command))
        .map(Map.Entry::getKey)
        .sorted()
        .toList();
    String aliasesStr;
    if (aliases.isEmpty()) {
      aliasesStr = "&o<none>";
    } else {
      aliasesStr = aliases.stream().map("&b%s&r"::formatted).collect(Collectors.joining(", "));
    }

    List<String> message = new ArrayList<>();
    message.add("&7=== Manual for &b/infiniplots %s&r &7===".formatted(command));
    message.add("- &7Usage: &b/infiniplots %s&r %s".formatted(command, usage));
    message.add("- &7Aliases: &b" + aliasesStr);
    message.addAll(manual);
    context.sendMessage(message);
  }

}
