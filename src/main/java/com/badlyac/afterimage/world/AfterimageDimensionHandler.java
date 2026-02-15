package com.badlyac.afterimage.world;

import com.badlyac.afterimage.AfterimageMod;
import com.badlyac.afterimage.network.AfterimageStateSyncPacket;
import com.badlyac.afterimage.registry.ModDimensions;
import com.badlyac.afterimage.state.AfterimageState;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AfterimageMod.MOD_ID)
public class AfterimageDimensionHandler {

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {

        if (!(event.getEntity() instanceof ServerPlayer player))
            return;

        if (event.getFrom() == ModDimensions.AFTERIMAGE_LEVEL
                && event.getTo() == Level.END) {

            AfterimageState.leave(player);

            AfterimageStateSyncPacket.sync(player);
        }
    }
}