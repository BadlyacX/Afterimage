package com.badlyac.afterimage.monster.palemimic;

import net.minecraft.server.level.ServerPlayer;
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
}
