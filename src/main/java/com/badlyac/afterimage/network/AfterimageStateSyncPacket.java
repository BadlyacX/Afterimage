package com.badlyac.afterimage.network;

import com.badlyac.afterimage.client.AfterimageClient;
import com.badlyac.afterimage.state.AfterimageState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class AfterimageStateSyncPacket {
    private final boolean inAfterimage;

    public AfterimageStateSyncPacket(boolean inAfterimage) {
        this.inAfterimage = inAfterimage;
    }

    public static void encode(AfterimageStateSyncPacket msg, FriendlyByteBuf buf) {
         buf.writeBoolean(msg.inAfterimage);
    }

    public static AfterimageStateSyncPacket decode(FriendlyByteBuf buf) {
        return new AfterimageStateSyncPacket(buf.readBoolean());
    }

    public static void handle(AfterimageStateSyncPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork((() -> {
            AfterimageClient.syncAfterimageState(msg.inAfterimage);
        }));
    }

    public static void sync(ServerPlayer player) {
        AfterimageNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new AfterimageStateSyncPacket(
                        AfterimageState.isInAfterimage(player)
                )
        );
    }
}
