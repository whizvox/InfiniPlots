package me.whizvox.infiniplots.util;

import me.whizvox.infiniplots.InfiniPlots;
import me.whizvox.infiniplots.flag.DefaultFlags;
import me.whizvox.infiniplots.flag.FlagValue;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class ProtectionFlags implements ConfigurationSerializable {

  public final Map<String, FlagValue> flags;

  public ProtectionFlags(Map<String, Object> data) {
    Map<String, FlagValue> temp = new HashMap<>();
    data.forEach((key, valueObj) -> {
      if (!key.equals("==")) {
        FlagValue value;
        if (valueObj instanceof String sValue) {
          value = FlagValue.VALUES_MAP.get(sValue);
        } else if (valueObj instanceof FlagValue fValue) {
          value = fValue;
        } else if (valueObj instanceof Integer iValue) {
          try {
            value = FlagValue.from(iValue);
          } catch (IllegalArgumentException e) {
            value = null;
          }
        } else {
          value = null;
        }
        if (value == null) {
          InfiniPlots.getInstance().getLogger().log(Level.WARNING, "Invalid flag value when reading %s: %s", new Object[] { key, valueObj });
        } else {
          temp.put(key, value);
        }
      }
    });
    this.flags = Collections.unmodifiableMap(temp);
  }

  @NotNull
  @Override
  public Map<String, Object> serialize() {
    Map<String, Object> map = new HashMap<>();
    flags.forEach((flag, value) -> map.put(flag, value.friendlyName));
    return map;
  }

  public static final ProtectionFlags DEFAULT;

  static {
    Map<String, Object> data = new HashMap<>();
    DefaultFlags.ALL_FLAGS.values().forEach(flag -> data.put(flag.name(), flag.value()));
    DEFAULT = new ProtectionFlags(data);
  }

}
