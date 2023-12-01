package me.whizvox.infiniplots.compat.worldguard.impl;

import me.whizvox.infiniplots.compat.worldguard.WorldGuardWrapper;

//
// Continue working on this at a later time...
//
public class WorldGuardWrapperImpl extends WorldGuardWrapper {

  /*private static final String GLOBAL_REGION_ID = "g_plotworld";

  private final WorldGuard wg;

  // cache
  private final Map<UUID, RegionManager> managers;

  public WorldGuardWrapperImpl() {
    wg = WorldGuard.getInstance();
    managers = new HashMap<>();
  }

  private static String getPlotRegionId(int plotNumber) {
    return "plot" + plotNumber;
  }

  @Nullable
  private RegionManager getRegionManager(World world) {
    return managers.computeIfAbsent(world.getUID(), uuid -> wg.getPlatform().getRegionContainer().get(new BukkitWorld(world)));
  }

  @Nullable
  private ProtectedRegion getRegion(PlotWorld plotWorld, int plotNumber) {
    RegionManager rm = getRegionManager(plotWorld.world);
    if (rm == null) {
      return null;
    }
    ProtectedRegion region;
    if (plotNumber < 1) {
      region = rm.getRegion(GLOBAL_REGION_ID);
      if (region == null) {
        region = new GlobalProtectedRegion(GLOBAL_REGION_ID);
        rm.addRegion(region);
      }
    } else {
      String regionId = getPlotRegionId(plotNumber);
      region = rm.getRegion(regionId);
      if (region == null) {
        ChunkPos p1 = plotWorld.generator.getPosition(plotNumber);
        ChunkPos p2 = new ChunkPos(p1.x() + plotWorld.generator.plotWidth, p1.z() + plotWorld.generator.plotDepth);
        ChunkRegion cRegion = new ChunkRegion(p1, p2);
        region = new ProtectedCuboidRegion(
            regionId,
            BlockVector3.at(cRegion.firstCornerX(), -2048, cRegion.firstCornerZ()),
            BlockVector3.at(cRegion.secondCornerX(), 2047, cRegion.secondCornerZ())
        );
        updateEditors_do(plotWorld, plotNumber, region);
        region.setFlag(Flags.BUILD, StateFlag.State.ALLOW);
        rm.addRegion(region);
      }
    }
    return region;
  }

  private void updateEditors_do(PlotWorld plotWorld, int plotNumber, ProtectedRegion region) {
    DefaultDomain members = new DefaultDomain();
    plotWorld.getAllEditors().getOrDefault(plotNumber, Set.of()).forEach(members::addPlayer);
    if (members.size() > 0) {
      region.setMembers(members);
    }
  }

  private static <V> void parseAndSetFlag(Flag<V> flag, String input, ProtectedRegion region) {
    try {
      V value = flag.parseInput(FlagContext.create().setInput(input).setObject("region", region).build());
      region.setFlag(flag, value);
    } catch (InvalidFlagFormat e) {
      InfiniPlots.getInstance().getLogger().log(Level.WARNING, "Could not set WorldGuard flag " + flag.getName(), e);
    }
  }

  @Override
  public void updateEditors(PlotWorld plotWorld, int plotNumber) {
    ProtectedRegion region = getRegion(plotWorld, plotNumber);
    if (region == null) {
      InfiniPlots.getInstance().getLogger().warning("Attempted to update editors for an invalid plot world: " + plotWorld.world.getName());
      return;
    }
    updateEditors_do(plotWorld, plotNumber, region);
  }

  @Override
  public void setFlag(PlotWorld plotWorld, int plotNumber, String flag, @Nullable String value) {
    ProtectedRegion region = getRegion(plotWorld, plotNumber);
    if (region == null) {
      InfiniPlots.getInstance().getLogger().warning("Invalid world: " + plotWorld.name + " (UID: " + plotWorld.world.getUID() + ")");
      return;
    }
    Flag<?> foundFlag = wg.getFlagRegistry().get(flag);
    if (foundFlag == null) {
      InfiniPlots.getInstance().getLogger().warning("Invalid WorldGuard flag: " + flag);
      return;
    }
    parseAndSetFlag(foundFlag, value, region);
  }*/

}
