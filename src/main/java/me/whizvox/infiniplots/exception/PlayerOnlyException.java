package me.whizvox.infiniplots.exception;

public class PlayerOnlyException extends InterruptCommandException {

  public PlayerOnlyException() {
    super("Only players can use this command");
  }

}
