package com.badlyac.afterimage.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public final class AfterimageParticleUtil {

    public static void burst(ServerLevel level, Vec3 center) {
        level.sendParticles(
                net.minecraft.core.particles.ParticleTypes.PORTAL,
                center.x, center.y + 0.8, center.z,
                120,
                0.6, 1.0, 0.6,
                0.18
        );

        level.sendParticles(
                net.minecraft.core.particles.ParticleTypes.REVERSE_PORTAL,
                center.x, center.y + 0.8, center.z,
                80,
                0.4, 0.8, 0.4,
                0.12
        );

        level.sendParticles(
                net.minecraft.core.particles.ParticleTypes.SQUID_INK,
                center.x, center.y + 0.9, center.z,
                30,
                0.25, 0.35, 0.25,
                0.02
        );
    }

    public static void burst(ServerLevel level, BlockPos pos) {
        burst(level, Vec3.atCenterOf(pos));
    }
}
