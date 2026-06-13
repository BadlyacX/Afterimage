package com.badlyac.afterimage.client;

import com.badlyac.afterimage.AfterimageMod;
import com.badlyac.afterimage.monster.palemimic.PaleMimicEntity;
import com.badlyac.afterimage.network.AfterimageNetwork;
import com.badlyac.afterimage.network.PaleMimicBlackoutReadyPacket;
import com.badlyac.afterimage.registry.ModDimensions;
import com.badlyac.afterimage.registry.ModSounds;
import com.badlyac.afterimage.util.PostProcessEffectUtil;
import com.mojang.blaze3d.shaders.AbstractUniform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.mojang.math.Axis;

@Mod.EventBusSubscriber(
        modid = AfterimageMod.MOD_ID,
        value = Dist.CLIENT
)
public class AfterimageClient {

    private static final ResourceLocation EFFECT =
            ResourceLocation.fromNamespaceAndPath(AfterimageMod.MOD_ID, "shaders/post/afterimage.json");
    private static final double PALE_MIMIC_NOISE_RANGE = 20.0D;
    private static final double PALE_MIMIC_HEARTBEAT_RANGE = 32.0D;
    private static final int PALE_MIMIC_HEARTBEAT_FAR_TICKS = 35;
    private static final int PALE_MIMIC_HEARTBEAT_NEAR_TICKS = 6;
    private static final int CAPTURE_LOOK_TICKS = 60;
    private static final int CAPTURE_ROLL_TICKS = 2;
    private static final int CAPTURE_POST_ROLL_WAIT_TICKS = 6;
    private static final int CAPTURE_HOLD_BLACK_TICKS = 50;
    private static final int CAPTURE_FADE_OUT_TICKS = 20;
    private static final int CAPTURE_TOTAL_TICKS =
            CAPTURE_LOOK_TICKS
                    + CAPTURE_ROLL_TICKS
                    + CAPTURE_POST_ROLL_WAIT_TICKS
                    + CAPTURE_HOLD_BLACK_TICKS
                    + CAPTURE_FADE_OUT_TICKS;
    private static final int RADIO_STATIC_CHECK_INTERVAL_TICKS = 60 * 20;
    private static final int RADIO_STATIC_DURATION_TICKS = 80;
    private static final float RADIO_STATIC_CHANCE = 0.5F;
    private static final float RADIO_STATIC_NOISE_INTENSITY = 1.0F;

    private static boolean clientInAfterimage = false;
    private static boolean clientInPaleMimicPlain = false;
    private static float paleMimicNoiseIntensity = 0.0F;
    private static int paleMimicCaptureTicks = 0;
    private static Vec3 paleMimicCaptureTarget = Vec3.ZERO;
    private static float paleMimicCaptureStartYRot = 0.0F;
    private static float paleMimicCaptureStartXRot = 0.0F;
    private static boolean paleMimicCaptureSoundPlayed = false;
    private static boolean paleMimicBlackoutReadySent = false;
    private static int paleMimicCaughtTitleTicks = 0;
    private static int paleMimicHeartbeatCooldown = 0;
    private static int radioStaticAttemptCooldown = RADIO_STATIC_CHECK_INTERVAL_TICKS;
    private static int radioStaticTicks = 0;

    private static final RandomSource RANDOM = RandomSource.create();

    public static boolean isEnabled() {
        return PostProcessEffectUtil.isEnabled();
    }

    public static void syncAfterimageState(boolean afterimage) {
        if (clientInAfterimage == afterimage) return;

        clientInAfterimage = afterimage;
        updateEffectState();
    }

    public static void syncPaleMimicNoise(Minecraft mc) {
        float intensity = getPaleMimicNoiseIntensity(mc);

        if (Math.abs(paleMimicNoiseIntensity - intensity) < 0.001F) {
            return;
        }

        paleMimicNoiseIntensity = intensity;
        updateEffectState();
        applyEffectUniforms();
    }

