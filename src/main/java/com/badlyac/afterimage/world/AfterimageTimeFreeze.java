package com.badlyac.afterimage.world;

import com.badlyac.afterimage.AfterimageMod;
import com.badlyac.afterimage.registry.ModDimensions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AfterimageMod.MOD_ID)
public class AfterimageTimeFreeze {

    @SubscribeEvent
    public static void onWorldTick(TickEvent.LevelTickEvent event) {

        if (event.level.isClientSide()) return;

        if (!(event.level instanceof ServerLevel level)) return;

        if (level.dimension() != ModDimensions.AFTERIMAGE_LEVEL) return;

        level.setDayTime(18000);
        level.setRainLevel(0);
        level.setThunderLevel(0);
        level.setWeatherParameters(0, 0, false, false);
    }

    @SubscribeEvent
    public static void onFluidFlow(BlockEvent.FluidPlaceBlockEvent event) {

        if (!(event.getLevel() instanceof Level level)) return;

        if (level.dimension() == ModDimensions.AFTERIMAGE_LEVEL) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onFireSpread(BlockEvent.NeighborNotifyEvent event) {

        if (!(event.getLevel() instanceof Level level)) return;

        if (level.dimension() == ModDimensions.AFTERIMAGE_LEVEL) {
            event.setCanceled(true);
        }
    }
}
