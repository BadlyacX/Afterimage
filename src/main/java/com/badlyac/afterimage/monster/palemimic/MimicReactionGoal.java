package com.badlyac.afterimage.monster.palemimic;

import com.badlyac.afterimage.registry.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class MimicReactionGoal extends Goal {

    private final PaleMimicEntity mimic;
    private ServerPlayer target;

    public MimicReactionGoal(PaleMimicEntity mimic) {
        this.mimic = mimic;
        this.setFlags(EnumSet.of(Flag.LOOK));
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
    public void tick() {
        if (target == null) return;

        boolean seen = playerSeesMimic(target);

        if (seen) {
            mimic.setUnseenTicks(0);

            if (!mimic.isTriggered()) {
                triggerReaction();
            }

            return;
        }

        mimic.setUnseenTicks(
                mimic.getUnseenTicks() + 1
        );

        if (!mimic.isWarning()
                && mimic.getUnseenTicks() >= 200) {

            startWarning();
        }

        if (mimic.isWarning()) {

            mimic.setWarningTicks(
                    mimic.getWarningTicks() + 1
            );

            tickWarning();

            if (mimic.getWarningTicks() >= 400) {
                triggerReaction();
            }
        }

        if (mimic.isAggressive()) {
            tickAggressive();
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
        mimic.setWarning(true);
        mimic.setWarningTicks(0);
    }

    private void tickWarning() {
        if (!(mimic.level() instanceof ServerLevel level)) return;

        if (mimic.getWarningTicks() % 20 != 0) return;

        float progress = mimic.getWarningTicks() / 400F;
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
    }

    private void tickAggressive() {

        mimic.setAggressiveTicks(
                mimic.getAggressiveTicks() + 1
        );

        Vec3 dir = target.position()
                .subtract(mimic.position())
                .normalize();

        mimic.setDeltaMovement(
                dir.scale(0.8)
        );

        if (mimic.getAggressiveTicks() >= 20) {
            mimic.setAggressive(false);
        }
    }
}