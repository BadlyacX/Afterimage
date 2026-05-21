package com.badlyac.afterimage.monster.palemimic;

import com.badlyac.afterimage.AfterimageMod;
import com.badlyac.afterimage.registry.ModDimensions;
import com.badlyac.afterimage.registry.ModEntities;
import com.badlyac.afterimage.state.AfterimageState;
import com.badlyac.afterimage.util.Clock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = AfterimageMod.MOD_ID)
public class PaleMimicSpawnManager {
    private static final long FIRST_SPAWN_TICKS = 3 * Clock.MIN;
    private static final long DOUBLE_CHANCE_TICKS = 15 * Clock.MIN;

    private static final long SPAWN_CHECK_INTERVAL = 20 * Clock.SEC;

    private static final double BASE_SPAWN_CHANCE = 0.20;
    private static final double DOUBLED_SPAWN_CHANCE = BASE_SPAWN_CHANCE * 2.0;

    private static final double SPAWN_DISTANCE = 10.0;

    private static final Map<UUID, Integer> afterimageTicks = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> spawnCheckCooldown = new ConcurrentHashMap<>();
    private static final Map<UUID, UUID> activeMimics = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        if (!(player.level() instanceof ServerLevel level)) return;

        UUID playerId = player.getUUID();

        if (!isValidAfterimagePlayer(player, level)) {
            clearPlayer(player, level);
            return;
        }

        cleanupDeadMimic(playerId, level);

        if (hasActiveMimic(playerId, level)) {
            incrementAfterimageTicks(playerId);
            return;
        }

        long ticks = incrementAfterimageTicks(playerId);

        if (ticks < FIRST_SPAWN_TICKS) return;

        long cooldown = spawnCheckCooldown.getOrDefault(playerId, 0L);

        if (cooldown > 0) {
            spawnCheckCooldown.put(playerId, cooldown - 1);
        }

        spawnCheckCooldown.put(playerId, SPAWN_CHECK_INTERVAL);

        double chance = ticks >= DOUBLE_CHANCE_TICKS
                ? DOUBLED_SPAWN_CHANCE
                : BASE_SPAWN_CHANCE;

        if (level.random.nextDouble() > chance) return;

        spawnMimic(player, level);

    }

    private static boolean isValidAfterimagePlayer(ServerPlayer player, ServerLevel level) {
        return level.dimension().equals(ModDimensions.AFTERIMAGE_LEVEL)
                && AfterimageState.isInAfterimage(player);
    }

    private static int incrementAfterimageTicks(UUID playerId) {
        int ticks = afterimageTicks.getOrDefault(playerId, 0) + 1;
        afterimageTicks.put(playerId, ticks);
        return ticks;
    }

    private static void spawnMimic(ServerPlayer player, ServerLevel level) {
        BlockPos spawnPos = findSpawnPos(player, level);

        if (spawnPos == null) return;

        PaleMimicEntity mimic = ModEntities.PALE_MIMIC.get().create(level);

        if (mimic == null) return;

        mimic.moveTo(
                spawnPos.getX() + 0.5,
                spawnPos.getY(),
                spawnPos.getZ() + 0.5,
                player.getYRot(),
                0.0F
        );

        mimic.setTargetPlayer(player);
        pickDisguisePlayer(player, level, mimic);

        level.addFreshEntity(mimic);

        activeMimics.put(player.getUUID(), mimic.getUUID());
    }

    private static void pickDisguisePlayer(ServerPlayer targetPlayer, ServerLevel level, PaleMimicEntity mimic) {
        List<ServerPlayer> candidates = new ArrayList<>();

        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            if (player.level() != level) continue;
            if (player.getUUID().equals(targetPlayer.getUUID())) continue;

            candidates.add(player);
        }

        if (candidates.isEmpty()) {
            mimic.clearDisguisePlayer();
            return;
        }

        ServerPlayer disguisePlayer = candidates.get(level.random.nextInt(candidates.size()));
        mimic.setDisguisePlayer(disguisePlayer);
    }

    private static BlockPos findSpawnPos(ServerPlayer player, ServerLevel level) {
        Vec3 backward = player.getLookAngle().normalize().scale(-1);

        for (int i = 0; i < 24; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2.0;
            Vec3 randomOffset = new Vec3(
                    Math.cos(angle),
                    0,
                    Math.sin(angle)
            ).scale(0.45);

            Vec3 dir = backward.add(randomOffset).normalize();

            Vec3 rawPos = player.position().add(dir.scale(SPAWN_DISTANCE));

            if (isInPlayerView(player, rawPos, level)) continue;

            BlockPos safePos = findSafeGround(level, BlockPos.containing(rawPos));

            if (safePos == null) continue;

            Vec3 center = Vec3.atCenterOf(safePos);

            if (isInPlayerView(player, center, level)) continue;

            return safePos;
        }

        return null;
    }

    private static BlockPos findSafeGround(ServerLevel level, BlockPos origin) {
        int minY = level.getMinBuildHeight();
        int maxY = level.getMaxBuildHeight();

        int startY = Math.min(Math.max(origin.getY(), minY), maxY - 2);

        for (int y = startY + 6; y >= startY - 12; y--) {
            if (y <= minY || y >= maxY - 2) continue;

            BlockPos feet = new BlockPos(origin.getX(), y, origin.getZ());
            BlockPos below = feet.below();
            BlockPos head = feet.above();

            boolean solidGround = level.getBlockState(below).isSolidRender(level, below);
            boolean feetClear = level.getBlockState(feet).isAir();
            boolean headClear = level.getBlockState(head).isAir();

            if (solidGround && feetClear && headClear) {
                return feet;
            }
        }

        return null;
    }

    private static boolean isInPlayerView(ServerPlayer player, Vec3 pos, ServerLevel level) {
        Vec3 look = player.getLookAngle().normalize();
        Vec3 toPos = pos.subtract(player.getEyePosition()).normalize();

        double dot = look.dot(toPos);

        if (dot < 0.5) return false;

        BlockHitResult result = level.clip(new ClipContext(
                player.getEyePosition(),
                pos.add(0, 1.0, 0),
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                player
        ));

        return result.getType() == HitResult.Type.MISS;
    }

    private static boolean hasActiveMimic(UUID playerId, ServerLevel level) {
        UUID mimicId = activeMimics.get(playerId);

        if (mimicId == null) return false;

        Entity entity = level.getEntity(mimicId);

        if (!(entity instanceof PaleMimicEntity mimic)) {
            activeMimics.remove(playerId);
            return false;
        }

        if (!mimic.isAlive() || mimic.isRemoved()) {
            activeMimics.remove(playerId);
            return false;
        }

        return true;
    }

    private static void cleanupDeadMimic(UUID playerId, ServerLevel level) {
        UUID mimicId = activeMimics.get(playerId);

        if (mimicId == null) return;

        Entity entity = level.getEntity(mimicId);

        if (!(entity instanceof PaleMimicEntity) || entity.isRemoved() || !entity.isAlive()) {
            activeMimics.remove(playerId);
        }
    }

    private static void clearPlayer(ServerPlayer player, ServerLevel level) {
        UUID playerId = player.getUUID();

        afterimageTicks.remove(playerId);
        spawnCheckCooldown.remove(playerId);

        UUID mimicId = activeMimics.remove(playerId);

        if (mimicId != null) {
            Entity entity = level.getEntity(mimicId);

            if (entity instanceof PaleMimicEntity) {
                entity.discard();
            }
        }
    }
}
