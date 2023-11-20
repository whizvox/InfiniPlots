package me.whizvox.infiniplots.command.lib;

import me.whizvox.infiniplots.InfiniPlots;
import me.whizvox.infiniplots.command.CommandContext;
import me.whizvox.infiniplots.command.CommandHandler;
import me.whizvox.infiniplots.exception.InterruptCommandException;
import org.bukkit.command.CommandSender;

import java.util.List;

public class DenyCommandHandler extends CommandHandler {

  private static final List<String> MANUAL = List.of(
      "If prompted to confirm a potentially dangerous and irreversible action, this will deny that.",
      "See also:",
      "- &b/infiniplots confirm"
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
    if (InfiniPlots.getInstance().getConfirmationManager().deny(context.sender())) {
      context.sendMessage("&aConfirmation denied");
    } else {
      context.sendMessage("&cThere is nothing to deny");
    }
  }

}
