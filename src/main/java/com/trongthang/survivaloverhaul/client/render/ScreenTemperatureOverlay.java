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

        Identifier texture = null;
        float opacity = 0.0f;

        if (temperature <= 5.0f) { // FROSTBITE limit
            texture = FROSTBITE_EFFECT;
            opacity = 0.75f;
        } else if (temperature < 15.0f) { // COLD
            texture = FROSTBITE_EFFECT;
            opacity = 0.35f * ((15.0f - temperature) / 10.0f); // scales dynamically
        } else if (temperature >= 32.5f) { // HEAT_STROKE
            texture = HEAT_STROKE_EFFECT;
            opacity = 0.6f;
        } else if (temperature > 25.0f) { // HOT
            texture = HEAT_STROKE_EFFECT;
            opacity = 0.25f * ((temperature - 25.0f) / 7.5f);
        }

        if (texture != null && opacity > 0.0f) {
            int width = context.getScaledWindowWidth();
            int height = context.getScaledWindowHeight();

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, opacity);
            context.drawTexture(texture, 0, 0, 0, 0, width, height, width, height);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.disableBlend();
        }
    }
}
