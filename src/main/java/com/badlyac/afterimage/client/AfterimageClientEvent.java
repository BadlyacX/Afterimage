package com.badlyac.afterimage.client;

import com.badlyac.afterimage.AfterimageMod;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.badlyac.afterimage.state.AfterimageState.isInAfterimage;

@Mod.EventBusSubscriber(modid = AfterimageMod.MOD_ID, value = Dist.CLIENT)
public class AfterimageClientEvent {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        AfterimageClient.syncAfterimageState(isInAfterimage(mc.player));
    }
}
