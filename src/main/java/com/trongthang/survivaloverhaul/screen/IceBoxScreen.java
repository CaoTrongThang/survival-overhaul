package com.trongthang.survivaloverhaul.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.trongthang.survivaloverhaul.SurvivalOverhaul;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class IceBoxScreen extends HandledScreen<IceBoxScreenHandler> {
    private static final Identifier TEXTURE = new Identifier(SurvivalOverhaul.MOD_ID,
            "textures/gui/ice_box_screen.png");

    public IceBoxScreen(IceBoxScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        if (title.getString().isEmpty()) {
            titleX = -1000;
        } else {
            titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
        }
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        if (!this.title.getString().isEmpty()) {
            context.drawText(this.textRenderer, this.title, this.titleX, this.titleY, 4210752, false);
        }
        context.drawText(this.textRenderer, this.playerInventoryTitle, this.playerInventoryTitleX,
                this.playerInventoryTitleY, 4210752, false);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;

        context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight);

        // Fuel level (animation)
        if (handler.isLit()) {
            int l = handler.getFuelProgress();
            // FUEL_LEVEL_HEIGHT is 27, rendered from bottom up
            context.drawTexture(TEXTURE, x + 73, y + 35 + (27 - l), 176, 24 + (27 - l), 29, l);
        }

        // Powered status (redstone icon)
        if (handler.isPowered()) {
            context.drawTexture(TEXTURE, x + 137, y + 37, 176, 0, 12, 24);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }
}
