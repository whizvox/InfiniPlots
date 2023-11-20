package me.whizvox.infiniplots.command.lib;

import me.whizvox.infiniplots.InfiniPlots;
import me.whizvox.infiniplots.command.CommandContext;
import me.whizvox.infiniplots.command.CommandHandler;
import me.whizvox.infiniplots.exception.InterruptCommandException;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ConfirmCommandHandler extends CommandHandler {

  private static final List<String> MANUAL = List.of(
      "Confirms a potentially dangerous and irreversible action. If not confirmed within 1 minute, the attempt will " +
      "expire.",
      "See also:",
      "- &b/infiniplots deny"
  );

  @Override
  public List<String> getManual() {
    return MANUAL;
  }

  @Override
  public boolean hasPermission(CommandSender sender) {
    return true;
  }

  @Override
  public void execute(CommandContext context) throws InterruptCommandException {
    if (!InfiniPlots.getInstance().getConfirmationManager().confirm(context.sender())) {
      context.sendMessage("&cThere is nothing to confirm");
    }
  }

}
