package me.whizvox.infiniplots.plot;

import org.jetbrains.annotations.Nullable;

/**
 * Different levels of "lockdown" that a plot world can have. This is typically used if either admins want to limit when
 * players can build in or enter a plot world, or if someone's griefing is bypassing the standard permission checks,
 * making this effectively an emergency option.
 * <br>
 * However, players with the proper permissions can bypass these levels. Other than {@link #OFF}, the permission string
 * is <code>infiniplots.lockdown.bypass.&lt;level&gt;</code>.
 */
public enum LockdownLevel {

  /**
   * No lockdown
   */
  OFF,
  /**
   * Block interactions and using items (apart from block items) are disabled
   */
  INTERACT,
  /**
   * Placing and breaking blocks is disabled. The effects of the previous level ({@link #INTERACT}) are inherited.
   */
  BUILD,
  /**
   * Players cannot enter the plot world, and are kicked to a default world if they attempt to. The effects of the
   * previous level ({@link #BUILD}) are inherited.
   */
  ENTER;

  @Nullable
  public static LockdownLevel from(int ordinal) {
    if (ordinal >= 0 && ordinal < values().length) {
      return values()[ordinal];
    }
    return null;
  }

}
