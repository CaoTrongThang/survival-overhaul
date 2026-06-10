package com.trongthang.survivaloverhaul.mixin.client;

import com.trongthang.survivaloverhaul.networking.NetworkingConstants;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;

import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Shadow
    public ClientPlayerEntity player;

    @Shadow
    public HitResult crosshairTarget;

    @Redirect(method = "doItemUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;interactBlock(Lnet/minecraft/client/network/ClientPlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;"))
    private ActionResult survivalOverhaul$onInteractBlock(ClientPlayerInteractionManager manager,
            ClientPlayerEntity player, Hand hand, BlockHitResult hitResult) {
        ActionResult result = manager.interactBlock(player, hand, hitResult);

        if (hand == Hand.MAIN_HAND && !result.isAccepted()) {
            if (this.player != null && this.player.getStackInHand(Hand.MAIN_HAND).isEmpty()
                    && this.player.getStackInHand(Hand.OFF_HAND).isEmpty()) {
                ClientPlayNetworking.send(NetworkingConstants.DRINKING_REQUEST_ID, PacketByteBufs.empty());
            }
        }

        return result;
    }

    @Inject(method = "doItemUse", at = @At("HEAD"))
    private void survivalOverhaul$onItemUse(CallbackInfo ci) {
        if (player != null && player.getStackInHand(Hand.MAIN_HAND).isEmpty()
                && player.getStackInHand(Hand.OFF_HAND).isEmpty()
                && crosshairTarget != null
                && crosshairTarget.getType() == HitResult.Type.MISS) {
            ClientPlayNetworking.send(NetworkingConstants.DRINKING_REQUEST_ID, PacketByteBufs.empty());
        }
    }
}
