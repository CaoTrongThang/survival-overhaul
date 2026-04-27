package com.trongthang.survivaloverhaul.mixin.client;

import com.trongthang.survivaloverhaul.client.hud.ThirstHudOverlay;
import com.trongthang.survivaloverhaul.client.hud.BodyDamageHud;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
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
        if (com.trongthang.survivaloverhaul.config.ModConfig.enableThirst) {
            ThirstHudOverlay.render(context, this.client, this.scaledWidth, this.scaledHeight);
        }
        BodyDamageHud.render(context, this.client, this.scaledWidth, this.scaledHeight);
    }
}
