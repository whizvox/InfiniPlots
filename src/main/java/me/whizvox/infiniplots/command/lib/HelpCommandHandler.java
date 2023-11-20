package me.whizvox.infiniplots.command.lib;

import me.whizvox.infiniplots.command.CommandContext;
import me.whizvox.infiniplots.command.CommandHandler;
import me.whizvox.infiniplots.exception.InterruptCommandException;
import org.bukkit.command.CommandSender;

import java.util.List;

public class HelpCommandHandler extends CommandHandler {

  private static final List<String> MANUAL = List.of(
      "Displays starting help information on how to use this plugin."
  );

  private static final List<String> MESSAGE = List.of(
      "&7=== &6Help Information for &lInfiniPlots &r&7===",
      "- &7For a list of commands, run &b/infiniplots commands",
      "- &7For more detailed information about a command, run &b/infiniplots manual <command>",
      "- &7For a list of permissions, run &b/infiniplots permissions",
      "- &7Visit the wiki to view online documentation: &eWIKI_LINK_HERE",
      "- &7Join the Discord for support or to report an issue: &eDISCORD_LINK_HERE"
  );

  @Override
  public List<String> getManual() {
    return MANUAL;
  }

  @Override
  public boolean hasPermission(CommandSender sender) {
    return sender.hasPermission("infiniplots.help");
  }

  @Override
  public void execute(CommandContext context) throws InterruptCommandException {
    // TODO Make help message configurable
    context.sendMessage(MESSAGE);
  }

}
