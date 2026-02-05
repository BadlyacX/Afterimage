package com.badlyac.afterimage.manager;

import com.badlyac.afterimage.AfterimageMod;
import com.badlyac.afterimage.state.AfterimageState;
import com.badlyac.afterimage.util.AfterimagePhaseUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AfterimageMod.MOD_ID)
public final class AfterimageVisibilityManager {

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        Entity target = event.getEntity();
        Player viewer = event.getEntity();

        if (!(viewer instanceof ServerPlayer)) return;

        boolean viewerAfter = AfterimageState.isInAfterimage(viewer);
        boolean targetAfter = AfterimagePhaseUtil.isAfterimage(target);

        if (viewerAfter != targetAfter) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        AfterimageState.onLogout(event.getEntity());
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            AfterimageState.onDeath(player);
        }
    }

    /*
    =======================================
    AfterimageState.toggle(player);
    AfterimageVisibilityManager.refreshVisibility(player);
    =================================================
    */
    public static void refreshVisibility(ServerPlayer player) {
        ServerLevel level = player.serverLevel();

        for (ServerPlayer other : level.players()) {
            if (other == player) continue;

            boolean pAfter = AfterimageState.isInAfterimage(player);
            boolean oAfter = AfterimageState.isInAfterimage(other);

            if (pAfter != oAfter) {
                forceRefresh(level, player);
                forceRefresh(level, other);
            }
        }
    }

    private static void forceRefresh(ServerLevel level, ServerPlayer player) {
        level.getChunkSource().removeEntity(player);
        level.getChunkSource().addEntity(player);
    }
}
