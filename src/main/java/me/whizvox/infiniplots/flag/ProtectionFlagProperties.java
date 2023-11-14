package me.whizvox.infiniplots.flag;

import java.util.List;
import java.util.Map;

public record ProtectionFlagProperties(String flag, String description, List<Map.Entry<String, Object>> worldGuardFlags) {

  public ProtectionFlagProperties(String flag, String description) {
    this(flag, description, List.of(Map.entry(camelCaseToDashCase(flag), false)));
  }

  public ProtectionFlagProperties(String flag, String description, String worldGuardFlag, Object defaultValue) {
    this(flag, description, List.of(Map.entry(worldGuardFlag, defaultValue)));
  }

  private static String camelCaseToDashCase(String str) {
    StringBuilder sb = new StringBuilder();
    for (char c : str.toCharArray()) {
      if (Character.isUpperCase(c)) {
        sb.append(Character.toLowerCase(c));
        sb.append('-');
      } else {
        sb.append(c);
      }
    }
    return sb.toString();
  }

}
