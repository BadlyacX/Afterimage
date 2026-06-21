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

    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getTo() != ModDimensions.NOSTALGIA_LEVEL) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        applyTo(player.serverLevel());
    }

    public static void applyTo(ServerLevel level) {
        int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, 0, 0);
        BlockPos structureOrigin = new BlockPos(0, surfaceY, 0);
        BlockPos spawnPos = structureOrigin.offset(HOUSE_RELATIVE_SPAWN);

        if (!structurePlaced) {
            AfterimageStructureLoader.replace(
                    level, structureOrigin, "nostalgic_house", new StructurePlaceSettings());
            structurePlaced = true;
        }

        level.setDefaultSpawnPos(spawnPos, 0F);
    }
}