    public static void startPaleMimicCapture(Vec3 target) {
        Minecraft mc = Minecraft.getInstance();

        paleMimicCaptureTarget = target;
        paleMimicCaptureTicks = CAPTURE_TOTAL_TICKS;
        paleMimicCaptureSoundPlayed = false;
        paleMimicBlackoutReadySent = false;
        paleMimicCaughtTitleTicks = 16;

        if (mc.player != null) {
            paleMimicCaptureStartYRot = mc.player.getYRot();
            paleMimicCaptureStartXRot = mc.player.getXRot();
        }

        updateEffectState();
        applyEffectUniforms();
    }

    public static void lockPaleMimicCaptureInput(Input input) {
        if (!isPaleMimicCaptureActive()) return;

        input.leftImpulse = 0.0F;
        input.forwardImpulse = 0.0F;
        input.up = false;
        input.down = false;
        input.left = false;
        input.right = false;
        input.jumping = false;
        input.shiftKeyDown = false;
    }

    public static void renderPaleMimicHud(GuiGraphics guiGraphics, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (paleMimicCaughtTitleTicks > 0) {
            renderPaleMimicTitle(
                    guiGraphics,
                    mc.font,
                    "I...GOT....YOU.....",
                    0xFFFF1010,
                    2.25F,
                    true,
                    partialTick
            );
            return;
        }

        if (!isPaleMimicCaptureActive() && paleMimicNoiseIntensity > 0.0F) {
            renderPaleMimicTitle(
                    guiGraphics,
                    mc.font,
                    "RUN",
                    0xFFFF0000,
                    4.75F,
                    true,
                    partialTick
            );
        }
    }

    private static boolean isPaleMimicCaptureActive() {
        return paleMimicCaptureTicks > 0;
    }

    private static float getPaleMimicNoiseIntensity(Minecraft mc) {
        if (mc.level == null || mc.player == null) return 0.0F;

        double closestDistanceSqr = PALE_MIMIC_NOISE_RANGE * PALE_MIMIC_NOISE_RANGE;

        for (PaleMimicEntity mimic : mc.level.getEntitiesOfClass(
                PaleMimicEntity.class,
                mc.player.getBoundingBox().inflate(PALE_MIMIC_NOISE_RANGE),
                mimic -> mimic.isAggressive()
                        && mimic.getTargetPlayerId()
                        .map(mc.player.getUUID()::equals)
                        .orElse(false)
        )) {
            closestDistanceSqr = Math.min(closestDistanceSqr, mimic.distanceToSqr(mc.player));
        }

        double distance = Math.sqrt(closestDistanceSqr);
        double closeness = 1.0D - Math.min(distance / PALE_MIMIC_NOISE_RANGE, 1.0D);
        return (float) (closeness * closeness * 3);
    }

    private static void updateEffectState() {
        boolean needsEffect = clientInAfterimage || clientInPaleMimicPlain
                || paleMimicNoiseIntensity > 0.0F || isPaleMimicCaptureActive() || isRadioStaticActive();
        if (needsEffect) {
            PostProcessEffectUtil.load(EFFECT);
        } else {
            PostProcessEffectUtil.disable();
        }
    }

    public static void syncPaleMimicPlainState(boolean inPaleMimicPlain) {
        if (clientInPaleMimicPlain == inPaleMimicPlain) return;
        clientInPaleMimicPlain = inPaleMimicPlain;
        updateEffectState();
    }

    private static void applyEffectUniforms() {
        PostProcessEffectUtil.applyToPasses(EFFECT, pass -> {
            // 後處理鏈中的每個 pass 只宣告自己用到的 uniform，
            // safeGetUniform 對沒有該 uniform 的 pass 會回傳無作用的 dummy，
            // 因此這裡逐一設定即可，數值會各自落到對應的效果 pass 上。
            // 兩個維度的效果互斥：pale_mimic_plain 只顯示 bumpy，afterimage 只顯示灰階
            // 加上顯式 !other 條件，避免狀態切換過程中兩者同時為 true 導致效果疊加
            AbstractUniform bumpyAmount = pass.getEffect().safeGetUniform("BumpyAmount");
            bumpyAmount.set(clientInPaleMimicPlain && !clientInAfterimage ? 1.0F : 0.0F);

            AbstractUniform fogAmount = pass.getEffect().safeGetUniform("FogAmount");
            fogAmount.set(clientInPaleMimicPlain && !clientInAfterimage ? 1.0F : 0.0F);

            AbstractUniform grayAmount = pass.getEffect().safeGetUniform("GrayAmount");
            grayAmount.set(clientInAfterimage && !clientInPaleMimicPlain ? 1.0F : 0.0F);

            // 暗角跟著灰階走，條件與 GrayAmount 相同
            AbstractUniform vignetteAmount = pass.getEffect().safeGetUniform("VignetteAmount");
            vignetteAmount.set(clientInAfterimage && !clientInPaleMimicPlain ? 1.0F : 0.0F);

            AbstractUniform time = pass.getEffect().safeGetUniform("Time");
            time.set(getShaderTime());

            AbstractUniform noiseIntensity = pass.getEffect().safeGetUniform("NoiseIntensity");
            noiseIntensity.set(Math.max(paleMimicNoiseIntensity, getRadioStaticNoiseIntensity()));

            AbstractUniform screenRoll = pass.getEffect().safeGetUniform("ScreenRoll");
            screenRoll.set(getPaleMimicCaptureRoll());

            AbstractUniform blackout = pass.getEffect().safeGetUniform("Blackout");
            blackout.set(getPaleMimicCaptureBlackout());
        });
    }

