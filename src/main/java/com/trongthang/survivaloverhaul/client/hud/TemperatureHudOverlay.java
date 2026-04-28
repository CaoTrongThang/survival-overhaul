package com.trongthang.survivaloverhaul.client.hud;

import com.trongthang.survivaloverhaul.SurvivalOverhaul;
import com.trongthang.survivaloverhaul.effect.ModEffects;
import com.trongthang.survivaloverhaul.mechanics.temperature.ITemperatureData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

import static com.trongthang.survivaloverhaul.SurvivalOverhaul.LOGGER;

public class TemperatureHudOverlay {

    private static final Identifier ICONS = new Identifier(SurvivalOverhaul.MOD_ID, "textures/gui/overlay.png");

    private static final int TEMPERATURE_TEXTURE_POS_Y = 48;
    private static final int TEMPERATURE_TEXTURE_WIDTH = 16;
    private static final int TEMPERATURE_TEXTURE_HEIGHT = 16;

    /**
     * UV Mapping Guide for overlay.png:
     * - The icons are 16x16.
     * - u = Column Index * 16
     * - v = Row Index * 16 (Row 4 starts at v=48)
     * e.g. To change the snowflake to a new icon at column 15, row 0:
     * Change iconToDraw (u-multiplier) to 14 and TEMPERATURE_TEXTURE_POS_Y (v) to
     * 0.
     */
    public static void render(DrawContext context, MinecraftClient client, int width, int height) {
        if (client == null || client.player == null)
            return;
        if (client.options.hudHidden || client.player.getAbilities().creativeMode || client.player.isSpectator())
            return;

        PlayerEntity player = client.player;
        float temperature = ((ITemperatureData) player).survivalOverhaul$getTemperatureManager().getTemperature();
        float target = ((ITemperatureData) player).survivalOverhaul$getTemperatureManager().getTargetTemperature();

        int x = width / 2 - (TEMPERATURE_TEXTURE_WIDTH / 2);
        int y = height - 52;

        int iconToDraw = 3; // NORMAL Main

        boolean hasFrostbite = player.hasStatusEffect(ModEffects.FROSTBITE);
        boolean hasHeatstroke = player.hasStatusEffect(ModEffects.HEATSTROKE);

        boolean isDying = temperature <= 5.0f || temperature >= 32.5f || hasFrostbite || hasHeatstroke;
        boolean isHurt = player.hurtTime > 0;

        if (temperature <= 5.0f || hasFrostbite) {
            iconToDraw = isHurt ? 8 : 5;
        } else if (temperature < 15.0f) {
            iconToDraw = 5;
        } else if (temperature >= 32.5f || hasHeatstroke) {
            iconToDraw = isHurt ? 7 : 4;
        } else if (temperature > 25.0f) {
            iconToDraw = 11;
        }

        int animCycle = player.age % 100;
        boolean animating = Math.abs(target - temperature) > 0.5f && animCycle < 24;

        int shakeX = 0;
        int shakeY = 0;
        if (isDying) {
            if ((player.age / 2) % 2 == 0) {
                shakeX = 1;
                shakeY = 1;
            } else {
                shakeX = -1;
                shakeY = -1;
            }
        }

        x += shakeX;
        y += shakeY;

        // Draw Single Layer Icon
        context.drawTexture(ICONS, x, y, TEMPERATURE_TEXTURE_WIDTH * iconToDraw, TEMPERATURE_TEXTURE_POS_Y,
                TEMPERATURE_TEXTURE_WIDTH, TEMPERATURE_TEXTURE_HEIGHT, 256, 256);

        // Animating Arrows
        if (animating) {
            boolean rising = target > 20;
            int frame = (24 - animCycle) / 2 - 1;
            if (frame >= 0) {
                int arrowX = TEMPERATURE_TEXTURE_WIDTH * frame;
                int arrowY = TEMPERATURE_TEXTURE_POS_Y + (TEMPERATURE_TEXTURE_HEIGHT * (rising ? 1 : 2));

                context.drawTexture(ICONS, x, y, arrowX, arrowY, TEMPERATURE_TEXTURE_WIDTH, TEMPERATURE_TEXTURE_HEIGHT,
                        256, 256);
            }
        }
    }
}
