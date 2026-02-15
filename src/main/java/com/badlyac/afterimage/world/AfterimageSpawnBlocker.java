package com.badlyac.afterimage.world;

import com.badlyac.afterimage.AfterimageMod;
import com.badlyac.afterimage.registry.ModDimensions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AfterimageMod.MOD_ID)
public class AfterimageSpawnBlocker {

    @SubscribeEvent
    public static void onFinalizeSpawn(MobSpawnEvent.FinalizeSpawn event) {

        if (!(event.getLevel() instanceof ServerLevel level)) return;

        if (!level.dimension().equals(ModDimensions.AFTERIMAGE_LEVEL)) return;

        EntityType<?> type = event.getEntity().getType();

        // white list
//        if (type == ModEntities.AFTERIMAGE_MONSTER.get()) {
//            return;
//        }

        event.setSpawnCancelled(true);
    }

    @SubscribeEvent
    public static void onJoinLevel(EntityJoinLevelEvent event) {

        Entity entity = event.getEntity();

        if (entity.level().dimension() != ModDimensions.AFTERIMAGE_LEVEL)
            return;

        if (entity instanceof Mob) {
            entity.discard();
        }
    }
}