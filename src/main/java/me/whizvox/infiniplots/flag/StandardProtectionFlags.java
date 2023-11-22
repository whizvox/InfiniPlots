package me.whizvox.infiniplots.flag;

import me.whizvox.infiniplots.InfiniPlots;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class StandardProtectionFlags {

  public static final ProtectionFlagProperties
      INTERACT = new ProtectionFlagProperties("interact", "Other players can interact with blocks and items, such as stepping on pressure plates or interacting with buttons"),
      DAMAGE_MOBS = new ProtectionFlagProperties("damageMobs", "Other players can hurt mobs, including animals and monsters", List.of()),
      INVENTORY_ACCESS = new ProtectionFlagProperties("inventoryAccess", "Other players can access block inventories, such as chests and dispensers", "chest-access", false),
      RIDE = new ProtectionFlagProperties("ride", "Other players can ride of minecarts and animals"),
      VEHICLE_PLACE = new ProtectionFlagProperties("vehiclePlace", "Other players can place vehicles, such as boats and minecarts"),
      VEHICLE_DESTROY = new ProtectionFlagProperties("vehicleDestroy", "Other players can damage or destroy vehicles"),
      CROP_TRAMPLING = new ProtectionFlagProperties("cropTrampling", "Other players can trample crops"),
      ITEM_FRAME_ROTATION = new ProtectionFlagProperties("itemFrameRotation", "Other players can rotate item frames"),
      FIREWORK_DAMAGE = new ProtectionFlagProperties("fireworkDamage", "Fireworks can damage mobs and entities"),
      STILL_WATER = new ProtectionFlagProperties("stillWater", "Water will not flow", "water-flow", true),
      STILL_LAVA = new ProtectionFlagProperties("stillLava", "Lava will not flow", "lava-flow", true),
      ICE_FORM = new ProtectionFlagProperties("iceForm", "Ice can naturally form, which is only applicable in cold biomes"),
      ICE_MELT = new ProtectionFlagProperties("iceMelt", "Ice can melt from nearby heat sources"),
      FROSTED_ICE_FORM = new ProtectionFlagProperties("frostedIceForm", "Ice from the Frost Walker enchantment can form"),
      FROSTED_ICE_MELT = new ProtectionFlagProperties("frostedIceMelt", "Ice from the Frost Walker enchantment can melt"),
      LEAF_DECAY = new ProtectionFlagProperties("leafDecay", "Leaves from felled tree will decay over time"),
      GRASS_GROWTH = new ProtectionFlagProperties("grassGrowth", "Grass can spread to nearby dirt blocks"),
      MYCELIUM_SPREAD = new ProtectionFlagProperties("myceliumSpread", "Mycelium can spread to nearby dirt blocks"),
      VINE_GROWTH = new ProtectionFlagProperties("vineGrowth", "Vines can grow on the sides of blocks"),
      ROCK_GROWTH = new ProtectionFlagProperties("rockGrowth", "Dripstone stalagmites and stalactites can grow over time"),
      SCULK_GROWTH = new ProtectionFlagProperties("sculkGrowth", "Sculk can spread when a mob is killed near it"),
      CROP_GROWTH = new ProtectionFlagProperties("cropGrowth", "Crops can grow"),
      SOIL_DRY = new ProtectionFlagProperties("soilDry", "Unwatered farm land can dry out"),
      CORAL_FADE = new ProtectionFlagProperties("coralFade", "Non-submerged coral can dry and fade"),
      EXP_DROPS = new ProtectionFlagProperties("expDrops", "Experience can drop from killed mobs"),
      PISTONS = new ProtectionFlagProperties("pistons", "Pistons can extend and retract"),
      POTION_SPLASH = new ProtectionFlagProperties("potionSplash", "Splash potions can be used"),
      ITEM_PICKUP = new ProtectionFlagProperties("itemPickup", "Dropped items can be picked up"),
      ITEM_DROP = new ProtectionFlagProperties("itemDrop", "Items can be dropped");

  public static final Map<String, ProtectionFlagProperties> STANDARD_FLAGS;

  static {
    Map<String, ProtectionFlagProperties> temp = new HashMap<>();
    for (Field field : StandardProtectionFlags.class.getFields()) {
      if (Modifier.isStatic(field.getModifiers()) && ProtectionFlagProperties.class.isAssignableFrom(field.getType())) {
        try {
          ProtectionFlagProperties props = (ProtectionFlagProperties) field.get(null);
          temp.put(props.flag(), props);
        } catch (IllegalAccessException e) {
          InfiniPlots.getInstance().getLogger().log(Level.WARNING, "Could not cast protection flag properties", e);
        }
      }
    }
    STANDARD_FLAGS = Collections.unmodifiableMap(temp);
  }

}
