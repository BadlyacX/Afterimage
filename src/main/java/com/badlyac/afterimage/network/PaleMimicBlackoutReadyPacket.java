package com.badlyac.afterimage.network;

import com.badlyac.afterimage.monster.palemimic.MimicReactionGoal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PaleMimicBlackoutReadyPacket {

    public static void encode(PaleMimicBlackoutReadyPacket msg, FriendlyByteBuf buf) {
    }

    public static PaleMimicBlackoutReadyPacket decode(FriendlyByteBuf buf) {
        return new PaleMimicBlackoutReadyPacket();
    }

    public static void handle(PaleMimicBlackoutReadyPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                MimicReactionGoal.finishCaptureFor(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
