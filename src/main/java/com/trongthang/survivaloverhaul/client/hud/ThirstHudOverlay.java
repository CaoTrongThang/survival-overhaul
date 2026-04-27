package com.trongthang.survivaloverhaul.client.hud;

import com.trongthang.survivaloverhaul.SurvivalOverhaul;
import com.trongthang.survivaloverhaul.mechanics.thirst.IThirstData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public class ThirstHudOverlay {

    private static final Identifier THIRST_ICONS = new Identifier(SurvivalOverhaul.MOD_ID,
            "textures/gui/overlay.png");

    public static void render(DrawContext drawContext, MinecraftClient client, int width, int height) {
        if (client != null && client.player != null && !client.player.getAbilities().creativeMode
                && !client.player.isSpectator()) {

            // vanilla standard right side (e.g. food bar)
            int x = width / 2 + 91;
            int y = height - 49;

            PlayerEntity player = client.player;
            int thirst = ((IThirstData) player).survivalOverhaul$getThirstManager().getThirstLevel();
            boolean isThirsty = player.hasStatusEffect(com.trongthang.survivaloverhaul.effect.ModEffects.THIRST);

            // Render 10 icons
            for (int i = 0; i < 10; i++) {
                int drawX = x - (i * 8) - 9; // align right, shift left for each icon

                // Draw background (empty)
                drawContext.drawTexture(THIRST_ICONS, drawX, y, 0, 0, 9, 9, 256, 256);

                // Draw full or half
                if (thirst > i * 2) {
                    int textureX = isThirsty ? 27 : 0;
                    if (thirst == i * 2 + 1) {
                        // Half
                        drawContext.drawTexture(THIRST_ICONS, drawX, y, textureX + 18, 0, 9, 9, 256, 256);
                    } else {
                        // Full
                        drawContext.drawTexture(THIRST_ICONS, drawX, y, textureX + 9, 0, 9, 9, 256, 256);
                    }
                }
            }
        }
    }
}
