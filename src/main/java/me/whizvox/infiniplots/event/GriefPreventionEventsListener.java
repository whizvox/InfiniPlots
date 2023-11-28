package me.whizvox.infiniplots.event;

import me.whizvox.infiniplots.InfiniPlots;
import me.whizvox.infiniplots.flag.DefaultFlags;
import me.whizvox.infiniplots.flag.FlagValue;
import me.whizvox.infiniplots.plot.PlotWorld;
import me.whizvox.infiniplots.util.ChatUtils;
import me.whizvox.infiniplots.util.ChunkPos;
import me.whizvox.infiniplots.util.EntityTypePredicate;
import me.whizvox.infiniplots.util.MaterialPredicate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.*;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.inventory.ItemStack;
import org.spigotmc.event.entity.EntityMountEvent;

import java.util.UUID;
import java.util.function.Predicate;

public class GriefPreventionEventsListener implements Listener {

  private static final Predicate<Material> INVENTORY_BLOCKS = MaterialPredicate.materials(
      Material.CHEST,
      Material.TRAPPED_CHEST,
      Material.ENDER_CHEST,
      Material.BARREL,
      Material.SHULKER_BOX,
      Material.FURNACE,
      Material.BLAST_FURNACE,
      Material.SMOKER,
      Material.DISPENSER,
      Material.DROPPER,
      Material.BREWING_STAND,
      Material.BEACON,
      Material.HOPPER
  );

  private static final Predicate<Material> RAIL_BLOCKS = MaterialPredicate.tag(Tag.RAILS);

  private static final Predicate<Material> MINECART_ITEMS = MaterialPredicate.materials(
      Material.MINECART,
      Material.CHEST_MINECART,
      Material.COMMAND_BLOCK_MINECART,
      Material.TNT_MINECART,
      Material.HOPPER_MINECART,
      Material.FURNACE_MINECART
  );

  private static final Predicate<Material> BOAT_ITEMS = MaterialPredicate.tag(Tag.ITEMS_BOATS);

  private static final Predicate<Material> CONCRETE_BLOCKS = MaterialPredicate.materials(
      Material.WHITE_CONCRETE,
      Material.ORANGE_CONCRETE,
      Material.MAGENTA_CONCRETE,
      Material.LIGHT_BLUE_CONCRETE,
      Material.YELLOW_CONCRETE,
      Material.LIME_CONCRETE,
      Material.PINK_CONCRETE,
      Material.GRAY_CONCRETE,
      Material.LIGHT_GRAY_CONCRETE,
      Material.CYAN_CONCRETE,
      Material.PURPLE_CONCRETE,
      Material.BLUE_CONCRETE,
      Material.BROWN_CONCRETE,
      Material.GREEN_CONCRETE,
      Material.RED_CONCRETE,
      Material.BLACK_CONCRETE
  );

  private static final Predicate<Material> CORAL_BLOCKS = MaterialPredicate.tags(
      Tag.CORAL_BLOCKS,
      Tag.CORALS,
      Tag.WALL_CORALS
  );

  private static final Predicate<Material> VINE_BLOCKS = MaterialPredicate.composite(
      Material.VINE,
      Material.TWISTING_VINES,
      Material.WEEPING_VINES,
      Tag.CAVE_VINES
  );

  private static final Predicate<Material> AMETHYST_BLOCKS = MaterialPredicate.materials(
      Material.BUDDING_AMETHYST,
      Material.AMETHYST_CLUSTER,
      Material.SMALL_AMETHYST_BUD,
      Material.MEDIUM_AMETHYST_BUD,
      Material.LARGE_AMETHYST_BUD
  );

  private static final Predicate<EntityType> VEHICLE_ENTITIES = EntityTypePredicate.types(
      EntityType.BOAT,
      EntityType.CHEST_BOAT,
      EntityType.MINECART,
      EntityType.MINECART_FURNACE,
      EntityType.MINECART_CHEST,
      EntityType.MINECART_COMMAND,
      EntityType.MINECART_HOPPER,
      EntityType.MINECART_MOB_SPAWNER,
      EntityType.MINECART_TNT
  );

