package com.trongthang.survivaloverhaul.client.tooltip;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.Identifier;
import com.trongthang.survivaloverhaul.SurvivalOverhaul;

public class HydrationClientTooltipComponent implements TooltipComponent {
    public static final Identifier ICONS = new Identifier(SurvivalOverhaul.MOD_ID, "textures/gui/overlay.png");
    public static final int THIRST_TEXTURE_WIDTH = 9;
    public static final int THIRST_TEXTURE_HEIGHT = 9;

    public final int hydration;
    public final float saturation;
    public int hydrationIconNumber;

    public HydrationClientTooltipComponent(HydrationTooltipData data) {
        this.hydration = data.hydration();
        this.saturation = data.saturation();
        this.hydrationIconNumber = Math.min((int) Math.ceil(Math.abs(hydration) / 2f), 10);
    }

    @Override
    public int getHeight() {
        return 14;
    }

    @Override
    public int getWidth(TextRenderer textRenderer) {
        return hydrationIconNumber * THIRST_TEXTURE_WIDTH;
    }

    @Override
    public void drawItems(TextRenderer textRenderer, int tooltipX, int tooltipY, DrawContext context) {
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        int left = tooltipX + (this.hydrationIconNumber - 1) * THIRST_TEXTURE_WIDTH;
        int top = tooltipY + 2;

        for (int i = 0; i < this.hydrationIconNumber; i++) {
            int halfIcon = i * 2 + 1;
            int x = left - i * THIRST_TEXTURE_WIDTH;
            int y = top;

            if (halfIcon < Math.abs(this.hydration)) {
                // Full thirst icon
                context.drawTexture(ICONS, x, y, 9, 0, THIRST_TEXTURE_WIDTH, THIRST_TEXTURE_HEIGHT, 256, 256);
            } else if (halfIcon == Math.abs(this.hydration)) {
                // Half thirst icon
                context.drawTexture(ICONS, x, y, 18, 0, THIRST_TEXTURE_WIDTH, THIRST_TEXTURE_HEIGHT, 256, 256);
            }
        }

        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
    }
}
