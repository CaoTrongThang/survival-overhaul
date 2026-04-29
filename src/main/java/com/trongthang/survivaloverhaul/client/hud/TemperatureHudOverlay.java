package com.trongthang.survivaloverhaul.client.hud;

import com.trongthang.survivaloverhaul.SurvivalOverhaul;
import com.trongthang.survivaloverhaul.mechanics.temperature.ITemperatureData;
import com.trongthang.survivaloverhaul.mechanics.temperature.TemperatureManager;
import com.trongthang.survivaloverhaul.mechanics.temperature.TemperatureState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public class TemperatureHudOverlay {

    private static final Identifier ICONS = new Identifier(SurvivalOverhaul.MOD_ID, "textures/gui/overlay.png");

    private static final int TEMPERATURE_TEXTURE_POS_Y = 48;
    private static final int TEMPERATURE_TEXTURE_WIDTH = 16;
    private static final int TEMPERATURE_TEXTURE_HEIGHT = 16;

    private static float lastTemperature = -1.0f;
    private static boolean isRising = false;

    public static void render(DrawContext context, MinecraftClient client, int width, int height) {
        if (client == null || client.player == null)
            return;
        if (client.options.hudHidden || client.player.getAbilities().creativeMode || client.player.isSpectator())
            return;

        PlayerEntity player = client.player;
        TemperatureManager manager = ((ITemperatureData) player).survivalOverhaul$getTemperatureManager();
        float temperature = manager.getBodyTemperature();
        float target = manager.getAmbientTemperature();
        TemperatureState state = manager.getState();

        // Update temperature trend based on actual temperature change
        if (temperature != lastTemperature) {
            if (lastTemperature != -1.0f) {
                isRising = temperature > lastTemperature;
            }
            lastTemperature = temperature;
        }

        int x = width / 2 - (TEMPERATURE_TEXTURE_WIDTH / 2);
        int y = height - 52;

        int iconToDraw = 3; // NORMAL Main

        boolean isDying = state == TemperatureState.FREEZING || state == TemperatureState.HOT;
        boolean isHurt = player.hurtTime > 0;

        switch (state) {
            case FREEZING -> iconToDraw = isHurt ? 8 : 5;
            case COLD -> iconToDraw = isHurt ? 10 : 12;
            case HOT -> iconToDraw = isHurt ? 7 : 4;
            case WARM -> iconToDraw = isHurt ? 9 : 11;
            default -> iconToDraw = 3;
        }

        int animCycle = player.age % 100;
        // Animating if there's a significant difference between current and target
        boolean animating = Math.abs(target - temperature) > 0.1f && animCycle < 24;

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
            int frame = (24 - animCycle) / 2 - 1;
            if (frame >= 0) {
                int arrowX = TEMPERATURE_TEXTURE_WIDTH * frame;
                // isRising 1: Up, 2: Down (according to previous code's logic)
                int arrowY = TEMPERATURE_TEXTURE_POS_Y + (TEMPERATURE_TEXTURE_HEIGHT * (isRising ? 1 : 2));

                context.drawTexture(ICONS, x, y, arrowX, arrowY, TEMPERATURE_TEXTURE_WIDTH, TEMPERATURE_TEXTURE_HEIGHT,
                        256, 256);
            }
        }
    }
}
