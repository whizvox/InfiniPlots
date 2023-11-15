package me.whizvox.infiniplots.exception;

public class MissingArgumentException extends InterruptCommandException {

  public MissingArgumentException(String message) {
    super(message);
  }

  public static <T> T fail() {
    throw new MissingArgumentException("Missing argument");
  }

}
