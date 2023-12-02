package me.whizvox.infiniplots.flag;

import java.util.Map;
import java.util.Objects;

public enum FlagValue {

  DENY,
  EDITORS_ONLY,
  ALLOW;

  public String friendlyName() {
    return Objects.requireNonNullElse(REVERSE_LOOKUP.get(this), "(unknown)");
  }

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
  public static final Map<FlagValue, String> REVERSE_LOOKUP = Map.of(
      DENY, "deny",
      EDITORS_ONLY, "editors",
      ALLOW, "allow"
  );

  public static FlagValue from(int ordinal) {
    if (ordinal >= 0 && ordinal < values().length) {
      return values()[ordinal];
    }
    throw new IllegalArgumentException("Illegal FlagValue ordinal: " + ordinal);
  }

}
