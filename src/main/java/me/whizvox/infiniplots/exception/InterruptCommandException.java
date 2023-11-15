package me.whizvox.infiniplots.exception;

public class InterruptCommandException extends RuntimeException {

  public InterruptCommandException() {
  }

  public InterruptCommandException(String message) {
    super(message);
  }

  public InterruptCommandException(String message, Throwable cause) {
    super(message, cause);
  }

  public InterruptCommandException(Throwable cause) {
    super(cause);
  }

  public InterruptCommandException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
