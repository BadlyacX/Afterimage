package com.badlyac.afterimage.dimension.tornexpanse;

import com.badlyac.afterimage.AfterimageMod;
import com.badlyac.afterimage.registry.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AfterimageMod.MOD_ID)
public final class TornExpanseWorldSetup {

    // XZ 參考點；Y 由地表高度圖在執行期動態決定，不硬編碼。
    public static final BlockPos SPAWN = new BlockPos(0, 0, 0);

    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getTo() != ModDimensions.TORN_EXPANSE_LEVEL) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        applyTo(player.serverLevel());
    }

    public static void applyTo(ServerLevel level) {
        int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, SPAWN.getX(), SPAWN.getZ());
        BlockPos spawnPos = new BlockPos(SPAWN.getX(), Math.max(surfaceY, level.getMinBuildHeight()), SPAWN.getZ());
        level.setDefaultSpawnPos(spawnPos, 0F);
    }
}
