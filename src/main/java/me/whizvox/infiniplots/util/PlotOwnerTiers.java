package me.whizvox.infiniplots.util;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PlotOwnerTiers implements ConfigurationSerializable {

  public final Map<String, Integer> tiers;

  public PlotOwnerTiers(Map<String, Object> data) {
    Map<String, Integer> tiersTemp = new HashMap<>();
    data.forEach((name, countObj) -> {
      if (!name.equals("==")) {
        tiersTemp.put(name, (Integer) countObj);
      }
    });
    tiers = Collections.unmodifiableMap(tiersTemp);
  }

  @Override
  public Map<String, Object> serialize() {
    return new HashMap<>(tiers);
  }

  public static final PlotOwnerTiers DEFAULT = new PlotOwnerTiers(Map.of(
      "tier1", 1,
      "tier2", 3,
      "tier3", 10
  ));

}
