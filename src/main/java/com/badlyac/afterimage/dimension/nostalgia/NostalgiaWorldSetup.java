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
        BlockPos dimensionSpawn = level.getSharedSpawnPos();
        int groundY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, dimensionSpawn.getX(), dimensionSpawn.getZ());
        // groundY 是地面最高實體方塊上方的第一格空氣，-1 讓結構 Y=0 層與地表齊平
        BlockPos structureOrigin = new BlockPos(dimensionSpawn.getX(), groundY - 1, dimensionSpawn.getZ());

        if (!structurePlaced) {
            AfterimageStructureLoader.replace(
                    level, structureOrigin, "nostalgic_house", new StructurePlaceSettings());
            structurePlaced = true;
        }

        level.setDefaultSpawnPos(structureOrigin.offset(HOUSE_RELATIVE_SPAWN), 0F);
    }
}
