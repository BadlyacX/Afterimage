package com.badlyac.afterimage.handler;

import com.badlyac.afterimage.network.AfterimageNetwork;
import com.badlyac.afterimage.network.AfterimageStateSyncPacket;
import com.badlyac.afterimage.state.AfterimageState;
import com.badlyac.afterimage.util.AfterimageTeleportUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

public final class AfterimageTravelHandler {

    public static void toggle(ServerPlayer player) {

        boolean currentlyInAfterimage = AfterimageState.isInAfterimage(player);

        if (currentlyInAfterimage) {
            AfterimageTeleportUtil.teleportToOverworld(player);
            AfterimageState.leave(player);
        } else {
            if (AfterimageTeleportUtil.teleportToAfterimage(player)) {
                AfterimageState.enter(player);
            }
        }

        sync(player);
    }

    public static void sync(ServerPlayer player) {
        AfterimageNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new AfterimageStateSyncPacket(
                        AfterimageState.isInAfterimage(player)
                )
        );
    }
}
