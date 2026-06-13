package com.badlyac.afterimage.util;

import com.badlyac.afterimage.dimension.palemimic.PaleMimicPlainWorldSetup;
import com.badlyac.afterimage.registry.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public final class AfterimageTeleportUtil {
    private static final BlockPos PALE_MIMIC_VOID_ROOM_CENTER = new BlockPos(9, -60, 8);

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

    public static boolean teleportToPaleMimicDimension(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) return false;

        ServerLevel target = server.getLevel(ModDimensions.PALE_MIMIC_PLAIN_LEVEL);
        if (target == null) return false;

        PaleMimicPlainWorldSetup.applyTo(target);

        player.teleportTo(
                target,
                PALE_MIMIC_VOID_ROOM_CENTER.getX() + 0.5,
                PALE_MIMIC_VOID_ROOM_CENTER.getY(),
                PALE_MIMIC_VOID_ROOM_CENTER.getZ() + 1.5,
                player.getYRot(),
                player.getXRot()
        );

        player.resetFallDistance();

        return true;
    }

    public static void teleportThroughDoor(ServerPlayer player, ResourceKey<Level> destination) {
        MinecraftServer server = player.getServer();
        if (server == null) return;

        if (destination == Level.OVERWORLD) {
            ServerLevel overworld = server.overworld();
            BlockPos spawn = overworld.getSharedSpawnPos();
            teleportSafe(player, overworld, spawn);
        } else if (destination == Level.END) {
            ServerLevel end = server.getLevel(Level.END);
            if (end == null) return;
            teleportSafe(player, end, new BlockPos(100, 49, 0));
        } else {
            ServerLevel target = server.getLevel(destination);
            if (target == null) return;
            teleportSafe(player, target, target.getSharedSpawnPos());
        }
    }

    private static void teleportSafe(ServerPlayer player, ServerLevel level, BlockPos startPos) {
        BlockPos safe = findSafePos(level, startPos);
        if (safe == null) safe = startPos;
        player.teleportTo(level, safe.getX() + 0.5, safe.getY(), safe.getZ() + 0.5,
                player.getYRot(), player.getXRot());
        player.resetFallDistance();
    }

    /**
     * 先在 startPos 做垂直搜尋，找不到再向外擴展水平半徑逐圈嘗試。
     * 最多搜尋水平半徑 8 格，每格垂直 ±32 格。
     */
    private static BlockPos findSafePos(ServerLevel level, BlockPos startPos) {
        BlockPos result = findSafeY(level, startPos);
        if (result != null) return result;

        for (int radius = 1; radius <= 8; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.abs(dx) < radius && Math.abs(dz) < radius) continue;
                    result = findSafeY(level, new BlockPos(
                            startPos.getX() + dx, startPos.getY(), startPos.getZ() + dz));
                    if (result != null) return result;
                }
            }
        }

        return null;
    }

    public static void teleportToPaleMimicSpawn(ServerPlayer player) {
        player.teleportTo(
                PALE_MIMIC_VOID_ROOM_CENTER.getX() + 0.5,
                PALE_MIMIC_VOID_ROOM_CENTER.getY(),
                PALE_MIMIC_VOID_ROOM_CENTER.getZ() + 1.5
        );
        player.resetFallDistance();
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
