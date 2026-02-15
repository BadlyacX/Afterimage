package com.badlyac.afterimage.handler;

import com.badlyac.afterimage.network.AfterimageStateSyncPacket;
import com.badlyac.afterimage.registry.ModDimensions;
import com.badlyac.afterimage.state.AfterimageState;
import com.badlyac.afterimage.util.AfterimageTeleportUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public final class AfterimageTravelHandler {

    public static void toggle(ServerPlayer player) {

        boolean currentlyInAfterimage = AfterimageState.isInAfterimage(player);

        if (player.level().dimension() != Level.OVERWORLD
                && player.level().dimension() != ModDimensions.AFTERIMAGE_LEVEL) {
            return;
        }

        if (currentlyInAfterimage) {
            AfterimageTeleportUtil.teleportToOverworld(player);
            AfterimageState.leave(player);
        } else {
            if (AfterimageTeleportUtil.teleportToAfterimage(player)) {
                AfterimageState.enter(player);
            }
        }

        AfterimageStateSyncPacket.sync(player);
    }
}
