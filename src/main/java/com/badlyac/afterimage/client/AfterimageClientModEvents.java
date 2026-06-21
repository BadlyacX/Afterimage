package com.badlyac.afterimage.client;

import com.badlyac.afterimage.AfterimageMod;
import com.badlyac.afterimage.monster.palemimic.PaleMimicRenderer;
import com.badlyac.afterimage.registry.registries.ModEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = AfterimageMod.MOD_ID,
        value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.MOD
)
public class AfterimageClientModEvents {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(
                ModEntities.PALE_MIMIC.get(),
                PaleMimicRenderer::new
        );
    }
}
