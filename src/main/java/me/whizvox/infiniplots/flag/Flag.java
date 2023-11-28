package me.whizvox.infiniplots.flag;

public record Flag(String name, FlagValue value) {

  public static Flag DENY = new Flag("_", FlagValue.DENY);

}
