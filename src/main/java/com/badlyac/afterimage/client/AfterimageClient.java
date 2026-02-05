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

    private static boolean enabled = false;
    private static boolean clientInAfterimage = false;

//    private static float grayStrength = 1.0F;
//    private static float vignetteStrength = 0.6F;
//    private static float darkness = 0.15F;

    //    public static void setEffectStrength(float gray, float vignette, float dark) {
//        grayStrength = gray;
//        vignetteStrength = vignette;
//        darkness = dark;
//    }

    public static void enableEffect() {
        if (enabled) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        enabled = true;
        mc.execute(() -> {
            mc.gameRenderer.loadEffect(EFFECT);
        });
    }

    public static void disableEffect() {
        if (!enabled) return;

        Minecraft mc = Minecraft.getInstance();
        enabled = false;
        mc.execute(mc.gameRenderer::shutdownEffect);
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void syncAfterimageState(boolean afterimage) {
        if (clientInAfterimage == afterimage) return;

        clientInAfterimage = afterimage;

        if (afterimage) {
            enableEffect();
        } else {
            disableEffect();
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null && enabled) {
            disableEffect();
        }
    }
}
