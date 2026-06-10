package com.trongthang.survivaloverhaul.client.hud;

import com.trongthang.survivaloverhaul.SurvivalOverhaul;
import com.trongthang.survivaloverhaul.mechanics.poop.IPoopData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public class PoopHudOverlay {

    private static final Identifier ICONS = new Identifier(SurvivalOverhaul.MOD_ID,
            "textures/gui/overlay.png");

    // Poop icons are at x=164..191, y=0..7 in overlay.png (4 states × 8px wide, 8px
    // tall)
    // State layout (each 8×8):
    // 164,0 = empty background
    // 172,0 = half filled
    // 180,0 = full filled
    // We only have one row (no hot variant), so textureY = 0.

    private static final int ICON_U_EMPTY = 164;
    private static final int ICON_U_HALF = 173; // 164 + 8 + 1px gap
    private static final int ICON_U_FULL = 182; // 173 + 8 + 1px gap
    private static final int ICON_V = 0;
    private static final int ICON_SIZE = 8;

    public static void render(DrawContext drawContext, MinecraftClient client, int width, int height) {
        if (client == null || client.player == null)
            return;
        PlayerEntity player = client.player;
        if (player.getAbilities().creativeMode || player.isSpectator())
            return;

        int poop = ((IPoopData) player).survivalOverhaul$getPoopManager().getPoopLevel();

        // Same x-anchor as thirst bar; poop bar sits 10px above the thirst bar (y - 10)
        int x = width / 2 + 91;
        int y = height - 49 - 10; // thirst is at height-49, poop is one row above

        for (int i = 0; i < 10; i++) {
            int drawX = x - (i * 8) - 9;
            int drawY = y;

            // Always draw empty background first
            drawContext.drawTexture(ICONS, drawX, drawY, ICON_U_EMPTY, ICON_V, ICON_SIZE, ICON_SIZE, 256, 256);

            int filled = poop - i * 2;
            if (filled >= 2) {
                // Full icon
                drawContext.drawTexture(ICONS, drawX, drawY, ICON_U_FULL, ICON_V, ICON_SIZE, ICON_SIZE, 256, 256);
            } else if (filled == 1) {
                // Half icon
                drawContext.drawTexture(ICONS, drawX, drawY, ICON_U_HALF, ICON_V, ICON_SIZE, ICON_SIZE, 256, 256);
            }
        }
    }
}
