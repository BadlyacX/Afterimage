package com.badlyac.afterimage.dimension.tornexpanse;

import com.badlyac.afterimage.AfterimageMod;
import com.badlyac.afterimage.registry.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AfterimageMod.MOD_ID)
public final class TornExpanseWorldSetup {

    public static final BlockPos SPAWN = new BlockPos(0, 64, 0);
    private static final int PLATFORM_RADIUS = 4;
    private static final int PLATFORM_CLEARANCE = 4;

    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getTo() != ModDimensions.TORN_EXPANSE_LEVEL) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        applyTo(player.serverLevel());
    }

    /**
     * 終界雜訊地形在原點附近不保證有實體地塊，所以強制鋪一塊穩定的破碎平台，
     * 不依賴自然生成的浮島剛好落在重生點。
     */
    public static void applyTo(ServerLevel level) {
        ensurePlatform(level);
        level.setDefaultSpawnPos(SPAWN, 0F);
    }

    private static void ensurePlatform(ServerLevel level) {
        int floorY = SPAWN.getY() - 1;

        for (int dx = -PLATFORM_RADIUS; dx <= PLATFORM_RADIUS; dx++) {
            for (int dz = -PLATFORM_RADIUS; dz <= PLATFORM_RADIUS; dz++) {
                BlockPos floorPos = new BlockPos(SPAWN.getX() + dx, floorY, SPAWN.getZ() + dz);
                if (level.getBlockState(floorPos).getBlock() != Blocks.END_STONE) {
                    level.setBlock(floorPos, Blocks.END_STONE.defaultBlockState(), 3);
                }

                for (int dy = 0; dy < PLATFORM_CLEARANCE; dy++) {
                    BlockPos airPos = new BlockPos(SPAWN.getX() + dx, SPAWN.getY() + dy, SPAWN.getZ() + dz);
                    if (!level.getBlockState(airPos).isAir()) {
                        level.setBlock(airPos, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }
    }
}
