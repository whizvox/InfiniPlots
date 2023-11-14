package me.whizvox.infiniplots.event;

import me.whizvox.infiniplots.InfiniPlots;
import me.whizvox.infiniplots.plot.PlotWorld;
import me.whizvox.infiniplots.util.ChatUtils;
import me.whizvox.infiniplots.util.PermissionUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.spigotmc.event.entity.EntityMountEvent;

public class GriefPreventionEventsListener implements Listener {

  private static final String
      BYPASS_BUILD = PermissionUtils.buildPermission("admin.bypass.build"),
      BYPASS_INTERACT = PermissionUtils.buildPermission("admin.bypass.interact"),
      BYPASS_HURT_ENTITY = PermissionUtils.buildPermission("admin.bypass.hurtEntity"),
      BYPASS_DAMAGE_VEHICLE = PermissionUtils.buildPermission("admin.bypass.damageVehicle"),
      BYPASS_RIDE = PermissionUtils.buildPermission("admin.bypass.ride");

  private boolean checkActionAllowed(Cancellable event, Player player, Location location, String permission) {
    PlotWorld plotWorld = InfiniPlots.getInstance().getPlotManager().getPlotWorld(location.getWorld().getUID());
    boolean allowed = plotWorld == null || player.hasPermission(permission) || plotWorld.canEdit(player, location);
    if (!allowed) {
      player.sendMessage(ChatUtils.altColors("&cNot allowed here"));
      event.setCancelled(true);
    }
    return allowed;
  }

  @EventHandler
  public void onBreakBlock(BlockBreakEvent event) {
    checkActionAllowed(event, event.getPlayer(), event.getBlock().getLocation(), BYPASS_BUILD);
  }

  @EventHandler
  public void onPlaceBlock(BlockPlaceEvent event) {
    checkActionAllowed(event, event.getPlayer(), event.getBlock().getLocation(), BYPASS_BUILD);
  }

  @EventHandler
  public void onInteract(PlayerInteractEvent event) {
    // TODO Allow exceptions
    if (event.hasBlock() && (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.PHYSICAL)) {
      checkActionAllowed(event, event.getPlayer(), event.getClickedBlock().getLocation(), BYPASS_INTERACT);
    }
  }

  @EventHandler
  public void onHurt(EntityDamageByEntityEvent event) {
    if (event.getEntity() instanceof Player player) {
      checkActionAllowed(event, player, event.getEntity().getLocation(), BYPASS_HURT_ENTITY);
    }
  }

  @EventHandler
  public void onDamageVehicle(VehicleDamageEvent event) {
    if (event.getAttacker() instanceof Player player) {
      checkActionAllowed(event, player, event.getVehicle().getLocation(), BYPASS_DAMAGE_VEHICLE);
    }
  }

  @EventHandler
  public void onDestroyVehicle(VehicleDestroyEvent event) {
    if (event.getAttacker() instanceof Player player) {
      checkActionAllowed(event, player, event.getVehicle().getLocation(), BYPASS_DAMAGE_VEHICLE);
    }
  }

  @EventHandler
  public void onRideVehicle(VehicleEnterEvent event) {
    if (event.getEntered() instanceof Player player) {
      checkActionAllowed(event, player, event.getVehicle().getLocation(), BYPASS_RIDE);
    }
  }

  @EventHandler
  public void onMountEntity(EntityMountEvent event) {
    if (event.getMount() instanceof Player player) {
      checkActionAllowed(event, player, event.getEntity().getLocation(), BYPASS_RIDE);
    }
  }

}
