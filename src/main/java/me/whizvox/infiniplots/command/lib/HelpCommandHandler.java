package me.whizvox.infiniplots.command.lib;

import me.whizvox.infiniplots.command.ArgumentHelper;
import me.whizvox.infiniplots.command.CommandContext;
import me.whizvox.infiniplots.command.CommandDelegator;
import me.whizvox.infiniplots.command.CommandHandler;
import me.whizvox.infiniplots.exception.InterruptCommandException;
import me.whizvox.infiniplots.util.ChatUtils;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class HelpCommandHandler extends CommandHandler {

  private static final int PAGE_SIZE = 7;

  private final CommandDelegator delegator;

  public HelpCommandHandler(CommandDelegator delegator) {
    this.delegator = delegator;
  }

  @Override
  public String getUsageArguments() {
    return "[<page>]";
  }

  @Override
  public List<String> listSuggestions(CommandContext context) {
    if (context.args().size() == 1) {
      List<String> pages = new ArrayList<>();
      int totalPages = (int) Math.ceil((float) delegator.getHandlers().size() / PAGE_SIZE);
      for (int i = 1; i <= totalPages; i++) {
        String s = String.valueOf(i);
        if (s.startsWith(context.args().get(0))) {
          pages.add(String.valueOf(i));
        }
      }
      return pages;
    }
    return super.listSuggestions(context);
  }

  @Override
  public boolean hasPermission(CommandSender sender) {
    return sender.hasPermission("infiniplots");
  }

  @Override
  public void execute(CommandContext context) throws InterruptCommandException {
    int pageNumber = ArgumentHelper.getInt(context, 0, () -> 1, 1, 100);
    int totalCommands = delegator.getHandlers().size();
    int totalPages = (int) Math.ceil((float) totalCommands / PAGE_SIZE);
    List<String> commands = delegator.getHandlers().keySet().stream()
        .sorted()
        .skip((pageNumber - 1) * PAGE_SIZE)
        .limit(PAGE_SIZE)
        .toList();
    List<String> message = new ArrayList<>();
    message.add("&7===&r &6List of Commands&r (&b%d&7/&b%d&r) &7===".formatted(pageNumber, totalPages));
    commands.forEach(command -> {
      CommandHandler handler = delegator.getHandlers().get(command);
      String usage = ChatUtils.buildUsage(handler.getUsageArguments());
      message.add("- &b/infiniplots %s&r %s".formatted(command, usage));
    });
    if (context.args().isEmpty() && totalPages > 1) {
      message.add("Type &b/infiniplots help 2&r to see more commands".formatted(context.label()));
    }
    message.add("Type &b/infiniplots manual <command>&r to get detailed help on a command".formatted(context.label()));
    context.sendMessage(message);
  }

}
