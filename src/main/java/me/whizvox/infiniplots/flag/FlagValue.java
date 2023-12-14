package me.whizvox.infiniplots.flag;

import java.util.Map;
import java.util.Objects;

public enum FlagValue {

  DENY,
  EDITORS_ONLY("editors"),
  ALLOW;

  public final String friendlyName;

  FlagValue(String friendlyName) {
    this.friendlyName = friendlyName;
  }

  FlagValue() {
    this.friendlyName = toString().toLowerCase();
  }

  public boolean isAllowed(boolean isEditor) {
    return isEditor ? (this == EDITORS_ONLY || this == ALLOW) : this == ALLOW;
  }

  public boolean isAllowed() {
    return this == ALLOW;
  }

  public static final Map<String, FlagValue> VALUES_MAP = Map.of(
      DENY.friendlyName, DENY,
      EDITORS_ONLY.friendlyName, EDITORS_ONLY,
      ALLOW.friendlyName, ALLOW
  );

  public static FlagValue from(int ordinal) {
    if (ordinal >= 0 && ordinal < values().length) {
      return values()[ordinal];
    }
    throw new IllegalArgumentException("Illegal FlagValue ordinal: " + ordinal);
  }

}
