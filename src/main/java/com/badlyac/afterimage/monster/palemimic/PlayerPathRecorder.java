package com.badlyac.afterimage.monster.palemimic;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerPathRecorder {

    private final Map<UUID, Deque<PathPoint>> historyMap = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> lastGroundMap = new ConcurrentHashMap<>();
    private static final int MAX_SIZE = 200;
    private static final double MIN_DIST_SQ = 0.0025;

    public void record(ServerPlayer player) {
        UUID id = player.getUUID();

        Deque<PathPoint> history = historyMap.computeIfAbsent(
                id, k -> new ArrayDeque<>()
        );

        Vec3 pos = player.position();

        if (history.isEmpty()) {
            history.addLast(new PathPoint(
                    pos,
                    player.getYRot(),
                    player.getXRot(),
                    isJumped(player)
            ));
            return;
        }

        PathPoint last = history.getLast();

        if (last.pos().distanceToSqr(pos) > MIN_DIST_SQ) {
            history.addLast(new PathPoint(
                    pos,
                    player.getYRot(),
                    player.getXRot(),
                    isJumped(player)
            ));
        }

        if (history.size() > MAX_SIZE) {
            history.removeFirst();
        }
    }

    public PathPoint getDelayed(UUID id, int delay) {
        Deque<PathPoint> history = historyMap.get(id);
        if (history == null || history.size() <= delay) return null;

        int targetIndex = history.size() - 1 - delay;

        int i = 0;
        for (PathPoint p : history) {
            if (i == targetIndex) return p;
            i++;
        }

        return null;
    }

    public boolean isJumped(ServerPlayer player) {
        UUID id = player.getUUID();
        boolean wasOnGround = lastGroundMap.getOrDefault(id, false);
        boolean onGround = player.onGround();

        boolean jumped = wasOnGround && !onGround && player.getDeltaMovement().y > 0;

        lastGroundMap.put(id, onGround);
        return jumped;
    }

    public void clear(UUID id) {
        historyMap.remove(id);
    }

}
