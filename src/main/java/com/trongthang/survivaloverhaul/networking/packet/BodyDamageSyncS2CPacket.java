package com.trongthang.survivaloverhaul.networking.packet;

import com.trongthang.survivaloverhaul.client.ClientBodyDamageManager;
import com.trongthang.survivaloverhaul.mechanics.bodyparts.BodyPart;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;

import java.util.HashMap;
import java.util.Map;

public class BodyDamageSyncS2CPacket {
    public static void receive(MinecraftClient client, ClientPlayNetworkHandler handler,
            PacketByteBuf buf, PacketSender responseSender) {

        Map<BodyPart, Float> receivedHealthList = new HashMap<>();
        for (BodyPart part : BodyPart.values()) {
            float health = buf.readFloat();
            receivedHealthList.put(part, health);
            // System.out.println("[SurvivalOverhaul] Synced part " + part.name() + ": " +
            // health);
        }

        client.execute(() -> {
            for (Map.Entry<BodyPart, Float> entry : receivedHealthList.entrySet()) {
                ClientBodyDamageManager.setHealth(entry.getKey(), entry.getValue());
            }
        });
    }
}
