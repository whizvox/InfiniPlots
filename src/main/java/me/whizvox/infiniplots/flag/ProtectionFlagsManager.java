package me.whizvox.infiniplots.flag;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ProtectionFlagsManager {

  private final Map<String, ProtectionFlagProperties> properties;

  public ProtectionFlagsManager() {
    properties = new HashMap<>();
  }

  public void forEach(Consumer<ProtectionFlagProperties> consumer) {
    properties.values().forEach(consumer);
  }

  public void register(ProtectionFlagProperties props) {
    properties.put(props.flag(), props);
  }

  public void registerDefaults() {
    properties.putAll(StandardProtectionFlags.STANDARD_FLAGS);
  }

  public void unregister(String flag) {
    properties.remove(flag);
  }

}
