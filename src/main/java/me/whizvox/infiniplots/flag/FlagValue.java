package me.whizvox.infiniplots.flag;

import java.util.Map;

public enum FlagValue {

  DENY,
  EDITORS_ONLY,
  ALLOW;

  public boolean isAllowed(boolean isEditor) {
    return isEditor ? (this == EDITORS_ONLY || this == ALLOW) : this == ALLOW;
  }

  public boolean isAllowed() {
    return this == ALLOW;
  }

  public static final Map<String, FlagValue> VALUES_MAP = Map.of(
      "deny", DENY,
      "editors", EDITORS_ONLY,
      "allow", ALLOW
  );

  public static FlagValue from(int ordinal) {
    if (ordinal >= 0 && ordinal < values().length) {
      return values()[ordinal];
    }
    throw new IllegalArgumentException("Illegal FlagValue ordinal: " + ordinal);
  }

}
