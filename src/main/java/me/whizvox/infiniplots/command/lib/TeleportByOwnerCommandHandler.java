package me.whizvox.infiniplots.command.lib;

import me.whizvox.infiniplots.command.CommandContext;
import me.whizvox.infiniplots.command.CommandHandler;
import me.whizvox.infiniplots.exception.InterruptCommandException;
import org.bukkit.command.CommandSender;

import java.util.List;

public class TeleportByOwnerCommandHandler extends CommandHandler {

  private static final List<String> MANUAL = List.of(
      "Teleport to a plot by specifying the owner and, optionally, the owner-specific plot number.",
      "Examples:",
      "- &b/infiniplots tpowner Gamer72&r : Teleport to the first plot the target player owns",
      "- &b/infiniplots tpowner Gamer72 3&r : Teleport to the 3rd owned plot by the target player",
      "See also",
      "- &b/infiniplots manual tp",
      "- &b/infiniplots manual tpworld"
  );

  @Override
  public String getUsageArguments() {
    return "<owner> [<owner plot number>]";
  }

  @Override
  public List<String> getManual() {
    return MANUAL;
  }

  @Override
  public boolean hasPermission(CommandSender sender) {
    return sender.hasPermission("infiniplots.tp.owner");
  }

  @Override
  public void execute(CommandContext context) throws InterruptCommandException {
    context.sendMessage("&7Work-in-progress!");
  }

}
