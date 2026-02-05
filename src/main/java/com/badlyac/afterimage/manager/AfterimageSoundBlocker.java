package com.badlyac.afterimage.manager;

import com.badlyac.afterimage.AfterimageMod;
import net.minecraftforge.client.event.sound.PlaySoundSourceEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber(modid = AfterimageMod.MOD_ID)
public class AfterimageSoundBlocker {

    @SubscribeEvent
    public static void onPlaySoundAtEntity(PlaySoundSourceEvent event) {

    }
}
