package com.trongthang.survivaloverhaul.mixin.client;

import com.trongthang.survivaloverhaul.client.hud.BodyDamageHud;
import com.trongthang.survivaloverhaul.client.hud.FoodHudOverlay;
import com.trongthang.survivaloverhaul.client.hud.TemperatureHudOverlay;
import com.trongthang.survivaloverhaul.client.hud.ThirstHudOverlay;
import com.trongthang.survivaloverhaul.config.ModConfig;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    private int scaledWidth;

    @Shadow
    private int scaledHeight;

    @Inject(method = "renderStatusBars", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", ordinal = 1))
    private void survivalOverhaul$renderThirstBar(DrawContext context, CallbackInfo ci) {
        if (ModConfig.enableThirst) {
            ThirstHudOverlay.render(context, this.client, this.scaledWidth, this.scaledHeight);
        }
        if (ModConfig.enableTemperature) {
            TemperatureHudOverlay.render(context, this.client, this.scaledWidth, this.scaledHeight);
        }

        if (ModConfig.enableTemperature && FoodHudOverlay.shouldRenderCustomFoodBar(this.client.player)) {
            FoodHudOverlay.render(context, this.client, this.scaledWidth, this.scaledHeight);
        }

        if (ModConfig.enableBodyDamage) {
            BodyDamageHud.render(context, this.client, this.scaledWidth, this.scaledHeight);
        }
    }

    @WrapOperation(method = "renderStatusBars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V"))
    private void survivalOverhaul$silenceVanillaFood(DrawContext instance, Identifier texture, int x, int y, int u,
            int v, int width, int height, Operation<Void> original) {
        // v = 27 indicates the food/hunger row in icons.png
        if (v == 27 && FoodHudOverlay.shouldRenderCustomFoodBar(this.client.player) && ModConfig.enableTemperature) {
            return; // Silence vanilla drawing
        }
        original.call(instance, texture, x, y, u, v, width, height);
    }
}
