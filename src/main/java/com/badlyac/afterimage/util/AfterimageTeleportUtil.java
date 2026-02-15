package com.badlyac.afterimage.util;

import com.badlyac.afterimage.registry.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

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

        BlockPos original = player.blockPosition();

        BlockPos safePos = findSafeY(target, original);

        System.out.println("[AFTERIMAGE] Dimension: " + player.level().dimension());
        System.out.println("[AFTERIMAGE] Location: " + player.blockPosition());
        System.out.println("[AFTERIMAGE] Original: " + original);
        System.out.println("[AFTERIMAGE] SafePos: " + safePos);
        System.out.println("[AFTERIMAGE] Spawn: " + target.getSharedSpawnPos());

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

        player.resetFallDistance();
    }

    private static BlockPos findSafeY(ServerLevel level, BlockPos original) {

        int x = original.getX();
        int z = original.getZ();
        int startY = original.getY();

        int maxOffset = 32;

        for (int offset = 0; offset <= maxOffset; offset++) {

            int upY = startY + offset;
            if (upY < level.getMaxBuildHeight() - 2) {
                BlockPos pos = new BlockPos(x, upY, z);
                if (isSafe(level, pos)) return pos;
            }

            int downY = startY - offset;
            if (downY > level.getMinBuildHeight() + 1) {
                BlockPos pos = new BlockPos(x, downY, z);
                if (isSafe(level, pos)) return pos;
            }
        }

        return null;
    }

    private static boolean isSafe(ServerLevel level, BlockPos pos) {

        AABB playerBox = new AABB(
                pos.getX() + 0.001,
                pos.getY(),
                pos.getZ() + 0.001,
                pos.getX() + 0.999,
                pos.getY() + 1.8,
                pos.getZ() + 0.999
        );

        if (!level.noCollision(playerBox)) {
            return false;
        }

        BlockPos below = pos.below();
        BlockState belowState = level.getBlockState(below);

        return belowState.isCollisionShapeFullBlock(level, below);
    }

}
