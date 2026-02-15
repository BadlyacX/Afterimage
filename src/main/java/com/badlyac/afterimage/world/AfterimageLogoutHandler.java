package com.badlyac.afterimage.world;

import com.badlyac.afterimage.AfterimageMod;
import com.badlyac.afterimage.handler.AfterimageTravelHandler;
import com.badlyac.afterimage.network.AfterimageStateSyncPacket;
import com.badlyac.afterimage.registry.ModDimensions;
import com.badlyac.afterimage.state.AfterimageState;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AfterimageMod.MOD_ID)
public class AfterimageLogoutHandler {

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {

        if (!(event.getEntity() instanceof ServerPlayer player))
            return;

        if (player.level().dimension() == ModDimensions.AFTERIMAGE_LEVEL) {

            player.teleportTo(
                    player.server.overworld(),
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    player.getYRot(),
                    player.getXRot()
            );
        }

        AfterimageState.leave(player);
        AfterimageStateSyncPacket.sync(player);
    }
}
