package com.trongthang.survivaloverhaul.mixin.client;

import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {

    @Inject(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;setSprinting(Z)V", shift = org.spongepowered.asm.mixin.injection.At.Shift.AFTER))
    private void survivalOverhaul$tickMovement(CallbackInfo ci) {
        if (com.trongthang.survivaloverhaul.config.ModConfig.enableThirst
                && !((ClientPlayerEntity) (Object) this).isCreative()) {
            if (((com.trongthang.survivaloverhaul.mechanics.thirst.IThirstData) this)
                    .survivalOverhaul$getThirstManager()
                    .getThirstLevel() <= com.trongthang.survivaloverhaul.config.ModConfig.thirstSprintThreshold) {
                ((ClientPlayerEntity) (Object) this).setSprinting(false);
            }
        }
    }
}
