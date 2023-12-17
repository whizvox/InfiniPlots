package me.whizvox.infiniplots.command.lib;

import me.whizvox.infiniplots.command.ArgumentHelper;
import me.whizvox.infiniplots.command.CommandContext;
import me.whizvox.infiniplots.command.CommandHandler;
import me.whizvox.infiniplots.command.SuggestionHelper;
import me.whizvox.infiniplots.exception.InterruptCommandException;
import me.whizvox.infiniplots.exception.NotEnoughPermissionException;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.List;

public class TeleportToWorldCommandHandler extends CommandHandler {

  private static final List<String> MANUAL = List.of(
      "Teleports to the spawn location of a world. This doesn't necessarily have to be a plot world.",
      "Example:",
      "- &b/infiniplots tpworld plots&r : Teleport to the plots world"
  );

  @Override
  public String getUsageArguments() {
    return "<world>";
  }

  @Override
  public List<String> getManual() {
    return MANUAL;
  }

  @Override
  public List<String> listSuggestions(CommandContext context) {
    if (context.args().size() == 1) {
      return SuggestionHelper.worlds(context.arg(0));
    }
    return super.listSuggestions(context);
  }

  @Override
  public boolean hasPermission(CommandSender sender) {
    return sender.hasPermission("infiniplots.tpworld");
  }

  @Override
  public void execute(CommandContext context) throws InterruptCommandException {
    Player player = context.getPlayerOrException();
    World world = ArgumentHelper.getWorld(context, 0);
    NotEnoughPermissionException.check(player, "infiniplots.tpworld." + world.getName());
    player.teleport(world.getSpawnLocation(), PlayerTeleportEvent.TeleportCause.COMMAND);
    context.sendMessage("Teleporting to &b%s&r...", world.getName());
  }

}
