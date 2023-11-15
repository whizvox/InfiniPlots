package me.whizvox.infiniplots.exception;

import org.bukkit.command.CommandSender;

public class NotEnoughPermissionException extends InterruptCommandException {

  public NotEnoughPermissionException(String message) {
    super(message);
  }

  public static <T> T fail(String permission) {
    throw new NotEnoughPermissionException("Do not have permission: " + permission);
  }

  public static void check(CommandSender sender, String permission) {
    if (!sender.hasPermission(permission)) {
      throw new NotEnoughPermissionException("Do not have permission: " + permission);
    }
  }

}
