package com.badlyac.afterimage.monster.palemimic;

import com.badlyac.afterimage.AfterimageMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

@Mod.EventBusSubscriber(modid = AfterimageMod.MOD_ID)
public class PlayerMovementRecorder {
    public static final Map<UUID, Deque<MovementSnapshot>> HISTORY = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        ServerPlayer player = (ServerPlayer) event.player;

        HISTORY.computeIfAbsent(player.getUUID(), k -> new ArrayDeque<>());
        Deque<MovementSnapshot> deque = HISTORY.get(player.getUUID());

        deque.addLast(new MovementSnapshot(
                player.position(),
                player.getYRot(),
                player.getXRot()
                )
        );

        if (deque.size() > 40) deque.removeFirst();
    }
}