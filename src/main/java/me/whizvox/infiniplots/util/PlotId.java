package me.whizvox.infiniplots.util;

import me.whizvox.infiniplots.exception.InterruptCommandException;
import me.whizvox.infiniplots.exception.PlayerOnlyException;
import me.whizvox.infiniplots.plot.Plot;
import me.whizvox.infiniplots.plot.PlotWorld;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * An interface used to identify a single plot. Plots can be identified via one of two composite keys: a world ID and
 * world number, or an owner ID and an owner number.
 */
public interface PlotId {

  boolean hasWorld();

  UUID world();

  int worldNumber();

  boolean hasOwner();

  UUID owner();

  int ownerNumber();

  default String getFriendlyString() {
    return toString();
  }

  default void checkValid() {
    if (!hasWorld() && !hasOwner()) {
      throw new IllegalArgumentException("Plot ID must have a world or an owner");
    }
  }

  record WorldPlotId(UUID world, int worldNumber) implements PlotId {

    @Override
    public boolean hasWorld() {
      return true;
    }

    @Override
    public boolean hasOwner() {
      return false;
    }

    @Override
    public UUID owner() {
      return null;
    }

    @Override
    public int ownerNumber() {
      return 0;
    }

    @Override
    public String getFriendlyString() {
      StringBuilder sb = new StringBuilder();
      sb.append('#');
      World worldInst = Bukkit.getWorld(world);
      if (worldInst == null) {
        sb.append("<unknown>");
      } else {
        sb.append(worldInst.getName());
      }
      sb.append(':').append(worldNumber);
      return sb.toString();
    }

  }

  record OwnerPlotId(UUID owner, int ownerNumber) implements PlotId {

    @Override
    public boolean hasWorld() {
      return false;
    }

    @Override
    public UUID world() {
      return null;
    }

    @Override
    public int worldNumber() {
      return 0;
    }

    @Override
    public boolean hasOwner() {
      return true;
    }

    @Override
    public String getFriendlyString() {
      String ownerName = PlayerUtils.getOfflinePlayerName(owner);
      return ownerName + ":" + ownerNumber;
    }

  }

  static PlotId fromWorld(UUID worldId, int worldNumber) {
    return new WorldPlotId(worldId, worldNumber);
  }

  static PlotId fromWorld(Plot plot) {
    return fromWorld(plot.world(), plot.worldNumber());
  }

  static PlotId fromOwner(UUID ownerId, int ownerNumber) {
    return new OwnerPlotId(ownerId, ownerNumber);
  }

  static PlotId fromOwner(Plot plot) {
    return fromOwner(plot.owner(), plot.ownerNumber());
  }

  private static int parseNumber(String str, int begin) {
    String sub = str.substring(begin);
    try {
      return Integer.parseInt(sub);
    } catch (NumberFormatException e) {
      throw new InterruptCommandException("Invalid number: " + sub);
    }
  }

  /**
   * Parses a PlotId from a string. A PlotId string can be made up of a few parts: a domain, a separator, and a number.
   * The separator is always a colon (<b>:</b>), the domain can refer to a player name or a world name, and the number
   * refers to either an owner number or a world number and must be an integer greater than 0. If a plot ID string
   * begins with a hash symbol (<b>#</b>), then the string is interpreted as a world-based ID. Otherwise, it is
   * interpreted as an owner-based ID.
   * <br>
   * Owner-based IDs must be formatted like so: <code>[&lt;owner>:]&lt;number></code>. World-based IDs must be formatted
   * like so: <code>#[&lt;world>:]&lt;number></code>.
   * <br>
   * Here are some examples of plot ID strings:
   * <ul>
   *    <li><code>Bob123:3</code> - Refers to a plot owned by <b>Bob123</b> with an owner number of <b>3</b></li>
   *    <li><code>5</code> - Refers to a plot owned by the command sender with an owner number of <b>5</b></li>
   *    <li><code>#plots:6</code> - Refers to a plot in the <b>plots</b> world with a world number of <b>6</b></li>
   *    <li><code>#2</code> - Refers to a plot in either the world the command sender is standing in or the default
   *    world with a world number of <b>2</b></li>
   * </ul>
   * @param sender The sender using this string
   * @param str The string to be parsed
   * @return Either an instance of {@link OwnerPlotId} or {@link WorldPlotId}
   * @throws InterruptCommandException If something went wrong attempting to parse the string. A multitude of scenarios
   * can cause this, such as the number portion not being a valid number, the domain portion of a world ID not being a
   * valid world, etc.
   * @see PlotId
   */
  static PlotId fromString(CommandSender sender, String str) {
    int separatorIndex = str.indexOf(':');
    if (!str.isEmpty() && str.charAt(0) == '#') {
      if (separatorIndex < 0) {
        UUID worldId;
        if (sender instanceof Player player) {
          worldId = CommandHelper.getPlotWorld(player).world.getUID();
        } else {
          worldId = CommandHelper.getDefaultPlotWorld().world.getUID();
        }
        int number = parseNumber(str, 1);
        return fromWorld(worldId, number);
      }
      String worldName = str.substring(1, separatorIndex);
      UUID worldId;
      try {
        worldId = UUID.fromString(worldName);
      } catch (IllegalArgumentException e) {
        PlotWorld plotWorld = CommandHelper.getPlotWorld(str.substring(1, separatorIndex));
        worldId = plotWorld.world.getUID();
      }
      int number = parseNumber(str, separatorIndex + 1);
      return fromWorld(worldId, number);
    }
    if (separatorIndex < 0) {
      if (!(sender instanceof Player player)) {
        throw new PlayerOnlyException();
      }
      int number = parseNumber(str, 0);
      return fromOwner(player.getUniqueId(), number);
    }
    String ownerName = str.substring(0, separatorIndex);
    UUID ownerId;
    try {
      ownerId = UUID.fromString(ownerName);
    } catch (IllegalArgumentException e) {
      ownerId = CommandHelper.getOfflinePlayer(ownerName).getUniqueId();
    }
    int number = parseNumber(str, separatorIndex + 1);
    return fromOwner(ownerId, number);
  }

}
