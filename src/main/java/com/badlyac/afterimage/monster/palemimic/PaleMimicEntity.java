package com.badlyac.afterimage.monster.palemimic;


import com.badlyac.afterimage.util.Clock;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public class PaleMimicEntity extends Monster {
    private static final EntityDataAccessor<Optional<UUID>> DATA_DISGUISE_PLAYER_ID =
            SynchedEntityData.defineId(PaleMimicEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Optional<UUID>> DATA_TARGET_PLAYER_ID =
            SynchedEntityData.defineId(PaleMimicEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Boolean> DATA_AGGRESSIVE =
            SynchedEntityData.defineId(PaleMimicEntity.class, EntityDataSerializers.BOOLEAN);

    private boolean triggered;
    private boolean warning;

    private int unseenTicks;
    private int warningTicks;
    private int aggressiveTicks;

    public static final PlayerPathRecorder INSTANCE =
            new PlayerPathRecorder();

    public enum State {
        FOLLOWING,
        WARNING,
        AGGRESSIVE,
        DISAPPEARING
    }

    public PaleMimicEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.triggered = false;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.ATTACK_DAMAGE, 1.0);
    }

    public boolean isTriggered() {
        return this.triggered;
    }

    public void setTriggered(boolean triggered) {
        this.triggered = triggered;
    }

    public boolean isAggressive() {
        return this.entityData.get(DATA_AGGRESSIVE);
    }

    public void setAggressive(boolean aggressive) {
        this.entityData.set(DATA_AGGRESSIVE, aggressive);
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
        Optional<UUID> targetPlayerId = this.getTargetPlayerId();
        if (targetPlayerId.isEmpty()) return null;

        if (this.level() instanceof ServerLevel serverLevel) {
            return (ServerPlayer) serverLevel.getPlayerByUUID(targetPlayerId.get());
        }

        return null;
    }

    public void setTargetPlayer(ServerPlayer serverPlayer) {
        this.entityData.set(DATA_TARGET_PLAYER_ID, Optional.of(serverPlayer.getUUID()));
        this.setTarget(serverPlayer);
    }

    public Optional<UUID> getTargetPlayerId() {
        return this.entityData.get(DATA_TARGET_PLAYER_ID);
    }

    public Optional<UUID> getDisguisePlayerId() {
        return this.entityData.get(DATA_DISGUISE_PLAYER_ID);
    }

    public void setDisguisePlayer(ServerPlayer serverPlayer) {
        this.entityData.set(DATA_DISGUISE_PLAYER_ID, Optional.of(serverPlayer.getUUID()));
    }

    public void clearDisguisePlayer() {
        this.entityData.set(DATA_DISGUISE_PLAYER_ID, Optional.empty());
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_DISGUISE_PLAYER_ID, Optional.empty());
        this.entityData.define(DATA_TARGET_PLAYER_ID, Optional.empty());
        this.entityData.define(DATA_AGGRESSIVE, false);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        this.getDisguisePlayerId().ifPresent(uuid -> tag.putUUID("DisguisePlayer", uuid));
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("DisguisePlayer")) {
            this.entityData.set(DATA_DISGUISE_PLAYER_ID, Optional.of(tag.getUUID("DisguisePlayer")));
        } else {
            this.clearDisguisePlayer();
        }
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
    public boolean causeFallDamage(float fallDistance, float multiplier, @NotNull DamageSource source) {
        return false;
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        return false;
    }

    @Override
    public void tick() {
        super.tick();

        Player target = this.findTargetPlayer();
        this.lookAtTargetPlayer(target);

        if (level().isClientSide || !(level() instanceof ServerLevel level)) return;

        ServerPlayer serverTarget = target instanceof ServerPlayer serverPlayer ? serverPlayer : null;

        if (serverTarget == null) {
            serverTarget = (ServerPlayer) level.getNearestPlayer(this, 64);
            if (serverTarget != null) {
                this.setTargetPlayer(serverTarget);
                this.lookAtTargetPlayer(serverTarget);
            }
        }

        if (serverTarget != null && !serverTarget.isRemoved()) {
            INSTANCE.record(serverTarget);
        }
    }

    private Player findTargetPlayer() {
        Optional<UUID> targetPlayerId = this.getTargetPlayerId();
        if (targetPlayerId.isEmpty()) return null;

        for (Player player : this.level().players()) {
            if (player.getUUID().equals(targetPlayerId.get())) {
                return player;
            }
        }

        return null;
    }

    private void lookAtTargetPlayer(Player target) {
        if (target == null || target.isRemoved()) return;

        Vec3 delta = target.getEyePosition().subtract(this.getEyePosition());
        double horizontalDistance = Math.sqrt(delta.x * delta.x + delta.z * delta.z);

        float targetYHeadRot = (float) (Mth.atan2(delta.z, delta.x) * Mth.RAD_TO_DEG) - 90.0F;
        float targetXRot = (float) -(Mth.atan2(delta.y, horizontalDistance) * Mth.RAD_TO_DEG);

        this.setYHeadRot(targetYHeadRot);
        this.yHeadRotO = targetYHeadRot;
        this.setXRot(Mth.clamp(targetXRot, -90.0F, 90.0F));
        this.xRotO = this.getXRot();
    }

    @Override
    public void registerGoals() {
        this.goalSelector.addGoal(0, new MimicReactionGoal(this));
        this.goalSelector.addGoal(1, new MimicFollowGoal(this, INSTANCE, Clock.SEC * 2));
    }
}
