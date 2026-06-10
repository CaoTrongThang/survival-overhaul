package com.trongthang.survivaloverhaul.networking.packet;

import com.trongthang.survivaloverhaul.mechanics.poop.IPoopData;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;

public class PoopSyncS2CPacket {

    public static void receive(MinecraftClient client, ClientPlayNetworkHandler handler,
            PacketByteBuf buf, PacketSender responseSender) {
        int poopLevel = buf.readInt();

        client.execute(() -> {
            if (client.player != null) {
                ((IPoopData) client.player).survivalOverhaul$getPoopManager().setPoopLevel(poopLevel);
            }
        });
    }
}
