package com.trongthang.survivaloverhaul.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.trongthang.survivaloverhaul.SurvivalOverhaul;
import com.trongthang.survivaloverhaul.config.ModConfig;
import com.trongthang.survivaloverhaul.mechanics.temperature.ITemperatureData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public class ScreenTemperatureOverlay {
    private static final Identifier FROSTBITE_EFFECT = new Identifier(SurvivalOverhaul.MOD_ID,
            "textures/gui/freeze_effect.png");
    private static final Identifier HEAT_STROKE_EFFECT = new Identifier(SurvivalOverhaul.MOD_ID,
            "textures/gui/heat_effect.png");

    private static float currentOpacity = 0.0f;
    private static Identifier lastTexture = null;
    private static long lastRenderTime = 0;

    public static void render(DrawContext context, float tickDelta) {
        if (!ModConfig.enableTemperature)
            return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden)
            return;

        PlayerEntity player = client.player;
        if (player.getAbilities().creativeMode || player.isSpectator())
            return;

        float temperature = 20.0f;
        if (player instanceof ITemperatureData tempData) {
            temperature = tempData.survivalOverhaul$getTemperatureManager().getBodyTemperature();
        }

        Identifier targetTexture = null;
        float targetOpacity = 0.0f;

        float time = player.age + tickDelta;

        if (temperature <= 5.0f) { // FROSTBITE limit
            targetTexture = FROSTBITE_EFFECT;
            targetOpacity = 0.75f + (float) (Math.sin(time * 0.05f) * 0.15f); // Slow pulse
        } else if (temperature < 15.0f) { // COLD
            targetTexture = FROSTBITE_EFFECT;
            targetOpacity = 0.35f * ((15.0f - temperature) / 10.0f);
        } else if (temperature >= 32.5f) { // HEAT_STROKE
            targetTexture = HEAT_STROKE_EFFECT;
            float heartBeat = (float) Math.abs(Math.sin(time * 0.15f)); // Fast heartbeat pulse
            targetOpacity = 0.6f + heartBeat * 0.2f;
        } else if (temperature > 25.0f) { // HOT
            targetTexture = HEAT_STROKE_EFFECT;
            targetOpacity = 0.25f * ((temperature - 25.0f) / 7.5f);
        }

        if (targetTexture != null) {
            lastTexture = targetTexture;
        }

        targetOpacity = Math.max(0.0f, Math.min(1.0f, targetOpacity));

        long currentTime = System.currentTimeMillis();
        if (lastRenderTime == 0)
            lastRenderTime = currentTime;
        float dt = (currentTime - lastRenderTime) / 1000.0f;
        lastRenderTime = currentTime;
        if (dt > 0.1f)
            dt = 0.1f; // Cap delta to prevent huge jumps

        // Smooth opacity transition
        currentOpacity += (targetOpacity - currentOpacity) * (dt * 3.0f); // Fast and smooth lerp

        if (currentOpacity < 0.01f && targetOpacity <= 0.0f) {
            currentOpacity = 0.0f;
            return;
        }

        if (lastTexture != null && currentOpacity > 0.0f) {
            int width = context.getScaledWindowWidth();
            int height = context.getScaledWindowHeight();

            int renderWidth = width;
            int renderHeight = height;
            int x = 0;
            int y = 0;

            if (lastTexture == HEAT_STROKE_EFFECT && currentOpacity > 0.1f) {
                // Heat Stroke: Wavy distortion effect to simulate heat waves and dizziness
                int offsetX = (int) (Math.sin(time * 0.1f) * 8.0f * currentOpacity);
                int offsetY = (int) (Math.cos(time * 0.08f) * 6.0f * currentOpacity);

                renderWidth = width + 16;
                renderHeight = height + 16;
                x = -8 + offsetX;
                y = -8 + offsetY;
            } else if (lastTexture == FROSTBITE_EFFECT && currentOpacity > 0.1f) {
                // Frostbite: Shivering/shaking effect
                int offsetX = (int) (Math.sin(time * 1.5f) * 4.0f * currentOpacity);
                int offsetY = (int) (Math.cos(time * 1.7f) * 4.0f * currentOpacity);

                renderWidth = width + 16;
                renderHeight = height + 16;
                x = -8 + offsetX;
                y = -8 + offsetY;
            }

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, currentOpacity);
            context.drawTexture(lastTexture, x, y, 0, 0, renderWidth, renderHeight, renderWidth, renderHeight);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.disableBlend();
        }
    }
}
