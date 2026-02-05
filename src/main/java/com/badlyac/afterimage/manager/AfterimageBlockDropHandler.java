package com.badlyac.afterimage.manager;

import com.badlyac.afterimage.AfterimageMod;
import com.badlyac.afterimage.state.AfterimageState;
import com.badlyac.afterimage.util.AfterimagePhaseUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AfterimageMod.MOD_ID)
public class AfterimageBlockDropHandler {

    @SubscribeEvent
    public static void onBlockDrops(LivingDropsEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        if (!AfterimageState.isInAfterimage(player)) return;

        for (ItemEntity item : event.getDrops()) {
            AfterimagePhaseUtil.markAfterimage(item);
        }
    }
}