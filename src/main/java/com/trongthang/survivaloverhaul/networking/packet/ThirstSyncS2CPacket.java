package com.trongthang.survivaloverhaul.networking.packet;

import com.trongthang.survivaloverhaul.mechanics.thirst.IThirstData;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public class ThirstSyncS2CPacket {

    public static void receive(MinecraftClient client, ClientPlayNetworkHandler handler,
            PacketByteBuf buf, PacketSender responseSender) {
        int thirst = buf.readInt();
        float saturation = buf.readFloat();

        client.execute(() -> {
            if (client.player != null) {
                ((IThirstData) client.player).survivalOverhaul$getThirstManager().setThirstLevel(thirst);
                ((IThirstData) client.player).survivalOverhaul$getThirstManager().setThirstSaturationLevel(saturation);
            }
        });
    }

    public static void send(ServerPlayerEntity player, int thirst, float saturation) {
        // We will do this method in ThirstManager directly or inside a common place,
        // but keeping it simple here
    }
}
