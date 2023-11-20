package me.whizvox.infiniplots;

import me.whizvox.infiniplots.util.ChatUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.Predicate;

public class ConfirmationManager {

  private static final int TIMEOUT = 1000 * 60;
  private static final UUID NON_PLAYER_ID = UUID.fromString("802267d1-acec-4ce3-a947-a360c209638e");

  private final Map<UUID, Confirmation> confirmations;

  public ConfirmationManager() {
    confirmations = new HashMap<>();
  }

  private void removeIf(Predicate<Confirmation> condition) {
    List<UUID> toRemove = new ArrayList<>();
    confirmations.forEach((playerId, confirmation) -> {
      if (condition.test(confirmation)) {
        toRemove.add(playerId);
      }
    });
    toRemove.forEach(confirmations::remove);
  }

  private UUID getSenderId(CommandSender sender) {
    return sender instanceof Player player ? player.getUniqueId() : NON_PLAYER_ID;
  }

  public boolean contains(UUID playerId) {
    return confirmations.containsKey(playerId);
  }

  public boolean add(CommandSender sender, Runnable onConfirm) {
    UUID senderId = getSenderId(sender);
    if (contains(senderId)) {
      return false;
    }
    confirmations.put(senderId, new Confirmation(System.currentTimeMillis() + TIMEOUT, sender, onConfirm));
    sender.sendMessage(ChatUtils.altColors("&eYou are trying to perform an destructive and irreversible action. Run &b/infiniplots confirm&e to confirm this action."));
    return true;
  }

  public boolean deny(CommandSender sender) {
    return confirmations.remove(getSenderId(sender)) != null;
  }

  public void removeAllExpired() {
    long now = System.currentTimeMillis();
    removeIf(confirmation -> confirmation.whenExpires < now);
  }

  public void removeAll() {
    confirmations.clear();
  }

  public boolean confirm(CommandSender sender) {
    UUID senderId = getSenderId(sender);
    Confirmation confirmation = confirmations.remove(senderId);
    if (confirmation == null || System.currentTimeMillis() > confirmation.whenExpires) {
      return false;
    }
    confirmation.onConfirm.run();
    return true;
  }

  public record Confirmation(long whenExpires, CommandSender sender, Runnable onConfirm) {}

}
