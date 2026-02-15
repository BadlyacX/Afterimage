package com.badlyac.afterimage.world;

import com.badlyac.afterimage.AfterimageMod;
import com.badlyac.afterimage.handler.AfterimageTravelHandler;
import com.badlyac.afterimage.registry.ModDimensions;
import com.badlyac.afterimage.state.AfterimageState;
import com.badlyac.afterimage.util.AfterimageTeleportUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AfterimageMod.MOD_ID)
public class AfterimageRespawnHandler {

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {

        if (!(event.getEntity() instanceof ServerPlayer player))
            return;

        if (player.level().dimension() == ModDimensions.AFTERIMAGE_LEVEL) {

            AfterimageTeleportUtil.teleportToOverworld(player);
        }

        AfterimageState.leave(player);
        AfterimageTravelHandler.sync(player);
    }
}
