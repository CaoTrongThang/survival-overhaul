package com.trongthang.survivaloverhaul.client.hud;

import com.trongthang.survivaloverhaul.SurvivalOverhaul;
import com.trongthang.survivaloverhaul.mechanics.temperature.ITemperatureData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Random;

public class FoodHudOverlay {
    private static final Identifier ICONS = new Identifier(SurvivalOverhaul.MOD_ID, "textures/gui/overlay.png");
    private static final Random random = new Random();

    public static boolean shouldRenderCustomFoodBar(PlayerEntity player) {
        if (player instanceof ITemperatureData tempData) {
            return tempData.survivalOverhaul$getTemperatureManager().isCold();
        }
        return false;
    }

    public static void render(DrawContext context, MinecraftClient client, int width, int height) {
        PlayerEntity player = client.player;
        if (player == null)
            return;

        int foodLevel = player.getHungerManager().getFoodLevel();
        int left = width / 2 + 91;
        int top = height - 39;

        random.setSeed(player.age * 312284L);

        for (int i = 0; i < 10; ++i) {
            int x = left - i * 8 - 9;
            int y = top;

            // Shaking logic similar to vanilla hunger
            if (player.getHungerManager().getSaturationLevel() <= 0.0F && player.age % (foodLevel * 3 + 1) == 0) {
                y += random.nextInt(3) - 1;
            }

            int u = 54; // Start of Cold Hunger icons (6 * 9)
            int v = 9; // Row 2 (y=9)

            if (player.hasStatusEffect(StatusEffects.HUNGER)) {
                u += 27; // Hunger effect variants (3 * 9)
            }

            // 1. Draw Background (Empty)
            context.drawTexture(ICONS, x, y, u, v, 9, 9, 256, 256);

            // 2. Draw Full/Half
            if (i * 2 + 1 < foodLevel) {
                // Full
                context.drawTexture(ICONS, x, y, u + 9, v, 9, 9, 256, 256);
            } else if (i * 2 + 1 == foodLevel) {
                // Half
                context.drawTexture(ICONS, x, y, u + 18, v, 9, 9, 256, 256);
            }
        }
    }
}
