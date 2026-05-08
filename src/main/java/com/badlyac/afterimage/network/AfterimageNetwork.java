package com.badlyac.afterimage.network;

import com.badlyac.afterimage.AfterimageMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class AfterimageNetwork {

    public static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(AfterimageMod.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int index = 0;

    public static void register() {
        CHANNEL.registerMessage(
                index++,
                AfterimageStateSyncPacket.class,
                AfterimageStateSyncPacket::encode,
                AfterimageStateSyncPacket::decode,
                AfterimageStateSyncPacket::handle
        );
    }
}
