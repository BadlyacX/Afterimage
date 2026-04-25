package com.badlyac.afterimage.monster.palemimic;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class MimicTriggerGoal extends Goal {

    private final PaleMimicEntity mimic;

    public MimicTriggerGoal(PaleMimicEntity mimic) {
        this.mimic = mimic;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (mimic.getState() != PaleMimicEntity.State.PASSIVE
                && mimic.getState() != PaleMimicEntity.State.WATCHING) return false;

        ServerPlayer target = mimic.getTargetPlayer();
        if (target == null) return false;

        return mimic.distanceTo(target) < 4.0D;
    }

    @Override
    public void start() {
        mimic.setState(PaleMimicEntity.State.TRIGGERED);
        mimic.setAttackTime(20);
    }

    @Override
    public boolean canContinueToUse() {
        return mimic.getState() == PaleMimicEntity.State.TRIGGERED;
    }

    @Override
    public void tick() {
        int t = mimic.getAttackTime() - 1;
        mimic.setAttackTime(t);

        if (t <= 0) {
            mimic.setState(PaleMimicEntity.State.ATTACK);
        }
    }
}
