package me.whizvox.infiniplots.command.lib;

import me.whizvox.infiniplots.InfiniPlots;
import me.whizvox.infiniplots.command.CommandContext;
import me.whizvox.infiniplots.command.CommandHandler;
import me.whizvox.infiniplots.exception.InterruptCommandException;
import me.whizvox.infiniplots.util.ChunkPos;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.util.BoundingBox;

import java.util.List;
import java.util.Random;

public abstract class AbstractRegenerateCommandHandler extends CommandHandler {

  public interface Arguments {
    World world();
    List<ChunkPos> chunks();
  }

  public record ArgumentsImpl(World world, List<ChunkPos> chunks) implements Arguments {
  }

  protected abstract Arguments getArguments(CommandContext context);

  protected void onPostConfirm(CommandContext context, Arguments args) {
  }

  @Override
  public void execute(CommandContext context) throws InterruptCommandException {
    Arguments args = getArguments(context);
    InfiniPlots.getInstance().getConfirmationManager().add(context.sender(), () -> {
      World world = args.world();
      List<ChunkPos> chunks = args.chunks();
      if (!chunks.isEmpty()) {
        chunks.forEach(pos -> {
          int startX = pos.x() * 16;
          int startZ = pos.z() * 16;
          world.getNearbyEntities(new BoundingBox(
              startX, world.getMinHeight(), startZ,
              startX + 16, world.getMaxHeight(), startZ + 16
          )).forEach(entity -> {
            if (!(entity instanceof Player)) {
              entity.remove();
            }
          });
          ChunkGenerator.ChunkData chunkData = Bukkit.createChunkData(world);
          ChunkGenerator gen = world.getGenerator();
          gen.generateSurface(world, new Random(), pos.x(), pos.z(), chunkData);
          for (int y = world.getMinHeight(); y < world.getMaxHeight(); y++) {
            for (int z = 0; z < 16; z++) {
              for (int x = 0; x < 16; x++) {
                world.setBlockData(startX + x, y, startZ + z, chunkData.getBlockData(x, y, z));
              }
            }
          }
        });
      }
      onPostConfirm(context, args);
    });
  }

}