    private static float getPaleMimicCaptureRoll() {
        if (!isPaleMimicCaptureActive()) return 0.0F;

        int elapsed = CAPTURE_TOTAL_TICKS - paleMimicCaptureTicks;
        float progress = Mth.clamp((elapsed - CAPTURE_LOOK_TICKS) / (float) CAPTURE_ROLL_TICKS, 0.0F, 1.0F);
        return smooth(progress) * Mth.HALF_PI;
    }

    private static float getPaleMimicCaptureBlackout() {
        if (!isPaleMimicCaptureActive()) return 0.0F;

        int elapsed = CAPTURE_TOTAL_TICKS - paleMimicCaptureTicks;
        int blackoutStart = CAPTURE_LOOK_TICKS + CAPTURE_ROLL_TICKS + CAPTURE_POST_ROLL_WAIT_TICKS;
        int fadeOutStart = blackoutStart + CAPTURE_HOLD_BLACK_TICKS;

        if (elapsed < blackoutStart) return 0.0F;
        if (elapsed >= fadeOutStart) {
            float progress = Mth.clamp((elapsed - fadeOutStart) / (float) CAPTURE_FADE_OUT_TICKS, 0.0F, 1.0F);
            return 1.0F - smooth(progress);
        }

        return 1.0F;
    }

    private static float smooth(float value) {
        return value * value * (3.0F - 2.0F * value);
    }

    private static float getShaderTime() {
        LocalPlayer player = Minecraft.getInstance().player;
        return player == null ? 0.0F : player.tickCount / 20.0F;
    }

    private static boolean isRadioStaticActive() {
        return radioStaticTicks > 0;
    }

    private static float getRadioStaticNoiseIntensity() {
        if (!isRadioStaticActive()) return 0.0F;

        float fade = Mth.clamp(radioStaticTicks / 20.0F, 0.0F, 1.0F);
        return RADIO_STATIC_NOISE_INTENSITY * fade;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null) {
            clientInAfterimage = false;
            clientInPaleMimicPlain = false;
            paleMimicNoiseIntensity = 0.0F;
            paleMimicCaptureTicks = 0;
            paleMimicCaptureSoundPlayed = false;
            paleMimicBlackoutReadySent = false;
            paleMimicCaughtTitleTicks = 0;
            paleMimicHeartbeatCooldown = 0;
            radioStaticAttemptCooldown = RADIO_STATIC_CHECK_INTERVAL_TICKS;
            radioStaticTicks = 0;
            if (PostProcessEffectUtil.isEnabled()) PostProcessEffectUtil.disable();
            return;
        }

        if (paleMimicCaughtTitleTicks > 0) {
            paleMimicCaughtTitleTicks--;
        }

        tickPaleMimicCapture(mc.player);
        tickPaleMimicHeartbeat(mc);
        tickRadioStatic(mc);
        updateEffectState();