  private boolean checkPlayerAction(Cancellable event, PlotWorld plotWorld, Player player, Location location, String flag) {
    if (!player.hasPermission("infiniplots.bypass." + flag)) {
      boolean allowed;
      int plotNumber = plotWorld.generator.getPlotNumber(new ChunkPos(location));
      if (plotNumber < 1) {
        // not in a plot
        allowed = false;
      } else {
        boolean isEditor = plotWorld.isPlotEditor(player, plotNumber);
        allowed = plotWorld.worldFlags.getValue(flag).isAllowed(isEditor) ||
            plotWorld.getPlotFlags(plotNumber).getValue(flag).isAllowed(isEditor);
      }
      if (!allowed) {
        player.sendMessage(ChatUtils.altColors("&cNot allowed here"));
        event.setCancelled(true);
        return false;
      }
    }
    return true;
  }

  private boolean checkPlayerAction(Cancellable event, Player player, Location location, String flag) {
    PlotWorld plotWorld = InfiniPlots.getInstance().getPlotManager().getPlotWorld(location.getWorld().getUID());
    if (plotWorld == null) {
      return true;
    }
    return checkPlayerAction(event, plotWorld, player, location, flag);
  }

  private boolean checkNaturalAction(Cancellable event, PlotWorld plotWorld, Location location, String flag) {
    ChunkPos pos = new ChunkPos(location);
    // do not allow the event if either of the following is true:
    // 1. the location of the event is not in a plot
    // 2. the world flag is not set to ALLOW if defined
    if (!plotWorld.generator.inPlot(pos.x(), pos.z()) || plotWorld.worldFlags.getValue(flag) != FlagValue.ALLOW) {
      // TODO Add config option for this or make this smarter
      InfiniPlots.getInstance().getLogger().finer("Prohibited " + flag + " event at " + location);
      event.setCancelled(true);
      return false;
    }
    return true;
  }

  private boolean checkNaturalAction(Cancellable event, Location location, String flag) {
    PlotWorld plotWorld = InfiniPlots.getInstance().getPlotManager().getPlotWorld(location.getWorld().getUID());
    if (plotWorld == null) {
      return false;
    }
    return checkNaturalAction(event, plotWorld, location, flag);
  }

  @EventHandler
  public void onBreakBlock(BlockBreakEvent event) {
    checkPlayerAction(event, event.getPlayer(), event.getBlock().getLocation(), DefaultFlags.BUILD.name());
  }

