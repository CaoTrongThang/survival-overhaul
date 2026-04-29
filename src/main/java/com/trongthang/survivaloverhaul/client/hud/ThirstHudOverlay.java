package com.trongthang.survivaloverhaul.client.hud;

import com.trongthang.survivaloverhaul.SurvivalOverhaul;
import com.trongthang.survivaloverhaul.effect.ModEffects;
import com.trongthang.survivaloverhaul.mechanics.temperature.ITemperatureData;
import com.trongthang.survivaloverhaul.mechanics.thirst.IThirstData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Random;

public class ThirstHudOverlay {

    private static final Identifier THIRST_ICONS = new Identifier(SurvivalOverhaul.MOD_ID,
            "textures/gui/overlay.png");
    private static final Random random = new Random();

    public static void render(DrawContext drawContext, MinecraftClient client, int width, int height) {
        if (client != null && client.player != null && !client.player.getAbilities().creativeMode
                && !client.player.isSpectator()) {

            // vanilla standard right side (e.g. food bar)
            int x = width / 2 + 91;
            int y = height - 49;

            PlayerEntity player = client.player;
            var thirstManager = ((IThirstData) player).survivalOverhaul$getThirstManager();
            int thirst = thirstManager.getThirstLevel();
            float hydration = thirstManager.getThirstSaturationLevel();
            boolean isThirsty = player.hasStatusEffect(ModEffects.THIRST);

            random.setSeed((long) (player.age * 312284));

            boolean isHot = ((ITemperatureData) player).survivalOverhaul$getTemperatureManager().isHot();
            int textureY = isHot ? 9 : 0; // Row 2 (y=9) is for "hot" thirst variants

            // Render 10 icons
            for (int i = 0; i < 10; i++) {
                int drawX = x - (i * 8) - 9; // align right, shift left for each icon
                int drawY = y;

                if (hydration <= 0.0F && player.age % (thirst * 3 + 1) == 0) {
                    drawY += random.nextInt(3) - 1;
                }

                // Draw background (empty)
                drawContext.drawTexture(THIRST_ICONS, drawX, drawY, 0, textureY, 9, 9, 256, 256);

                // Draw full or half
                if (thirst > i * 2) {
                    int textureX = isThirsty ? 27 : 0;
                    if (thirst == i * 2 + 1) {
                        // Half
                        drawContext.drawTexture(THIRST_ICONS, drawX, drawY, textureX + 18, textureY, 9, 9, 256, 256);
                    } else {
                        // Full
                        drawContext.drawTexture(THIRST_ICONS, drawX, drawY, textureX + 9, textureY, 9, 9, 256, 256);
                    }
                }
            }
        }
    }
}
