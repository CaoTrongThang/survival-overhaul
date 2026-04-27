package com.trongthang.survivaloverhaul.networking;

import com.trongthang.survivaloverhaul.mechanics.thirst.IThirstData;
import com.trongthang.survivaloverhaul.mechanics.bodyparts.IBodyDamageData;
import com.trongthang.survivaloverhaul.mechanics.bodyparts.BodyPart;
import com.trongthang.survivaloverhaul.SurvivalOverhaul;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public class ModNetworking {

    public static void sendThirstSync(ServerPlayerEntity player, int thirstLevel, float saturationLevel) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(thirstLevel);
        buf.writeFloat(saturationLevel);
        ServerPlayNetworking.send(player, NetworkingConstants.THIRST_SYNC_ID, buf);
    }

    public static void sync(ServerPlayerEntity player,
            IThirstData data) {
        sendThirstSync(player, data.survivalOverhaul$getThirstManager().getThirstLevel(),
                data.survivalOverhaul$getThirstManager().getThirstSaturationLevel());
    }

    public static void sendBodyDamageSync(ServerPlayerEntity player, IBodyDamageData data) {
        PacketByteBuf buf = PacketByteBufs.create();
        for (BodyPart part : BodyPart.values()) {
            buf.writeFloat(data.survivalOverhaul$getBodyDamageManager().getHealth(part));
        }
        ServerPlayNetworking.send(player, NetworkingConstants.BODY_DAMAGE_SYNC_ID, buf);
    }

    public static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(NetworkingConstants.HEAL_LIMB_C2S_ID,
                (server, player, handler, buf, responseSender) -> {
                    String partName = buf.readString();
                    server.execute(() -> {
                        try {
                            BodyPart part = BodyPart.valueOf(partName);
                            if (player instanceof IBodyDamageData data) {
                                data.survivalOverhaul$getBodyDamageManager().heal(part, part.getMaxHealth()); // Fully
                                                                                                              // heal
                                                                                                              // the
                                                                                                              // limb
                            }
                        } catch (IllegalArgumentException e) {
                            SurvivalOverhaul.LOGGER.error("Invalid BodyPart received: {}", partName);
                        }
                    });
                });
    }
}
