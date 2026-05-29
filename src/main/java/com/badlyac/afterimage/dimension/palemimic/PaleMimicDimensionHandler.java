package com.badlyac.afterimage.dimension.palemimic;

import com.badlyac.afterimage.AfterimageMod;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AfterimageMod.MOD_ID)
public final class PaleMimicDimensionHandler {

    public PaleMimicDimensionHandler() {

    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {

    }

    @SubscribeEvent
    public static void onFinalizeSpawn(MobSpawnEvent.FinalizeSpawn event) {
        // Mobs are now allowed to spawn for the Silent Hill vibe
    }

    @SubscribeEvent
    public static void onJoinLevel(EntityJoinLevelEvent event) {
        // Mobs are now allowed to join for the Silent Hill vibe
    }


}
