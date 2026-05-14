package com.badlyac.afterimage.monster.palemimic;


import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.UUID;

public class PaleMimicEntity extends Monster implements GeoEntity {

    private boolean triggered;
    private boolean aggressive;
    private boolean warning;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private UUID targetPlayerId = null;

    private int unseenTicks;
    private int warningTicks;
    private int aggressiveTicks;

    public enum State {
        FOLLOWING,
        WARNING,
        AGGRESSIVE,
        DISAPPEARING
    }

    public PaleMimicEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.triggered = false;
        this.aggressive = false;
    }

    public boolean isTriggered() {
        return this.triggered;
    }

    public void setTriggered(boolean triggered) {
        this.triggered = triggered;
    }

    public boolean isAggressive() {
        return aggressive;
    }

    public void setAggressive(boolean aggressive) {
        this.aggressive = aggressive;
    }

    public boolean isWarning() {
        return warning;
    }

    public void setWarning(boolean warning) {
        this.warning = warning;
    }

    public int getUnseenTicks() {
        return unseenTicks;
    }

    public void setUnseenTicks(int unseenTicks) {
        this.unseenTicks = unseenTicks;
    }

    public int getWarningTicks() {
        return warningTicks;
    }

    public void setWarningTicks(int warningTicks) {
        this.warningTicks = warningTicks;
    }

    public int getAggressiveTicks() {
        return aggressiveTicks;
    }

    public void setAggressiveTicks(int aggressiveTicks) {
        this.aggressiveTicks = aggressiveTicks;
    }

    public ServerPlayer getTargetPlayer() {
        if (targetPlayerId == null) return null;

        if (this.level() instanceof ServerLevel serverLevel) {
            return (ServerPlayer) serverLevel.getPlayerByUUID(targetPlayerId);
        }

        return null;
    }

    public void setTargetPlayer(ServerPlayer serverPlayer) {
        this.targetPlayerId = serverPlayer.getUUID();
    }

    @Override
    public void playStepSound(@NotNull BlockPos pos, @NotNull BlockState state) {
        if (this.isAggressive()) return;

        SoundType soundType = state.getSoundType();

        this.playSound(
                soundType.getStepSound(),
                0.15F,
                0.85F
        );
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                new AnimationController<>(
                        this,
                        "controller",
                        0,
                        state -> {
                            if (state.isMoving()) {
                                state.setAndContinue(
                                        RawAnimation.begin().thenLoop("walk")
                                );

                                return PlayState.CONTINUE;
                            }
                            state.setAndContinue(
                                    RawAnimation.begin().thenLoop("idle")
                            );

                            return PlayState.CONTINUE;
                        }
                ));
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide
                && this.getTargetPlayer() == null
                && level() instanceof ServerLevel level) {
            ServerPlayer nearest = (ServerPlayer) level.getNearestPlayer(this, 64);

            if (nearest != null) {
                this.setTarget(nearest);
            }
        }
    }
}