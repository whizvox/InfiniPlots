package me.whizvox.infiniplots.flag;

import me.whizvox.infiniplots.InfiniPlots;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class DefaultFlags {

  public static final Flag
      BUILD = new Flag("build", FlagValue.EDITORS_ONLY),
      INTERACT = new Flag("interact", FlagValue.EDITORS_ONLY),
      DAMAGE_MOBS = new Flag("damageMobs", FlagValue.EDITORS_ONLY),
      INVENTORY_ACCESS = new Flag("inventoryAccess", FlagValue.EDITORS_ONLY),
      RIDE = new Flag("ride", FlagValue.EDITORS_ONLY),
      VEHICLE_PLACE = new Flag("vehiclePlace", FlagValue.EDITORS_ONLY),
      VEHICLE_DESTROY = new Flag("vehicleDestroy", FlagValue.EDITORS_ONLY),
      CROP_TRAMPLING = new Flag("cropTrampling", FlagValue.EDITORS_ONLY),
      ITEM_FRAME_ROTATION = new Flag("itemFrameRotation", FlagValue.EDITORS_ONLY),
      FIREWORK_DAMAGE = new Flag("fireworkDamage", FlagValue.DENY),
      WATER_FLOW = new Flag("waterFlow", FlagValue.ALLOW),
      LAVA_FLOW = new Flag("lavaFlow", FlagValue.ALLOW),
      ICE_FORM = new Flag("iceForm", FlagValue.DENY),
      ICE_MELT = new Flag("iceMelt", FlagValue.DENY),
      FROSTED_ICE_FORM = new Flag("frostedIceForm", FlagValue.EDITORS_ONLY),
      FROSTED_ICE_MELT = new Flag("frostedIceMelt", FlagValue.ALLOW),
      LEAF_DECAY = new Flag("leafDecay", FlagValue.ALLOW),
      GRASS_GROWTH = new Flag("grassGrowth", FlagValue.ALLOW),
      MYCELIUM_SPREAD = new Flag("myceliumSpread", FlagValue.ALLOW),
      VINE_GROWTH = new Flag("vineGrowth", FlagValue.ALLOW),
      ROCK_GROWTH = new Flag("rockGrowth", FlagValue.ALLOW),
      SCULK_GROWTH = new Flag("sculkGrowth", FlagValue.ALLOW),
      CROP_GROWTH = new Flag("cropGrowth", FlagValue.ALLOW),
      SOIL_DRY = new Flag("soilDry", FlagValue.ALLOW),
      CORAL_FADE = new Flag("coralFade", FlagValue.ALLOW),
      EXP_DROPS = new Flag("expDrops", FlagValue.DENY),
      PISTONS = new Flag("pistons", FlagValue.ALLOW),
      POTION_SPLASH = new Flag("potionSplash", FlagValue.DENY),
      ITEM_PICKUP = new Flag("itemPickup", FlagValue.ALLOW),
      ITEM_DROP = new Flag("itemDrop", FlagValue.DENY),
      NETHER_PORTAL_CREATE = new Flag("netherPortalCreate", FlagValue.EDITORS_ONLY),
      END_PORTAL_CREATE = new Flag("endPortalCreate", FlagValue.DENY),
      NETHER_PORTAL_ENTER = new Flag("netherPortalEnter", FlagValue.DENY),
      END_PORTAL_ENTER = new Flag("endPortalEnter", FlagValue.DENY);

  public static final Map<String, Flag> ALL_FLAGS;

  static {
    Map<String, Flag> allFlags = new HashMap<>();
    for (Field field : DefaultFlags.class.getFields()) {
      if (Modifier.isStatic(field.getModifiers()) && Flag.class.isAssignableFrom(field.getType())) {
        try {
          Flag flag = (Flag) field.get(null);
          allFlags.put(flag.name(), flag);
        } catch (IllegalAccessException e) {
          InfiniPlots.getInstance().getLogger().log(Level.WARNING, "Could not access default flag " + field.getName());
        }
      }
    }
    ALL_FLAGS = Collections.unmodifiableMap(allFlags);
  }

}
