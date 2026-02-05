package com.badlyac.afterimage.state;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class AfterimageState {

    private static final Set<UUID> AFTERIMAGE_PLAYERS =
            ConcurrentHashMap.newKeySet();

    public static boolean isInAfterimage(Player player) {
        return AFTERIMAGE_PLAYERS.contains(player.getUUID());
    }

    public static void enter(ServerPlayer player) {
        AFTERIMAGE_PLAYERS.add(player.getUUID());
    }

    public static void leave(ServerPlayer player) {
        AFTERIMAGE_PLAYERS.remove(player.getUUID());
    }

    public static void toggle(ServerPlayer player) {
        UUID id = player.getUUID();
        if (AFTERIMAGE_PLAYERS.contains(id)) {
            AFTERIMAGE_PLAYERS.remove(id);
        } else {
            AFTERIMAGE_PLAYERS.add(id);
        }
    }

    public static void onLogout(Player player) {
        AFTERIMAGE_PLAYERS.remove(player.getUUID());
    }

    public static void onDeath(Player player) {
        AFTERIMAGE_PLAYERS.remove(player.getUUID());
    }
}
