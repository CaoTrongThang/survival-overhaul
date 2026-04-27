package com.trongthang.survivaloverhaul;

import com.trongthang.survivaloverhaul.client.ModClient;
import com.trongthang.survivaloverhaul.client.render.FluidRenderInit;
import com.trongthang.survivaloverhaul.networking.NetworkingConstants;
import com.trongthang.survivaloverhaul.networking.packet.ThirstSyncS2CPacket;
import com.trongthang.survivaloverhaul.networking.packet.BodyDamageSyncS2CPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class SurvivalOverhaulClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        SurvivalOverhaul.LOGGER.info("SurvivalOverhaulClient initializing...");
        FluidRenderInit.registerFluidRenders();
        ModClient.registerClient();
        ClientPlayNetworking.registerGlobalReceiver(
                NetworkingConstants.THIRST_SYNC_ID,
                ThirstSyncS2CPacket::receive);
        ClientPlayNetworking.registerGlobalReceiver(
                NetworkingConstants.BODY_DAMAGE_SYNC_ID,
                BodyDamageSyncS2CPacket::receive);
    }
}
