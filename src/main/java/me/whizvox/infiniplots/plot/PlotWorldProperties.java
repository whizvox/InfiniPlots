package me.whizvox.infiniplots.plot;

import me.whizvox.infiniplots.util.ChunkPos;

import java.util.UUID;

/**
 * Stores all relevant properties about a plot world
 * @param id The unique ID of the world (refers to {@link org.bukkit.World#getUID()})
 * @param name The name of the world
 * @param generator The key of the plot world generator
 * @param nextPos The next available position for a plot
 */
public record PlotWorldProperties(UUID id, String name, String generator, ChunkPos nextPos) {
}
