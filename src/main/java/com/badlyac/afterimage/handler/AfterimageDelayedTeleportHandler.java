package com.badlyac.afterimage.handler;

import com.badlyac.afterimage.AfterimageMod;
import com.badlyac.afterimage.registry.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = AfterimageMod.MOD_ID)
public final class AfterimageDelayedTeleportHandler {

    private static final int ENTER_DELAY_TICKS = 40;
    private static final int EXIT_DELAY_TICKS = 40;

    private static final int DARKNESS_TICKS = 100;
    private static final int BURST_LAST_TICKS = 5;

    private static final Map<UUID, PendingTeleport> PENDING = new ConcurrentHashMap<>();
    private static final Map<UUID, LockedPlayer> LOCKED = new ConcurrentHashMap<>();

    private record LockedPlayer(double x, double y, double z, float yaw, float pitch) {}
    private record PendingTeleport(int ticksLeft, int totalTicks, Runnable action) {}

    public static void playEnterThenTeleport(ServerPlayer player, Runnable action) {
        start(player, ENTER_DELAY_TICKS, ModSounds.ENTER_SOUND.get(), action);
    }

    public static void playExitThenTeleport(ServerPlayer player, Runnable action) {
        start(player, EXIT_DELAY_TICKS, ModSounds.EXIT_SOUND.get(), action);
    }

    private static void start(ServerPlayer player, int delay, net.minecraft.sounds.SoundEvent sound, Runnable action) {
        UUID id = player.getUUID();
        if (PENDING.containsKey(id)) return;

        player.level().playSound(
                null,
                player.blockPosition(),
                sound,
                SoundSource.PLAYERS,
                1.0F,
                1.0F
        );

        player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, DARKNESS_TICKS, 0, false, false, true));

        freezePlayer(player);
        PENDING.put(id, new PendingTeleport(delay, delay, action));
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        for (Map.Entry<UUID, LockedPlayer> entry : LOCKED.entrySet()) {
            ServerPlayer player = event.getServer().getPlayerList().getPlayer(entry.getKey());
            if (player == null) continue;

            LockedPlayer lock = entry.getValue();

            player.teleportTo(lock.x, lock.y, lock.z);
            player.setYRot(lock.yaw);
            player.setXRot(lock.pitch);
            player.setDeltaMovement(0, 0, 0);
            player.hurtMarked = true;
        }

        Iterator<Map.Entry<UUID, PendingTeleport>> it = PENDING.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<UUID, PendingTeleport> entry = it.next();
            UUID id = entry.getKey();
            PendingTeleport pending = entry.getValue();

            ServerPlayer player = event.getServer().getPlayerList().getPlayer(id);
            if (player != null) {
                spawnScaledInwardParticles(player, pending.ticksLeft, pending.totalTicks);

                if (pending.ticksLeft <= BURST_LAST_TICKS) {
                    spawnBurstParticles(player, pending.ticksLeft);
                }
            }

            int remaining = pending.ticksLeft - 1;

            if (remaining <= 0) {
                it.remove();

                if (player != null) {
                    unfreezePlayer(player);
                    try {
                        pending.action.run();
                    } catch (Throwable ignored) {
                    }
                }
            } else {
                entry.setValue(new PendingTeleport(remaining, pending.totalTicks, pending.action));
            }
        }
    }

    public static void cancel(ServerPlayer player) {
        if (PENDING.remove(player.getUUID()) != null) {
            unfreezePlayer(player);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            cancel(player);
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            cancel(player);
        }
    }

    public static void freezePlayer(ServerPlayer player) {
        LOCKED.put(player.getUUID(), new LockedPlayer(
                player.getX(),
                player.getY(),
                player.getZ(),
                player.getYRot(),
                player.getXRot()
        ));
    }

    public static void unfreezePlayer(ServerPlayer player) {
        LOCKED.remove(player.getUUID());
    }

    private static void spawnScaledInwardParticles(ServerPlayer player, int ticksLeft, int totalTicks) {
        if (!(player.level() instanceof ServerLevel level)) return;

        double px = player.getX();
        double py = player.getY() + 1.0;
        double pz = player.getZ();

        double progress = 1.0 - (ticksLeft / (double) totalTicks);
        int count = 2 + (int) Math.floor(progress * 14.0);
        double radius = 3.0 - (progress * 2.2);

        for (int i = 0; i < count; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2.0;
            double height = (level.random.nextDouble() * 2.0) - 1.0;

            double x = px + Math.cos(angle) * radius;
            double y = py + height;
            double z = pz + Math.sin(angle) * radius;

            double dx = (px - x) * (0.18 + progress * 0.22);
            double dy = (py - y) * (0.18 + progress * 0.22);
            double dz = (pz - z) * (0.18 + progress * 0.22);

            level.sendParticles(
                    ParticleTypes.REVERSE_PORTAL,
                    x, y, z,
                    0,
                    dx, dy, dz,
                    0.45
            );
        }
    }

    private static void spawnBurstParticles(ServerPlayer player, int ticksLeft) {
        if (!(player.level() instanceof ServerLevel level)) return;

        double px = player.getX();
        double py = player.getY() + 1.0;
        double pz = player.getZ();

        int strength = (BURST_LAST_TICKS - ticksLeft) + 1;
        int count = 40 + (strength * 25);

        level.sendParticles(
                ParticleTypes.PORTAL,
                px, py, pz,
                count,
                0.6, 0.8, 0.6,
                0.9
        );

        level.sendParticles(
                ParticleTypes.SMOKE,
                px, py, pz,
                10 + strength * 6,
                0.35, 0.45, 0.35,
                0.02
        );
    }
}