  @EventHandler
  public void onPlaceBlock(BlockPlaceEvent event) {
    checkPlayerAction(event, event.getPlayer(), event.getBlock().getLocation(), DefaultFlags.BUILD.name());
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    // TODO Allow exceptions
    Block block = event.getClickedBlock();
    ItemStack item = event.getItem();
    Player player = event.getPlayer();
    if (block != null) {
      if (event.getAction() == Action.PHYSICAL) {
        if (block.getType() == Material.FARMLAND) {
          if (!checkPlayerAction(event, player, block.getLocation(), DefaultFlags.CROP_TRAMPLING.name())) {
            return;
          }
        } else {
          if (!checkPlayerAction(event, player, block.getLocation(), DefaultFlags.INTERACT.name())) {
            return;
          }
        }
      } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
        if (INVENTORY_BLOCKS.test(block.getType())) {
          if (!checkPlayerAction(event, player, block.getLocation(), DefaultFlags.INVENTORY_ACCESS.name())) {
            return;
          }
        } else if (RAIL_BLOCKS.test(block.getType())) {
          if (item != null && MINECART_ITEMS.test(item.getType())) {
            if (!checkPlayerAction(event, player, block.getLocation(), DefaultFlags.VEHICLE_PLACE.name())) {
              return;
            }
          }
        } else if (block.getType() == Material.WATER) {
          if (item != null && BOAT_ITEMS.test(item.getType())) {
            if (!checkPlayerAction(event, player, block.getLocation(), DefaultFlags.VEHICLE_PLACE.name())) {
              return;
            }
          }
        } else {
          if (!checkPlayerAction(event, player, block.getLocation(), DefaultFlags.INTERACT.name())) {
            return;
          }
        }
      }
    }
    if (item != null) {
      if (item.getType() == Material.SPLASH_POTION || item.getType() == Material.LINGERING_POTION) {
        if (!checkPlayerAction(event, player, player.getLocation(), DefaultFlags.POTION_SPLASH.name())) {
          return;
        }
      }
    }
  }

  @EventHandler
  public void onPlayerInteractWithEntity(PlayerInteractEntityEvent event) {
    Player player = event.getPlayer();
    Entity entity = event.getRightClicked();
    if (entity instanceof ItemFrame) {
      checkPlayerAction(event, player, entity.getLocation(), DefaultFlags.ITEM_FRAME_ROTATION.name());
    } else {
      checkPlayerAction(event, player, entity.getLocation(), DefaultFlags.INTERACT.name());
    }
  }

  @EventHandler
  public void onHurt(EntityDamageByEntityEvent event) {
    if (event.getEntity() instanceof Player player) {
      checkPlayerAction(event, player, event.getEntity().getLocation(), DefaultFlags.DAMAGE_MOBS.name());
    }
  }

  @EventHandler
  public void onDamageVehicle(VehicleDamageEvent event) {
    if (event.getAttacker() instanceof Player player) {
      checkPlayerAction(event, player, event.getVehicle().getLocation(), DefaultFlags.VEHICLE_DESTROY.name());
    }
  }

  @EventHandler
  public void onDestroyVehicle(VehicleDestroyEvent event) {
    if (event.getAttacker() instanceof Player player) {
      checkPlayerAction(event, player, event.getVehicle().getLocation(), DefaultFlags.VEHICLE_DESTROY.name());
    }
  }

  @EventHandler
  public void onRideVehicle(VehicleEnterEvent event) {
    if (event.getEntered() instanceof Player player) {
      checkPlayerAction(event, player, event.getVehicle().getLocation(), DefaultFlags.RIDE.name());
    }
  }

  @EventHandler
  public void onMountEntity(EntityMountEvent event) {
    if (event.getMount() instanceof Player player) {
      checkPlayerAction(event, player, event.getEntity().getLocation(), DefaultFlags.RIDE.name());
    }
  }

  @EventHandler
  public void onPortalCreate(PortalCreateEvent event) {
    if (event.getEntity() instanceof Player player && event.getReason() == PortalCreateEvent.CreateReason.FIRE) {
      PlotWorld plotWorld = InfiniPlots.getInstance().getPlotManager().getPlotWorld(event.getWorld().getUID());
      if (plotWorld != null) {
        for (BlockState block : event.getBlocks()) {
          if (!checkPlayerAction(event, plotWorld, player, block.getLocation(), DefaultFlags.NETHER_PORTAL_CREATE.name())) {
            return;
          }
        }
      }
    } else {
      event.setCancelled(true);
      InfiniPlots.getInstance().getLogger().warning("Blocked portal creation attempt at " + event.getBlocks().get(0).getLocation());
    }
  }

  @EventHandler
  public void onEntityDamage(EntityDamageByEntityEvent event) {
    if (event.getDamager() instanceof Firework) {
      checkNaturalAction(event, event.getEntity().getLocation(), DefaultFlags.FIREWORK_DAMAGE.name());
    } else if (event.getDamager() instanceof Player player) {
      checkPlayerAction(event, player, event.getEntity().getLocation(), DefaultFlags.DAMAGE_MOBS.name());
    }
  }

  @EventHandler
  public void onBlockMove(BlockFromToEvent event) {
    Location location = event.getToBlock().getLocation();
    Material type = event.getBlock().getType();
    if (type == Material.DRAGON_EGG) {
      // somehow didn't catch the player interacting event
      checkNaturalAction(event, location, DefaultFlags.INTERACT.name());
    } else if (type == Material.WATER) {
      checkNaturalAction(event, location, DefaultFlags.WATER_FLOW.name());
    } else if (type == Material.LAVA) {
      checkNaturalAction(event, location, DefaultFlags.LAVA_FLOW.name());
    }
  }

  @EventHandler
  public void onBlockForm(BlockFormEvent event) {
    Location location = event.getBlock().getLocation();
    Material type = event.getNewState().getType();
    if (type == Material.ICE) {
      checkNaturalAction(event, location, DefaultFlags.ICE_FORM.name());
    } else if (type == Material.SNOW_BLOCK) {
      checkNaturalAction(event, location, DefaultFlags.SNOW_FALL.name());
    } else if (type == Material.FROSTED_ICE) {
      checkNaturalAction(event, location, DefaultFlags.FROSTED_ICE_FORM.name());
    } else if (CONCRETE_BLOCKS.test(type)) {
      checkNaturalAction(event, location, DefaultFlags.CONCRETE_FORM.name());
    }
  }

  @EventHandler
  public void onBlockFade(BlockFadeEvent event) {
    Location location = event.getBlock().getLocation();
    Material type = event.getBlock().getType();
    if (type == Material.ICE) {
      checkNaturalAction(event, location, DefaultFlags.ICE_MELT.name());
    } else if (type == Material.SNOW_BLOCK) {
      checkNaturalAction(event, location, DefaultFlags.SNOW_FALL.name());
    } else if (type == Material.FROSTED_ICE) {
      checkNaturalAction(event, location, DefaultFlags.FROSTED_ICE_MELT.name());
    } else if (type == Material.FARMLAND) {
      checkNaturalAction(event, location, DefaultFlags.SOIL_DRY.name());
    } else if (CORAL_BLOCKS.test(type)) {
      checkNaturalAction(event, location, DefaultFlags.CORAL_FADE.name());
    }
  }

  @EventHandler
  public void onLeavesDecay(LeavesDecayEvent event) {
    checkNaturalAction(event, event.getBlock().getLocation(), DefaultFlags.LEAF_DECAY.name());
  }

  @EventHandler
  public void onBlockSpread(BlockSpreadEvent event) {
    Location location = event.getBlock().getLocation();
    Material type = event.getNewState().getType();
    if (type == Material.GRASS_BLOCK) {
      checkNaturalAction(event, location, DefaultFlags.GRASS_SPREAD.name());
    } else if (type == Material.MYCELIUM) {
      checkNaturalAction(event, location, DefaultFlags.MYCELIUM_SPREAD.name());
    } else if (VINE_BLOCKS.test(type)) {
      checkNaturalAction(event, location, DefaultFlags.VINE_GROWTH.name());
    } else if (AMETHYST_BLOCKS.test(type) || type == Material.POINTED_DRIPSTONE) {
      checkNaturalAction(event, location, DefaultFlags.ROCK_GROWTH.name());
    } else if (type == Material.SCULK || type == Material.SCULK_VEIN) {
      checkNaturalAction(event, location, DefaultFlags.SCULK_GROWTH.name());
    } else if (type == Material.RED_MUSHROOM || type == Material.BROWN_MUSHROOM) {
      checkNaturalAction(event, location, DefaultFlags.MUSHROOM_GROWTH.name());
    }
  }

  @EventHandler
  public void onBlockGrow(BlockGrowEvent event) {
    Location location = event.getBlock().getLocation();
    Material type = event.getBlock().getType();
    if (type != Material.TURTLE_EGG) {
      checkNaturalAction(event, location, DefaultFlags.CROP_GROWTH.name());
    }
  }

  @EventHandler
  public void onExpBottleBroken(ExpBottleEvent event) {
    checkNaturalAction(event, event.getEntity().getLocation(), DefaultFlags.EXP_DROPS.name());
  }

  @EventHandler
  public void onEntitySpawn(EntitySpawnEvent event) {
    Location location = event.getLocation();
    EntityType type = event.getEntityType();
    if (type == EntityType.EXPERIENCE_ORB) {
      checkNaturalAction(event, location, DefaultFlags.EXP_DROPS.name());
    } else if (type == EntityType.AREA_EFFECT_CLOUD) {
      checkNaturalAction(event, location, DefaultFlags.POTION_SPLASH.name());
    } else if (event.getEntity() instanceof Item item) {
      UUID throwerId = item.getThrower();
      Player thrower;
      if (throwerId != null) {
        thrower = Bukkit.getPlayer(throwerId);
        if (thrower != null) {
          checkPlayerAction(event, thrower, location, DefaultFlags.ITEM_DROP.name());
        }
      } else {
        thrower = null;
      }
      if (thrower == null) {
        checkNaturalAction(event, location, DefaultFlags.ITEM_DROP.name());
      }
    } else if (VEHICLE_ENTITIES.test(type)) {
      checkNaturalAction(event, location, DefaultFlags.VEHICLE_PLACE.name());
    } else if (event.getEntity() instanceof Projectile projectile) {
      if (projectile.getShooter() instanceof Player player) {
        checkPlayerAction(event, player, location, DefaultFlags.SHOOT_PROJECTILE.name());
      } else {
        checkNaturalAction(event, location, DefaultFlags.SHOOT_PROJECTILE.name());
      }
    }
  }

  @EventHandler
  public void onPickupItem(EntityPickupItemEvent event) {
    if (event.getEntity() instanceof Player player) {
      checkPlayerAction(event, player, event.getItem().getLocation(), DefaultFlags.ITEM_PICKUP.name());
    } else {
      checkNaturalAction(event, event.getItem().getLocation(), DefaultFlags.ITEM_PICKUP.name());
    }
  }

  @EventHandler
  public void onDispenseEvent(BlockDispenseEvent event) {
    checkNaturalAction(event, event.getBlock().getLocation(), DefaultFlags.ITEM_DROP.name());
  }

  @EventHandler
  public void onPistonExtend(BlockPistonExtendEvent event) {
    checkNaturalAction(event, event.getBlock().getLocation(), DefaultFlags.PISTONS.name());
  }

  @EventHandler
  public void onPistonRetract(BlockPistonRetractEvent event) {
    checkNaturalAction(event, event.getBlock().getLocation(), DefaultFlags.PISTONS.name());
  }

  @EventHandler
  public void onEntityEnterPortal(EntityPortalEvent event) {
    checkNaturalAction(event, event.getEntity().getLocation(), DefaultFlags.NETHER_PORTAL_ENTER.name());
  }

  @EventHandler
  public void onPlayerEnterPortal(PlayerPortalEvent event) {
    checkPlayerAction(event, event.getPlayer(), event.getPlayer().getLocation(), DefaultFlags.NETHER_PORTAL_ENTER.name());
  }

  @EventHandler
  public void onPotionSplash(PotionSplashEvent event) {
    if (event.getPotion().getShooter() instanceof Player player) {
      checkPlayerAction(event, player, event.getPotion().getLocation(), DefaultFlags.POTION_SPLASH.name());
    } else {
      checkNaturalAction(event, event.getPotion().getLocation(), DefaultFlags.POTION_SPLASH.name());
    }
  }

  @EventHandler
  public void onEntityDropItem(EntityDropItemEvent event) {
    if (event.getEntity() instanceof Player player) {
      checkPlayerAction(event, player, player.getLocation(), DefaultFlags.ITEM_DROP.name());
    } else {
      checkNaturalAction(event, event.getEntity().getLocation(), DefaultFlags.ITEM_DROP.name());
    }
  }

}