        if (PostProcessEffectUtil.isEnabled()) {
            PostProcessEffectUtil.ensureLoadedEffectIsActive();
            applyEffectUniforms();
        }
    }

    private static void tickPaleMimicCapture(LocalPlayer player) {
        if (!isPaleMimicCaptureActive()) return;

        int elapsed = CAPTURE_TOTAL_TICKS - paleMimicCaptureTicks;
        facePaleMimicCaptureTarget(player, elapsed);

        if (!paleMimicCaptureSoundPlayed && elapsed >= CAPTURE_LOOK_TICKS) {
            paleMimicCaptureSoundPlayed = true;
            Minecraft.getInstance().getSoundManager().play(
                    SimpleSoundInstance.forUI(
                            ModSounds.NECK_BONE_FRACTURE.get(),
                            1.0F
                    )
            );
        }

        if (!paleMimicBlackoutReadySent && isPaleMimicCaptureBlackoutReady(elapsed)) {
            paleMimicBlackoutReadySent = true;
            AfterimageNetwork.CHANNEL.sendToServer(new PaleMimicBlackoutReadyPacket());
        }

        player.setDeltaMovement(Vec3.ZERO);
        paleMimicCaptureTicks--;
    }

    private static void tickRadioStatic(Minecraft mc) {
        if (!isInAfterimageDimension(mc)) {
            radioStaticAttemptCooldown = RADIO_STATIC_CHECK_INTERVAL_TICKS;
            radioStaticTicks = 0;
            return;
        }

        if (radioStaticTicks > 0) {
            radioStaticTicks--;
        }

        if (radioStaticAttemptCooldown > 0) {
            radioStaticAttemptCooldown--;
            return;
        }

        radioStaticAttemptCooldown = RADIO_STATIC_CHECK_INTERVAL_TICKS;
        if (RANDOM.nextFloat() >= RADIO_STATIC_CHANCE) return;

        radioStaticTicks = RADIO_STATIC_DURATION_TICKS;
        mc.getSoundManager().play(
                SimpleSoundInstance.forUI(
                        ModSounds.RADIO_STATIC.get(),
                        1.0F,
                        0.9F
                )
        );
    }

    private static boolean isInAfterimageDimension(Minecraft mc) {
        return mc.level != null && mc.level.dimension() == ModDimensions.AFTERIMAGE_LEVEL;
    }

    private static void tickPaleMimicHeartbeat(Minecraft mc) {
        if (isPaleMimicCaptureRollComplete()) {
            paleMimicHeartbeatCooldown = 0;
            return;
        }

        float closeness = getPaleMimicHeartbeatCloseness(mc);
        if (closeness <= 0.0F) {
            paleMimicHeartbeatCooldown = 0;
            return;
        }

        if (paleMimicHeartbeatCooldown > 0) {
            paleMimicHeartbeatCooldown--;
            return;
        }

        float volume = Mth.lerp(closeness, 0.45F, 1.0F);
        mc.getSoundManager().play(
                SimpleSoundInstance.forUI(
                        ModSounds.HEART_BEAT.get(),
                        1.0F,
                        volume
                )
        );

        paleMimicHeartbeatCooldown = getPaleMimicHeartbeatInterval(closeness);
    }

    private static float getPaleMimicHeartbeatCloseness(Minecraft mc) {
        if (mc.level == null || mc.player == null) return 0.0F;

        double closestDistanceSqr = PALE_MIMIC_HEARTBEAT_RANGE * PALE_MIMIC_HEARTBEAT_RANGE;
        boolean foundAggressiveMimic = false;

        for (PaleMimicEntity mimic : mc.level.getEntitiesOfClass(
                PaleMimicEntity.class,
                mc.player.getBoundingBox().inflate(PALE_MIMIC_HEARTBEAT_RANGE),
                mimic -> mimic.isAggressive()
                        && mimic.getTargetPlayerId()
                        .map(mc.player.getUUID()::equals)
                        .orElse(false)
        )) {
            foundAggressiveMimic = true;
            closestDistanceSqr = Math.min(closestDistanceSqr, mimic.distanceToSqr(mc.player));
        }

        if (!foundAggressiveMimic) return 0.0F;

        double distance = Math.sqrt(closestDistanceSqr);
        double closeness = 1.0D - Math.min(distance / PALE_MIMIC_HEARTBEAT_RANGE, 1.0D);
        return (float) Math.max(closeness, 0.05D);
    }

    private static int getPaleMimicHeartbeatInterval(float closeness) {
        float easedCloseness = closeness * closeness;
        return Math.max(
                PALE_MIMIC_HEARTBEAT_NEAR_TICKS,
                Math.round(Mth.lerp(
                        easedCloseness,
                        PALE_MIMIC_HEARTBEAT_FAR_TICKS,
                        PALE_MIMIC_HEARTBEAT_NEAR_TICKS
                ))
        );
    }

    private static boolean isPaleMimicCaptureRollComplete() {
        if (!isPaleMimicCaptureActive()) return false;

        int elapsed = CAPTURE_TOTAL_TICKS - paleMimicCaptureTicks;
        return elapsed >= CAPTURE_LOOK_TICKS + CAPTURE_ROLL_TICKS;
    }

    private static boolean isPaleMimicCaptureBlackoutReady(int elapsed) {
        int blackoutStart = CAPTURE_LOOK_TICKS + CAPTURE_ROLL_TICKS + CAPTURE_POST_ROLL_WAIT_TICKS;
        return elapsed > blackoutStart;
    }

    private static void facePaleMimicCaptureTarget(LocalPlayer player, int elapsed) {
        Vec3 eye = player.getEyePosition();
        Vec3 delta = paleMimicCaptureTarget.subtract(eye);
        double horizontalDistance = Math.sqrt(delta.x * delta.x + delta.z * delta.z);

        float targetYRot = (float) (Mth.atan2(delta.z, delta.x) * Mth.RAD_TO_DEG) - 90.0F;
        float targetXRot = (float) -(Mth.atan2(delta.y, horizontalDistance) * Mth.RAD_TO_DEG);

        float progress = Mth.clamp(elapsed / (float) CAPTURE_LOOK_TICKS, 0.0F, 1.0F);
        float eased = smooth(progress);
        float yRot = lerpAngle(paleMimicCaptureStartYRot, targetYRot, eased);
        float xRot = Mth.lerp(eased, paleMimicCaptureStartXRot, targetXRot);

        player.setYRot(yRot);
        player.setXRot(xRot);
        player.setYHeadRot(yRot);
    }

    private static float lerpAngle(float from, float to, float amount) {
        return from + Mth.wrapDegrees(to - from) * amount;
    }

    private static void renderPaleMimicTitle(
            GuiGraphics guiGraphics,
            Font font,
            String text,
            int color,
            float scale,
            boolean violentShake,
            float partialTick
    ) {
        Minecraft mc = Minecraft.getInstance();
        float time = mc.player == null ? partialTick : mc.player.tickCount + partialTick;
        float intensity = violentShake ? 1.0F : 0.35F;
        float shakeX = (Mth.sin(time * 3.7F) * 5.0F + Mth.sin(time * 13.1F) * 2.5F) * intensity;
        float shakeY = (Mth.cos(time * 4.9F) * 3.5F + Mth.sin(time * 17.3F) * 2.0F) * intensity;
        float rotation = (Mth.sin(time * 8.7F) * 3.0F + Mth.cos(time * 19.0F) * 1.2F) * intensity;

        int centerX = guiGraphics.guiWidth() / 2;
        int centerY = guiGraphics.guiHeight() / 3;
        int textWidth = font.width(text);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(centerX + shakeX, centerY + shakeY, 0.0F);
        guiGraphics.pose().mulPose(Axis.ZP.rotationDegrees(rotation));
        guiGraphics.pose().scale(scale, scale, 1.0F);

        drawCenteredTitleLayer(guiGraphics, font, text, textWidth, -9, -5, 0x55FF0000);
        drawCenteredTitleLayer(guiGraphics, font, text, textWidth, 8, 4, 0x55B00000);
        drawCenteredTitleLayer(guiGraphics, font, text, textWidth, -3, 2, 0xAA5A0000);
        drawCenteredTitleLayer(guiGraphics, font, text, textWidth, 0, 0, color);

        guiGraphics.pose().popPose();
    }

    private static void drawCenteredTitleLayer(
            GuiGraphics guiGraphics,
            Font font,
            String text,
            int textWidth,
            int xOffset,
            int yOffset,
            int color
    ) {
        guiGraphics.drawString(
                font,
                text,
                -textWidth / 2 + xOffset,
                yOffset,
                color,
                false
        );
    }
}
