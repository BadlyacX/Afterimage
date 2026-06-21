package com.badlyac.afterimage.dimension.palemimic;

import com.badlyac.afterimage.AfterimageMod;
import com.badlyac.afterimage.util.AfterimageTeleportUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.badlyac.afterimage.registry.registries.ModDimensions.PALE_MIMIC_PLAIN_LEVEL;

@Mod.EventBusSubscriber(modid = AfterimageMod.MOD_ID)
public final class PaleMimicPlainHandler {

    public PaleMimicPlainHandler() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        if (player.level().dimension() != PALE_MIMIC_PLAIN_LEVEL) return;

        double dx = player.getX() - PaleMimicPlainWorldSetup.SPAWN.getX();
        double dz = player.getZ() - PaleMimicPlainWorldSetup.SPAWN.getZ();

        if (dx * dx + dz * dz > (double) PaleMimicPlainWorldSetup.BOUNDARY_RADIUS * PaleMimicPlainWorldSetup.BOUNDARY_RADIUS) {
            AfterimageTeleportUtil.teleportToPaleMimicSpawn(player);
        }
    }

    @SubscribeEvent
    public static void onFinalizeSpawn(MobSpawnEvent.FinalizeSpawn event) {
        // Mobs are now allowed to spawn for the Silent Hill vibe
    }

    @SubscribeEvent
    public static void onJoinLevel(EntityJoinLevelEvent event) {
        // Mobs are now allowed to join for the Silent Hill vibe
    }
}
