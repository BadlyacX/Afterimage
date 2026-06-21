package com.badlyac.afterimage.dimension.nostalgia;

import com.badlyac.afterimage.AfterimageMod;
import com.badlyac.afterimage.registry.registries.ModDimensions;
import com.badlyac.afterimage.util.AfterimageStructureLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AfterimageMod.MOD_ID)
public final class NostalgiaWorldSetup {

    private static final BlockPos HOUSE_RELATIVE_SPAWN = new BlockPos(4, 2, 4);
    private static boolean structurePlaced = false;
    private static BlockPos cachedSpawnPos = null;

    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getTo() != ModDimensions.NOSTALGIA_LEVEL) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ServerLevel level = player.serverLevel();
        applyTo(level, player.blockPosition());
        BlockPos spawn = level.getSharedSpawnPos();
        player.teleportTo(spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5);
        player.resetFallDistance();
    }

    public static void applyTo(ServerLevel level, BlockPos playerPos) {
        if (!structurePlaced) {
            int houseX = playerPos.getX() - HOUSE_RELATIVE_SPAWN.getX();
            int houseZ = playerPos.getZ() - HOUSE_RELATIVE_SPAWN.getZ();
            int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, houseX, houseZ);
            BlockPos structureOrigin = new BlockPos(houseX, surfaceY, houseZ);
            cachedSpawnPos = structureOrigin.offset(HOUSE_RELATIVE_SPAWN);

            AfterimageStructureLoader.replace(
                    level, structureOrigin, "nostalgic_house", new StructurePlaceSettings());
            structurePlaced = true;
        }

        if (cachedSpawnPos != null) {
            level.setDefaultSpawnPos(cachedSpawnPos, 0F);
        }
    }
}
