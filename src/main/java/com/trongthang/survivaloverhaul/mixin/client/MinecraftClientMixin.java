package com.trongthang.survivaloverhaul.mixin.client;

import com.trongthang.survivaloverhaul.networking.NetworkingConstants;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Shadow
    public ClientPlayerEntity player;

    @Shadow
    public HitResult crosshairTarget;

    @Inject(method = "doItemUse", at = @At("HEAD"))
    private void survivalOverhaul$onItemUse(CallbackInfo ci) {
        if (player != null && player.getStackInHand(Hand.MAIN_HAND).isEmpty()
                && player.getStackInHand(Hand.OFF_HAND).isEmpty()
                && crosshairTarget != null && crosshairTarget.getType() == HitResult.Type.MISS) {
            ClientPlayNetworking.send(NetworkingConstants.DRINKING_REQUEST_ID, PacketByteBufs.empty());
        }
    }
}
