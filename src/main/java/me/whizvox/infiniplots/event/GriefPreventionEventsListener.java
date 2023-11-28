package me.whizvox.infiniplots.event;

import me.whizvox.infiniplots.InfiniPlots;
import me.whizvox.infiniplots.flag.DefaultFlags;
import me.whizvox.infiniplots.flag.FlagValue;
import me.whizvox.infiniplots.plot.PlotWorld;
import me.whizvox.infiniplots.util.ChatUtils;
import me.whizvox.infiniplots.util.ChunkPos;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.inventory.ItemStack;
import org.spigotmc.event.entity.EntityMountEvent;

import java.util.Set;

public class GriefPreventionEventsListener implements Listener {

  private static final Set<Material> INVENTORY_BLOCKS = Set.of(
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
  private static final Set<Material> RAIL_BLOCKS = Set.of(
      Material.RAIL,
      Material.ACTIVATOR_RAIL,
      Material.DETECTOR_RAIL,
      Material.POWERED_RAIL
  );
  private static final Set<Material> MINECART_ITEMS = Set.of(
      Material.MINECART,
      Material.CHEST_MINECART,
      Material.COMMAND_BLOCK_MINECART,
      Material.TNT_MINECART,
      Material.HOPPER_MINECART,
      Material.FURNACE_MINECART
  );
  private static final Set<Material> BOAT_ITEMS = Set.of(
      Material.OAK_BOAT,
      Material.BIRCH_BOAT,
      Material.SPRUCE_BOAT,
      Material.JUNGLE_BOAT,
      Material.ACACIA_BOAT,
      Material.DARK_OAK_BOAT,
      Material.CHERRY_BOAT,
      Material.MANGROVE_BOAT,
      Material.OAK_CHEST_BOAT,
      Material.BIRCH_CHEST_BOAT,
      Material.SPRUCE_CHEST_BOAT,
      Material.JUNGLE_CHEST_BOAT,
      Material.ACACIA_CHEST_BOAT,
      Material.DARK_OAK_CHEST_BOAT,
      Material.CHERRY_CHEST_BOAT,
      Material.MANGROVE_CHEST_BOAT
  );

  private boolean checkPlayerAction(Cancellable event, Player player, Location location, String flag) {
    PlotWorld plotWorld = InfiniPlots.getInstance().getPlotManager().getPlotWorld(location.getWorld().getUID());
    if (plotWorld != null) {
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
    }
    return true;
  }

  private boolean checkNaturalAction(Cancellable event, Location location, String flag) {
    PlotWorld plotWorld = InfiniPlots.getInstance().getPlotManager().getPlotWorld(location.getWorld().getUID());
    if (plotWorld != null) {
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
    }
    return true;
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
  public void onInteract(PlayerInteractEvent event) {
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
        }
      } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
        if (INVENTORY_BLOCKS.contains(block.getType())) {
          if (!checkPlayerAction(event, player, block.getLocation(), DefaultFlags.INVENTORY_ACCESS.name())) {
            return;
          }
        } else if (RAIL_BLOCKS.contains(block.getType())) {
          if (item != null && MINECART_ITEMS.contains(item.getType())) {
            if (!checkPlayerAction(event, player, block.getLocation(), DefaultFlags.VEHICLE_PLACE.name())) {
              return;
            }
          }
        } else if (block.getType() == Material.WATER) {
          if (item != null && BOAT_ITEMS.contains(item.getType())) {
            if (!checkPlayerAction(event, player, block.getLocation(), DefaultFlags.VEHICLE_PLACE.name())) {
              return;
            }
          }
        }
      }
      if (block.getType().isInteractable()) {
        if (!checkPlayerAction(event, player, block.getLocation(), DefaultFlags.INTERACT.name())) {
          return;
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
  public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
    Player player = event.getPlayer();
    Entity entity = event.getRightClicked();
    ItemStack item = player.getInventory().getItem(event.getHand());
    if (entity instanceof ItemFrame) {
      checkPlayerAction(event, player, entity.getLocation(), DefaultFlags.ITEM_FRAME_ROTATION.name());
    } else if (entity instanceof Pig) {
      // putting a saddle on a pig technically counts as placing a vehicle
      if (item != null && item.getType() == Material.SADDLE) {
        checkPlayerAction(event, player, entity.getLocation(), DefaultFlags.VEHICLE_PLACE.name());
      }
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
    PlotWorld plotWorld = InfiniPlots.getInstance().getPlotManager().getPlotWorld(event.getWorld().getUID());
    if (plotWorld != null) {
      if (event.getEntity() instanceof Player player && event.getReason() == PortalCreateEvent.CreateReason.FIRE) {
        for (BlockState block : event.getBlocks()) {
          if (!checkPlayerAction(event, player, block.getLocation(), DefaultFlags.NETHER_PORTAL_CREATE.name())) {
            return;
          }
        }
      } else {
        event.setCancelled(true);
        InfiniPlots.getInstance().getLogger().warning("Blocked portal creation attempt at " + event.getBlocks().get(0).getLocation());
      }
    }
  }

}
