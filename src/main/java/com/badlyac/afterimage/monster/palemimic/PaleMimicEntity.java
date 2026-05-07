package com.badlyac.afterimage.monster.palemimic;


import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class PaleMimicEntity extends Monster {

    private boolean triggered = false;
    private boolean aggressive = false;
    private boolean warning;
    private UUID targetPlayerId = null;
    private int unseenTicks;
    private int warningTicks;
    private int aggressiveTicks;


    public enum State {

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
}