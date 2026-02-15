package com.badlyac.afterimage.util;

import com.badlyac.afterimage.registry.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

public final class AfterimageTeleportUtil {

    public static boolean teleportToAfterimage(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) return false;

        ServerLevel target = server.getLevel(ModDimensions.AFTERIMAGE_LEVEL);
        if (target == null) return false;

        teleport(player, target);
        return true;
    }

    public static void teleportToOverworld(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) return;

        teleport(player, server.overworld());
    }

    private static void teleport(ServerPlayer player, ServerLevel target) {

        int x = player.blockPosition().getX();
        int z = player.blockPosition().getZ();

        BlockPos safePos = findSafePosition(target, x, z);

        if (safePos == null) {
            safePos = target.getSharedSpawnPos();
        }

        player.teleportTo(
                target,
                safePos.getX() + 0.5,
                safePos.getY(),
                safePos.getZ() + 0.5,
                player.getYRot(),
                player.getXRot()
        );
    }

    private static BlockPos findSafePosition(ServerLevel level, int x, int z) {

        int topY = level.getHeight(
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                x,
                z
        );

        for (int y = topY; y >= level.getMinBuildHeight(); y--) {

            BlockPos pos = new BlockPos(x, y, z);
            BlockState feet = level.getBlockState(pos);
            BlockState head = level.getBlockState(pos.above());
            BlockState below = level.getBlockState(pos.below());

            if (!feet.isAir()) continue;
            if (!head.isAir()) continue;
            if (!below.isSolid()) continue;

            if (below.getFluidState().isSource()) continue;

            return pos;
        }

        return null;
    }

}
