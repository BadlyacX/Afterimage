package com.badlyac.afterimage.dimension.nostalgia;

import com.badlyac.afterimage.AfterimageMod;
import com.badlyac.afterimage.registry.registries.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = AfterimageMod.MOD_ID)
public final class NostalgiaWorldSetup {

    private static final BlockPos SPAWN_XZ = new BlockPos(0, 0, 0);
    private static final int HOUSE_RADIUS = 10;
    private static boolean structurePlaced = false;

    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getTo() != ModDimensions.NOSTALGIA_LEVEL) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ServerLevel level = player.serverLevel();

        if (!structurePlaced) {
            System.out.println("[Nostalgia] onChangeDimension: player arrived, placing house near player");
            placeHouse(level, player.blockPosition());
            structurePlaced = true;
        }
    }

    public static void applyTo(ServerLevel level) {
        int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, SPAWN_XZ.getX(), SPAWN_XZ.getZ());
        BlockPos spawnPos = new BlockPos(SPAWN_XZ.getX(), Math.max(surfaceY, level.getMinBuildHeight()), SPAWN_XZ.getZ());
        level.setDefaultSpawnPos(spawnPos, 0F);
        System.out.println("[Nostalgia] applyTo: set spawn to " + spawnPos);
    }

    private static void placeHouse(ServerLevel level, BlockPos nearPos) {
        System.out.println("[Nostalgia] placeHouse: nearPos=" + nearPos);

        ResourceLocation id = ResourceLocation.fromNamespaceAndPath("afterimage", "nostalgic_house");
        Optional<StructureTemplate> opt = level.getStructureManager().get(id);
        System.out.println("[Nostalgia] template present: " + opt.isPresent());

        if (opt.isEmpty()) {
            System.err.println("[Nostalgia] FAILED: nostalgic_house.nbt not found via StructureTemplateManager");
            return;
        }

        StructureTemplate template = opt.get();
        Vec3i size = template.getSize();
        System.out.println("[Nostalgia] template size: " + size.getX() + "x" + size.getY() + "x" + size.getZ());

        if (size.getX() == 0 || size.getY() == 0 || size.getZ() == 0) {
            System.err.println("[Nostalgia] FAILED: template has zero-size, .nbt may be empty");
            return;
        }

        RandomSource random = RandomSource.create(level.getSeed());
        int offsetX = random.nextIntBetweenInclusive(-HOUSE_RADIUS, HOUSE_RADIUS);
        int offsetZ = random.nextIntBetweenInclusive(-HOUSE_RADIUS, HOUSE_RADIUS);
        int placeX = nearPos.getX() + offsetX;
        int placeZ = nearPos.getZ() + offsetZ;
        System.out.println("[Nostalgia] offset: (" + offsetX + ", " + offsetZ + "), target XZ: (" + placeX + ", " + placeZ + ")");

        level.getChunkAt(new BlockPos(placeX, 0, placeZ));

        int groundY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, placeX, placeZ);
        System.out.println("[Nostalgia] groundY: " + groundY + " (minBuild=" + level.getMinBuildHeight() + ")");

        if (groundY <= level.getMinBuildHeight()) {
            System.err.println("[Nostalgia] FAILED: groundY is at or below min build height, chunk may not be generated");
            return;
        }

        BlockPos origin = new BlockPos(placeX, groundY, placeZ);
        for (int dx = 0; dx < size.getX(); dx++) {
            for (int dz = 0; dz < size.getZ(); dz++) {
                BlockPos below = new BlockPos(placeX + dx, groundY - 1, placeZ + dz);
                if (level.getBlockState(below).isAir()) {
                    System.out.println("[Nostalgia] ground has air at " + below + ", shifting down");
                    int solidY = findSolidGround(level, placeX + dx, groundY - 1, placeZ + dz);
                    if (solidY < origin.getY() - 1) {
                        origin = new BlockPos(origin.getX(), solidY + 1, origin.getZ());
                    }
                }
            }
        }

        for (int dx = 0; dx < size.getX(); dx++) {
            for (int dz = 0; dz < size.getZ(); dz++) {
                BlockPos below = new BlockPos(origin.getX() + dx, origin.getY() - 1, origin.getZ() + dz);
                if (level.getBlockState(below).isAir()) {
                    level.setBlock(below, net.minecraft.world.level.block.Blocks.DIRT.defaultBlockState(), 2);
                }
            }
        }

        System.out.println("[Nostalgia] placing structure at: " + origin);

        boolean success = template.placeInWorld(
                level, origin, origin, new StructurePlaceSettings(), level.random, 2);
        System.out.println("[Nostalgia] placeInWorld result: " + success);
    }

    private static int findSolidGround(ServerLevel level, int x, int startY, int z) {
        for (int y = startY; y >= level.getMinBuildHeight(); y--) {
            if (!level.getBlockState(new BlockPos(x, y, z)).isAir()) {
                return y;
            }
        }
        return level.getMinBuildHeight();
    }
}
