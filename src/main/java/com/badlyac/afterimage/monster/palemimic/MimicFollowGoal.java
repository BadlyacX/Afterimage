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
    private final long delayTicks;

    public MimicFollowGoal(
            PaleMimicEntity mimic,
            PlayerPathRecorder recorder,
            long delayTicks
    ) {
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
        mimic.setDeltaMovement(Vec3.ZERO);
    }

    @Override
    public void tick() {
        if (target == null) return;

        PathPoint point = recorder.getDelayed(target.getUUID(), delayTicks);
        if (point == null) return;

        replay(point);
    }

    private void replay(PathPoint point) {
        mimic.getNavigation().stop();
        mimic.setDeltaMovement(point.movement());
        mimic.moveTo(
                point.pos().x,
                point.pos().y,
                point.pos().z,
                point.yaw(),
                point.pitch()
        );
        mimic.setYRot(point.yaw());
        mimic.setXRot(point.pitch());
        mimic.setYHeadRot(point.yaw());
        mimic.setYBodyRot(point.yaw());
        mimic.yRotO = point.yaw();
        mimic.xRotO = point.pitch();
        mimic.yHeadRotO = point.yaw();
        mimic.yBodyRotO = point.yaw();
        mimic.setShiftKeyDown(point.shiftKeyDown());
        mimic.setSprinting(point.sprinting());
        mimic.setSwimming(point.swimming());
        mimic.setPose(point.pose());

        if (point.jumping() && mimic.onGround()) {
            mimic.getJumpControl().jump();
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
