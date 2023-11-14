package me.whizvox.infiniplots.plot;

import java.util.Set;
import java.util.UUID;

/**
 * Stores all relevant information about a single plot.
 * @param world The unique ID of the world where this plot is located (refers to {@link org.bukkit.World#getUID()}
 * @param worldPlotId An identifier which is unique when in composition with the {@link #world} field. This begins at 1,
 *                    which corresponds to the plot at (0,0).
 * @param owner The unique ID of the owner player (refers to {@link org.bukkit.entity.Player#getUniqueId()}
 * @param ownerPlotId An identifier which is unique when in composition with the {@link #owner} field. This begins at 1.
 * @param members Represents all other players that have been given permission to build on this plot. This set does not
 *                contain the unique ID of the owner. This set is not guaranteed to be filled out.
 * @param flags Protection flags for this plot
 */
public record Plot(UUID world, int worldPlotId, UUID owner, int ownerPlotId, Set<UUID> members, Set<String> flags) {

}
