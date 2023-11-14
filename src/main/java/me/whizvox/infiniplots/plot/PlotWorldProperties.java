package me.whizvox.infiniplots.plot;

import java.util.Set;
import java.util.UUID;

/**
 * Stores all relevant properties about a plot world
 * @param id The unique ID of the world (refers to {@link org.bukkit.World#getUID()})
 * @param name The name of the world
 * @param generator The key of the plot world generator
 * @param lockdown The lockdown level of this world
 * @param flags Protection flags for this world
 */
public record PlotWorldProperties(UUID id,
                                  String name,
                                  String generator,
                                  LockdownLevel lockdown,
                                  Set<String> flags) {
}
