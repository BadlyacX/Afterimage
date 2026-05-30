package com.badlyac.afterimage.util;

import com.badlyac.afterimage.registry.ModDimensions;
import com.badlyac.afterimage.registry.ModEntities;
import com.badlyac.afterimage.monster.palemimic.PaleMimicEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public final class AfterimageTeleportUtil {
    private static final BlockPos PALE_MIMIC_VOID_ROOM_CENTER = new BlockPos(0, -60, 0);

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

        ServerLevel target = server.getLevel(ModDimensions.PALE_MIMIC_VOID_LEVEL);
        if (target == null) return false;

//        buildPaleMimicVoidRoom(target);
//        spawnPaleMimicVoidWatcher(target, player);

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

//    private static void spawnPaleMimicVoidWatcher(ServerLevel level, ServerPlayer player) {
//        AABB roomArea = new AABB(PALE_MIMIC_VOID_ROOM_CENTER).inflate(4.0D);
//
//        for (PaleMimicEntity mimic : level.getEntitiesOfClass(PaleMimicEntity.class, roomArea)) {
//            mimic.discard();
//        }
//
//        PaleMimicEntity mimic = ModEntities.PALE_MIMIC.get().create(level);
//        if (mimic == null) return;
//
//        mimic.moveTo(
//                PALE_MIMIC_VOID_ROOM_CENTER.getX() + 0.5,
//                PALE_MIMIC_VOID_ROOM_CENTER.getY(),
//                PALE_MIMIC_VOID_ROOM_CENTER.getZ() + 0.5,
//                0.0F,
//                0.0F
//        );
//        mimic.setNoAi(true);
//        mimic.setNoGravity(true);
//        mimic.setInvulnerable(true);
//        mimic.setTargetPlayer(player);
//
//        level.addFreshEntity(mimic);
//    }
//
//    private static void buildPaleMimicVoidRoom(ServerLevel level) {
//        BlockPos center = PALE_MIMIC_VOID_ROOM_CENTER;
//
//        for (int x = -2; x <= 2; x++) {
//            for (int y = -2; y <= 2; y++) {
//                for (int z = -2; z <= 2; z++) {
//                    BlockPos pos = center.offset(x, y, z);
//                    boolean shell = Math.abs(x) == 2 || Math.abs(y) == 2 || Math.abs(z) == 2;
//
//                    level.setBlockAndUpdate(
//                            pos,
//                            shell
//                                    ? Blocks.CRYING_OBSIDIAN.defaultBlockState()
//                                    : Blocks.AIR.defaultBlockState()
//                    );
//                }
//            }
//        }
//    }

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
