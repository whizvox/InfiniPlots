package me.whizvox.infiniplots.plot;

import me.whizvox.infiniplots.util.ChunkPos;

import java.util.Set;
import java.util.UUID;

/**
 * Stores all relevant information about a single plot.
 * @param id The unique ID of this plot, which is auto-assigned to a random UUID when inserting into the plots
 *           repository
 * @param owner The unique ID of the owner player (refers to {@link org.bukkit.entity.Player#getUniqueId()}
 * @param localId The local ID of this plot relative to the owner. If a player has no pre-existing plots, this is set to
 *                1. Whenever this same player lays claim to more plots, this value is increased by 1. In essence, you
 *                can refer to a single plot if given a player's unique ID and the local ID.
 * @param members Represents all other players that have been given permission to build on this plot. This set does not
 *                contain the unique ID of the owner. This set is not guaranteed to be filled out.
 * @param world The unique ID of the world where this plot is located (refers to {@link org.bukkit.World#getUID()}
 * @param pos The chunk position of this plot in the world
 */
public record Plot(UUID id, UUID owner, int localId, Set<UUID> members, UUID world, ChunkPos pos) {

}
