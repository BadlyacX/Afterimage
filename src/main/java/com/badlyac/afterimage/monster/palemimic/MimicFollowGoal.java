package com.badlyac.afterimage.monster.palemimic;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class MimicFollowGoal extends Goal {

    private final PaleMimicEntity mimic;
    private final PlayerPathRecorder recorder;

    private ServerPlayer target;
    private final int delayTicks;

    private int recalcCooldown;

    public MimicFollowGoal(PaleMimicEntity mimic, PlayerPathRecorder recorder, int dTicks) {
        this.mimic = mimic;
        this.recorder = recorder;
        this.delayTicks =dTicks;

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

        if (distSqr < 0.04) {
            mimic.getNavigation().stop();
            return;
        }
//         else if (!mimic.getNavigation().isDone() && mimic.horizontalCollision) {
//            mimic.getNavigation().stop();
//        }

        if (recalcCooldown-- <= 0) {
            recalcCooldown = 5;

            mimic.getNavigation().moveTo(
                    targetPos.x,
                    targetPos.y,
                    targetPos.z,
                    getSpeed(distSqr)
            );
        }
    }

    private double getSpeed(double distSqr) {
        double dist = Math.sqrt(distSqr);

        double base = 0.6;
        double max = 1.2;

        double t = Math.min(dist / 10.0, 1.0);
        return base + (max -base) * t;
    }
}
