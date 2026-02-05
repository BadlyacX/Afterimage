package com.badlyac.afterimage.manager;

import com.badlyac.afterimage.AfterimageMod;
import com.badlyac.afterimage.state.AfterimageState;
import com.badlyac.afterimage.util.AfterimagePhaseUtil;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AfterimageMod.MOD_ID)
public class AfterimageItemPhaseHandler {

    @SubscribeEvent
    public static void onItemToss(ItemTossEvent event) {
        if (!AfterimageState.isInAfterimage(event.getPlayer())) return;

        ItemEntity item = event.getEntity();
        AfterimagePhaseUtil.markAfterimage(item);
    }
}
