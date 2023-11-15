package me.whizvox.infiniplots.command.lib;

import me.whizvox.infiniplots.command.CommandContext;
import me.whizvox.infiniplots.command.CommandHandler;
import me.whizvox.infiniplots.exception.InterruptCommandException;
import org.bukkit.command.CommandSender;

import java.util.List;

public class GenerateWorldCommandHandler extends CommandHandler {

  private static final List<String> MANUAL = List.of(
      "Generates a new plot world.",
      "Examples:",
      "- &b/infiniplots genworld plots&r : Generates a plot world with the default plot world generator",
      "- &b/infiniplots genworld plots plains4&r : Generates a plot world with the &eplains4&r generator",
      "See also:",
      "- &b/infiniplots manual tpworld",
      "- &b/infiniplots list generators"
  );

  @Override
  public String getUsageArguments() {
    return "<world name> [<generator>]";
  }

  @Override
  public List<String> getManual() {
    return MANUAL;
  }

  @Override
  public boolean hasPermission(CommandSender sender) {
    return sender.hasPermission("infiniplots.genworld");
  }

  @Override
  public void execute(CommandContext context) throws InterruptCommandException {
    context.sendMessage("&7Work-in-progress!");
  }

}
