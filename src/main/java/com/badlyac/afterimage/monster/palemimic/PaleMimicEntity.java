package com.badlyac.afterimage.monster.palemimic;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

import java.util.Deque;
import java.util.Iterator;
import java.util.UUID;

import static com.badlyac.afterimage.monster.palemimic.PlayerMovementRecorder.HISTORY;

public class PaleMimicEntity extends Monster {
    private UUID targetUUID;
    private State state = State.PASSIVE;
    private int attackTime;
    private boolean executing = false;
    public int executeTick = 0;
    private boolean grabbing = false;

    public enum State {
        PASSIVE,
        WATCHING,
        TRIGGERED,
        ATTACK,
        VANISH
    }

    public PaleMimicEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public boolean isExecuting() {
        return executing;
    }

    public void startExecution() {
        this.executing = true;
        this.executeTick = 0;
    }

    public void setGrabbing(boolean grabbing) {
        this.grabbing = grabbing;
    }

    public void setTargetPlayer(ServerPlayer player) {
        this.targetUUID = player.getUUID();
    }

    public ServerPlayer getTargetPlayer() {
        if (targetUUID == null) return null;
        if (!(level() instanceof net.minecraft.server.level.ServerLevel serverLevel)) return null;
        return (ServerPlayer) serverLevel.getPlayerByUUID(targetUUID);
    }

    public MovementSnapshot getDelayed(Deque<MovementSnapshot> deque, int delay) {
        if (deque.size() <= delay) return null;
        Iterator<MovementSnapshot> it = deque.iterator();
        for (int i = 0; i < deque.size() - delay - 1; i++) {
            it.next();
        }
        return it.next();
    }

    public State getState() {
        return state;
    }

    public void setState(State s) {
        this.state = s;
    }

    public void setAttackTime(int t) {
        this.attackTime = t;
    }

    public int getAttackTime() {
        return attackTime;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.35D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide) return;

        ServerPlayer target = getTargetPlayer();
        if (target == null) return;

        Deque<MovementSnapshot> history = HISTORY.get(target.getUUID());
        if (history == null || history.size() < 10) return;


    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MimicAttackGoal(this));
        this.goalSelector.addGoal(3, new MimicFollowGoal(this));
    }
}