package com.badlyac.afterimage.world;

import com.badlyac.afterimage.manager.AfterimageVisibilityManager;
import com.badlyac.afterimage.network.AfterimageNetwork;
import com.badlyac.afterimage.network.AfterimageStateSyncPacket;
import com.badlyac.afterimage.state.AfterimageState;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

public class AfterimageTravelHandler {

    public static void tryToggle(ServerPlayer player) {
        if (AfterimageState.isInAfterimage(player)) {
            tryExit(player);
        } else {
            tryEnter(player);
        }
    }

    public static void tryEnter(ServerPlayer player) {
        if (AfterimageState.isInAfterimage(player)) return;

        AfterimageState.enter(player);
        sync(player);
        AfterimageVisibilityManager.refreshVisibility(player);
    }

    public static void tryExit(ServerPlayer player) {
        if (!AfterimageState.isInAfterimage(player)) return;

        AfterimageState.leave(player);
        sync(player);
        AfterimageVisibilityManager.refreshVisibility(player);
    }

    private static void sync(ServerPlayer player) {
        AfterimageNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new AfterimageStateSyncPacket(
                        AfterimageState.isInAfterimage(player)
                )
        );
    }
}
