package com.badlyac.afterimage.monster.palemimic;

import com.badlyac.afterimage.network.PaleMimicCapturePacket;
import com.badlyac.afterimage.registry.ModSounds;
import com.badlyac.afterimage.util.Clock;
import com.badlyac.afterimage.util.AfterimageTeleportUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MimicReactionGoal extends Goal {

    private static final double AGGRESSIVE_SPEED = 1.65D;
    private static final double AGGRESSIVE_TICKS = Clock.SEC * 10;
    private static final double CAPTURE_DISTANCE = 1.5D;
    private static final int CAPTURE_LOOK_TICKS = (int) Clock.SEC * 3;
    private static final int CAPTURE_ROLL_TICKS = 2;
    private static final int CAPTURE_POST_ROLL_WAIT_TICKS = 6;
    private static final int CAPTURE_SOUND_TICK = CAPTURE_LOOK_TICKS;
    private static final int CAPTURE_BLACKOUT_TICK =
            CAPTURE_SOUND_TICK + CAPTURE_ROLL_TICKS + CAPTURE_POST_ROLL_WAIT_TICKS;
    private static final int CAPTURE_TELEPORT_FALLBACK_TICK =
            CAPTURE_BLACKOUT_TICK + (int) Clock.SEC * 3;
    private static final Map<UUID, MimicReactionGoal> ACTIVE_CAPTURES = new HashMap<>();

    private final PaleMimicEntity mimic;
    private ServerPlayer target;
    private boolean capturing;
    private int captureTicks;
    private boolean captureSoundPlayed;

    public MimicReactionGoal(PaleMimicEntity mimic) {
        this.mimic = mimic;
        this.setFlags(EnumSet.noneOf(Flag.class));
    }

    public static void finishCaptureFor(ServerPlayer player) {
        MimicReactionGoal capture = ACTIVE_CAPTURES.get(player.getUUID());
        if (capture != null && capture.isCapturing(player)) {
            capture.finishCapture();
        }
    }

    @Override
    public boolean canUse() {
        target = mimic.getTargetPlayer();
        return target != null && !target.isRemoved();
    }

    @Override
    public boolean canContinueToUse() {
        return target != null && !target.isRemoved();
    }

    @Override
    public void stop() {
        clearActiveCapture();
        stopWarningSound();
        capturing = false;
        captureTicks = 0;
        captureSoundPlayed = false;
        target = null;
    }

    @Override
    public void tick() {
        if (target == null) {
            stopWarning();
            return;
        }

        if (mimic.isAggressive()) {
            tickAggressive();
            return;
        }

        boolean seen = playerSeesMimic(target);

        if (seen) {
            mimic.setUnseenTicks(0);

            if (!mimic.isTriggered()) {
                triggerReaction();
            }

            return;
        }

        if (!mimic.isWarning() && !mimic.isTriggered()) {
            mimic.setUnseenTicks(
                    mimic.getUnseenTicks() + 1
            );
        }

        if (!mimic.isWarning()
                && mimic.getUnseenTicks() >= 200) {

            startWarning();
        }

        if (mimic.isWarning()) {

            mimic.setWarningTicks(
                    mimic.getWarningTicks() + 1
            );

            tickWarning();

            if (mimic.getWarningTicks() >= 300) {
                triggerReaction();
            }
        }

    }

    private boolean playerSeesMimic(ServerPlayer player) {
        Vec3 look = player.getLookAngle().normalize();

        Vec3 toMimic = mimic.position()
                .subtract(player.getEyePosition()).normalize();
        double dot = look.dot(toMimic);

        if (dot < 0.5) return false;

        ClipContext context = new ClipContext(
                player.getEyePosition(),
                mimic.getEyePosition(),
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                player
        );

        BlockHitResult result =
                mimic.level().clip(context);

        return result.getType() == HitResult.Type.MISS;
    }

    private void startWarning() {
        mimic.setUnseenTicks(0);

        mimic.setWarning(true);
        mimic.setWarningTicks(0);
    }

    private void tickWarning() {
        if (!(mimic.level() instanceof ServerLevel level)) return;

        if (mimic.getWarningTicks() % 20 != 0) return;

        float progress = mimic.getWarningTicks() / 300F;
        float volume = 0.2F + progress * 1.8F;

        level.playSound(
                null,
                mimic.blockPosition(),
                ModSounds.WHISPERING.get(),
                SoundSource.HOSTILE,
                volume,
                0.7F
        );
    }

    private void triggerReaction() {
        stopWarning();
        mimic.setTriggered(true);

        if (mimic.getRandom().nextFloat() < 0.7F) {
            disappear();
        } else {
            startAggressive();
        }
    }

    private void disappear() {

        if (!(mimic.level() instanceof ServerLevel level)) {
            mimic.discard();
            return;
        }

        level.sendParticles(
                ParticleTypes.LARGE_SMOKE,
                mimic.getX(),
                mimic.getY() + 1,
                mimic.getZ(),
                30,
                0.3,
                0.5,
                0.3,
                0.02
        );

        level.playSound(
                null,
                mimic.blockPosition(),
                ModSounds.EXIT_SOUND.get(),
                SoundSource.HOSTILE,
                1F,
                0.8F
        );

        mimic.discard();
    }

    private void startAggressive() {
        mimic.setWarning(false);
        mimic.setWarningTicks(0);

        mimic.setAggressive(true);
        mimic.setAggressiveTicks(0);
        mimic.getNavigation().stop();
    }

    private void stopWarning() {
        if (!mimic.isWarning()) return;

        mimic.setWarning(false);
        mimic.setWarningTicks(0);
        stopWarningSound();
    }

    private void stopWarningSound() {
        if (!(mimic.level() instanceof ServerLevel level)) return;

        ClientboundStopSoundPacket packet = new ClientboundStopSoundPacket(
                ModSounds.WHISPERING.get().getLocation(),
                SoundSource.HOSTILE
        );

        for (ServerPlayer player : level.players()) {
            player.connection.send(packet);
        }
    }

    private void tickAggressive() {
        if (capturing) {
            tickCapture();
            return;
        }

        mimic.setAggressiveTicks(
                mimic.getAggressiveTicks() + 1
        );

        if (mimic.distanceTo(target) < CAPTURE_DISTANCE) {
            startCapture();
            return;
        }

        mimic.getNavigation().moveTo(target, AGGRESSIVE_SPEED);

        if (mimic.getAggressiveTicks() >= AGGRESSIVE_TICKS) {
            mimic.setAggressive(false);
            mimic.getNavigation().stop();
            disappear();
        }
    }

    private void startCapture() {
        if (target == null) return;

        capturing = true;
        captureTicks = 0;
        captureSoundPlayed = false;
        ACTIVE_CAPTURES.put(target.getUUID(), this);

        freezeMimic();
        PaleMimicCapturePacket.send(target, mimic.getEyePosition());
        freezeTarget();
    }

    private void tickCapture() {
        if (target == null || target.isRemoved()) {
            freezeMimic();
            clearActiveCapture();
            capturing = false;
            return;
        }

        freezeMimic();
        freezeTarget();
        captureTicks++;

        if (!captureSoundPlayed && captureTicks >= CAPTURE_SOUND_TICK) {
            captureSoundPlayed = true;
        }

        if (captureTicks >= CAPTURE_TELEPORT_FALLBACK_TICK) {
            finishCapture();
        }
    }

    private void freezeTarget() {
        target.setDeltaMovement(Vec3.ZERO);
        target.hurtMarked = true;
        target.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN,
                5,
                255,
                false,
                false,
                false
        ));
    }

    private void freezeMimic() {
        mimic.getNavigation().stop();
        mimic.setDeltaMovement(Vec3.ZERO);
        mimic.hurtMarked = true;
    }

    private boolean isCapturing(ServerPlayer player) {
        return capturing
                && target != null
                && target.getUUID().equals(player.getUUID())
                && !target.isRemoved();
    }

    private void clearActiveCapture() {
        if (target != null && ACTIVE_CAPTURES.get(target.getUUID()) == this) {
            ACTIVE_CAPTURES.remove(target.getUUID());
        }
    }

    private void playCaptureSound() {
        if (!(mimic.level() instanceof ServerLevel level)) return;

        level.playSound(
                null,
                target.blockPosition(),
                ModSounds.NECK_BONE_FRACTURE.get(),
                SoundSource.HOSTILE,
                1.0F,
                1.0F
        );
    }

    private void finishCapture() {
        if (!capturing || target == null || target.isRemoved()) return;

        clearActiveCapture();
        freezeMimic();
        freezeTarget();
        AfterimageTeleportUtil.teleportToPaleMimicVoidRoom(target);

        mimic.setAggressive(false);
        mimic.getNavigation().stop();
        mimic.discard();
        capturing = false;
    }
}
