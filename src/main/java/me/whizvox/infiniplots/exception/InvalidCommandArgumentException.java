package me.whizvox.infiniplots.exception;

public class InvalidCommandArgumentException extends InterruptCommandException {

  public InvalidCommandArgumentException(String message) {
    super("Invalid argument: " + message);
  }

}
