package me.whizvox.infiniplots.command.lib;

import me.whizvox.infiniplots.command.ArgumentHelper;
import me.whizvox.infiniplots.command.CommandContext;
import me.whizvox.infiniplots.command.SuggestionHelper;
import me.whizvox.infiniplots.exception.InterruptCommandException;
import me.whizvox.infiniplots.util.ChunkPos;
import me.whizvox.infiniplots.util.ChunkRegion;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class RegenCommandHandler extends AbstractRegenerateCommandHandler {

  private static final List<String> MANUAL = List.of(
      "Regenerates a region of chunks. &oNote&r: this only restores block data and will delete all entities.",
      "Examples:",
      "- &b/infiniplots regen&r : Regenerates the chunk you're currently standing in",
      "- &b/infiniplots regen 5 2&r : Regenerates the chunk located at (5,2)",
      "- &b/infiniplots regen 5 2 6 4&r : Regenerates the chunk region bounded by (&b5&r,&b2&r) and (&b6&r,&b4&r)",
      "- &b/infiniplots regen 5 2 6 4 plots&r : Regenerates the specified chunk region in the &eplots&r world",
      "See also:",
      "- &b/infiniplots manual reset"
  );

  @Override
  public String getUsageArguments() {
    return "[<x> <z> [<x2> <z2> [<world>]]]";
  }

  @Override
  public List<String> getManual() {
    return MANUAL;
  }

  @Override
  public List<String> listSuggestions(CommandContext context) {
    if (context.args().size() == 5) {
      return SuggestionHelper.worlds(context.arg(4));
    }
    return super.listSuggestions(context);
  }

  @Override
  public boolean hasPermission(CommandSender sender) {
    return sender.hasPermission("infiniplots.restore");
  }

  @Override
  protected Arguments getArguments(CommandContext context) {
    ChunkPos pos1;
    if (context.args().isEmpty()) {
      pos1 = new ChunkPos(context.getPlayerOrException().getLocation());
    } else {
      int x = ArgumentHelper.getRelativeInt(context, 0, () -> context.getPlayerOrException().getLocation().getChunk().getX());
      int z = ArgumentHelper.getRelativeInt(context, 1, () -> context.getPlayerOrException().getLocation().getChunk().getZ());
      pos1 = new ChunkPos(x, z);
    }
    ChunkPos pos2;
    if (context.args().size() < 2) {
      pos2 = pos1;
    } else {
      int x = ArgumentHelper.getRelativeInt(context, 2, () -> context.getPlayerOrException().getLocation().getChunk().getX());
      int z = ArgumentHelper.getRelativeInt(context, 3, () -> context.getPlayerOrException().getLocation().getChunk().getZ());
      pos2 = new ChunkPos(x, z);
    }
    World world = ArgumentHelper.getWorld(context, 4, () -> context.getPlayerOrException().getWorld());
    ChunkRegion region = new ChunkRegion(pos1, pos2);
    int width = region.p2().x() - region.p1().x();
    int depth = region.p2().z() - region.p1().z();
    if (width * depth > 200) {
      throw new InterruptCommandException("Too many chunks!");
    }
    List<ChunkPos> chunks = new ArrayList<>();
    for (int z = region.p1().z(); z <= region.p2().z(); z++) {
      for (int x = region.p1().x(); x <= region.p2().x(); x++) {
        chunks.add(new ChunkPos(x, z));
      }
    }
    return new ArgumentsImpl(world, chunks);
  }

  @Override
  protected void onPostConfirm(CommandContext context, Arguments args) {
    if (args.chunks().size() == 1) {
      context.sendMessage("&b1&a chunk has been restored");
    } else {
      context.sendMessage("&b%s&a chunks have been restored", args.chunks().size());
    }
  }

}
