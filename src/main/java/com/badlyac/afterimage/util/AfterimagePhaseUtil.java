package com.badlyac.afterimage.util;

import com.badlyac.afterimage.AfterimageMod;
import com.badlyac.afterimage.state.AfterimageState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;


public class AfterimagePhaseUtil {

    public static boolean isAfterimage(Entity entity) {
        if (entity instanceof Player player) return AfterimageState.isInAfterimage(player);

        return entity.getPersistentData().getBoolean(AfterimageMod.MOD_ID);
    }

    public static void markAfterimage(Entity entity) {
        entity.getPersistentData().putBoolean(AfterimageMod.MOD_ID, true);
    }
}
