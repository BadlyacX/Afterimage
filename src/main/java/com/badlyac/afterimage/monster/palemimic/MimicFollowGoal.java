package com.badlyac.afterimage.monster.palemimic;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class MimicFollowGoal extends Goal {

    private final PaleMimicEntity mimic;

    public MimicFollowGoal(PaleMimicEntity mimic) {
        this.mimic = mimic;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return true;
    }

    @Override
    public void tick() {
        if (mimic.level().isClientSide) return;

        ServerPlayer player = mimic.getTargetPlayer();
        if (player == null) return;

        if (isLookedAt(player)) {
            mimic.setDeltaMovement(Vec3.ZERO);
            return;
        }

        double dist = mimic.distanceTo(player);

        if (dist > 2.5D) {
            Vec3 dir = player.position().subtract(mimic.position()).normalize();

            double speed = dist > 6 ? 0.45 : 0.3;
            mimic.setDeltaMovement(dir.scale(speed));
            mimic.move(MoverType.SELF, mimic.getDeltaMovement());
        }
    }

    private boolean isLookedAt(ServerPlayer player) {
        Vec3 toMimic = mimic.position().subtract(player.getEyePosition());
        double dist = toMimic.length();

        if (dist < 0.0001) return true;

        Vec3 view = player.getViewVector(1.0F).normalize();
        Vec3 dir = toMimic.normalize();

        double dot = view.dot(dir);
        boolean inFov = dot > 0.92;

        boolean visible = player.hasLineOfSight(mimic);

        return inFov && visible;
    }
}