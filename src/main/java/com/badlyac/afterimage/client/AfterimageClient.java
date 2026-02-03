package com.badlyac.afterimage.client;

import com.badlyac.afterimage.AfterimageMod;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AfterimageMod.MOD_ID, value = Dist.CLIENT)
public class AfterimageClient {

    private static final ResourceLocation EFFECT =
            ResourceLocation.fromNamespaceAndPath(AfterimageMod.MOD_ID, "shaders/post/afterimage.json");

    private static float grayStrength = 1.0F;
    private static float vignetteStrength = 0.6F;
    private static float darkness = 0.15F;

    private static boolean enabled = false;
    private static boolean testApplied = false;

    public static void enableEffect() {
        if (enabled) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        enabled = true;
        mc.gameRenderer.loadEffect(EFFECT);
    }

    public static void disableEffect() {
        if (!enabled) return;

        Minecraft mc = Minecraft.getInstance();
        enabled = false;
        mc.gameRenderer.shutdownEffect();
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEffectStrength(float gray, float vignette, float dark) {
        grayStrength = gray;
        vignetteStrength = vignette;
        darkness = dark;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null) {
            if (enabled) disableEffect();
            testApplied = false;
            return;
        }

        if (!testApplied) {
            enableEffect();
            testApplied = true;
        }
    }


//    @SubscribeEvent
//    public static void onClientTick(TickEvent.ClientTickEvent e) {
//        if (e.phase != TickEvent.Phase.END) return;
//
//        Minecraft mc = Minecraft.getInstance();
//        if (mc.player == null && enabled) {
//            disableEffect();
//        }
//    }
}
