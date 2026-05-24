package com.badlyac.afterimage.network;

import com.badlyac.afterimage.client.AfterimageClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class PaleMimicCapturePacket {
    private final double targetX;
    private final double targetY;
    private final double targetZ;

    public PaleMimicCapturePacket(Vec3 target) {
        this(target.x, target.y, target.z);
    }

    private PaleMimicCapturePacket(double targetX, double targetY, double targetZ) {
        this.targetX = targetX;
        this.targetY = targetY;
        this.targetZ = targetZ;
    }

    public static void encode(PaleMimicCapturePacket msg, FriendlyByteBuf buf) {
        buf.writeDouble(msg.targetX);
        buf.writeDouble(msg.targetY);
        buf.writeDouble(msg.targetZ);
    }

    public static PaleMimicCapturePacket decode(FriendlyByteBuf buf) {
        return new PaleMimicCapturePacket(
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble()
        );
    }

    public static void handle(PaleMimicCapturePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> AfterimageClient.startPaleMimicCapture(
                new Vec3(msg.targetX, msg.targetY, msg.targetZ)
        ));
        ctx.get().setPacketHandled(true);
    }

    public static void send(ServerPlayer player, Vec3 target) {
        AfterimageNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new PaleMimicCapturePacket(target)
        );
    }
}
