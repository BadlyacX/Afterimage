package com.badlyac.afterimage.monster.palemimic;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class MimicAttackGoal extends Goal {

    private final PaleMimicEntity mimic;

    public MimicAttackGoal(PaleMimicEntity mimic) {
        this.mimic = mimic;
        this.setFlags(EnumSet.of(Flag.MOVE));
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

        double dist = mimic.distanceTo(player);

        if (!mimic.isExecuting() && dist < 2.5D) {
            mimic.startExecution();
        }

        if (!mimic.isExecuting()) return;

        mimic.executeTick++;

        if (mimic.executeTick <= 20) {

            player.setDeltaMovement(0, 0, 0);
            player.hurtMarked = true;

            Vec3 dir = mimic.position().subtract(player.position()).normalize();

            float yaw = (float)(Math.atan2(dir.z, dir.x) * (180F / Math.PI)) - 90F;
            float pitch = (float)(-(Math.atan2(dir.y, Math.sqrt(dir.x * dir.x + dir.z * dir.z)) * (180F / Math.PI)));

            player.setYRot(yaw);
            player.setXRot(pitch);
            player.yHeadRot = yaw;
            player.yBodyRot = yaw;

            mimic.setGrabbing(true);

            if (mimic.executeTick % 5 == 0) {
                mimic.level().playSound(
                        null,
                        player.blockPosition(),
                        SoundEvents.WARDEN_HEARTBEAT,
                        SoundSource.HOSTILE,
                        0.8F,
                        0.5F
                );
            }
        }

        if (mimic.executeTick == 20) {

            mimic.level().playSound(
                    null,
                    player.blockPosition(),
                    SoundEvents.SKELETON_HURT,
                    SoundSource.HOSTILE,
                    1.0F,
                    0.5F
            );

            player.teleportTo(
                    player.getX(),
                    mimic.level().getMinBuildHeight() - 20,
                    player.getZ()
            );

            mimic.discard();
        }
    }
}