package com.badlyac.afterimage.monster.palemimic;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class MimicFollowGoal extends Goal {

    private final PaleMimicEntity mimic;
    private final PlayerPathRecorder recorder;

    private ServerPlayer target;
    private final int delayTicks;

    private int recalcCooldown;

    private int stuckTicks = 0;
    private Vec3 lastPos = Vec3.ZERO;

    public MimicFollowGoal(PaleMimicEntity mimic, PlayerPathRecorder recorder, int delayTicks) {
        this.mimic = mimic;
        this.recorder = recorder;
        this.delayTicks = delayTicks;

        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (mimic.isTriggered()) return false;

        target = mimic.getTargetPlayer();
        return target != null && !target.isRemoved();
    }

    @Override
    public boolean canContinueToUse() {
        return target != null && !target.isRemoved() && !mimic.isTriggered();
    }

    @Override
    public void stop() {
        target = null;
        mimic.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (target == null) return;

        PathPoint point = recorder.getDelayed(target.getUUID(), delayTicks);

        Vec3 targetPos = point.pos();

        double distSqr = mimic.distanceToSqr(targetPos);
        double moved = mimic.position().distanceToSqr(lastPos);

        if (moved < 0.0005) {
            stuckTicks++;
        } else {
            stuckTicks = 0;
        }

        lastPos = mimic.position();

        if (distSqr < 0.04) {
            mimic.getNavigation().stop();
            return;
        }

        if (recalcCooldown-- <= 0) {
            recalcCooldown = 5;

            mimic.getNavigation().moveTo(
                    targetPos.x,
                    targetPos.y,
                    targetPos.z,
                    getSpeed(distSqr)
            );

            if (point.jumping() && mimic.onGround())
                mimic.getJumpControl().jump();
        }
    }

    private double getSpeed(double distSqr) {
        double dist = Math.sqrt(distSqr);

        double base = 0.6;
        double max = 1.2;

        double t = Math.min(dist / 10.0, 1.0);
        return base + (max - base) * t;
    }

    private void disappear() {
        if (!(mimic.level() instanceof ServerLevel level)) {
            mimic.discard();
            return;
        }

        level.sendParticles(
                ParticleTypes.LARGE_SMOKE,
                mimic.getX(),
                mimic.getY(),
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
                SoundEvents.SOUL_ESCAPE,
                SoundSource.HOSTILE,
                1.0F,
                0.8F
        );

        mimic.discard();
    }
}
