package com.badlyac.afterimage.client;

import com.badlyac.afterimage.AfterimageMod;
import com.badlyac.afterimage.registry.ModDimensions;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
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
        AfterimageClient.syncPaleMimicNoise(mc);
        AfterimageClient.syncPaleMimicPlainState(
                mc.level != null && mc.level.dimension() == ModDimensions.PALE_MIMIC_PLAIN_LEVEL
        );
    }

    @SubscribeEvent
    public static void onMovementInputUpdate(MovementInputUpdateEvent event) {
        AfterimageClient.lockPaleMimicCaptureInput(event.getInput());
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        AfterimageClient.renderPaleMimicHud(event.getGuiGraphics(), event.getPartialTick());
    }
}
