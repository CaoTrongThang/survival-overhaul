package com.trongthang.survivaloverhaul.client.screen;

import com.trongthang.survivaloverhaul.client.ClientBodyDamageManager;
import com.trongthang.survivaloverhaul.mechanics.bodyparts.BodyPart;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import com.trongthang.survivaloverhaul.networking.NetworkingConstants;
import net.minecraft.util.Identifier;
import com.mojang.blaze3d.systems.RenderSystem;

public class BodyDamageScreen extends Screen {
    private static final Identifier BG_TEXTURE = new Identifier("survivaloverhaul",
            "textures/gui/body_health_screen.png");

    private final boolean isBandageMode;
    private int bandageHealsRemaining = 3;

    private static final int BG_WIDTH = 176;
    private static final int BG_HEIGHT = 183;

    private static final int HEALTH_BAR_WIDTH = 30;
    private static final int HEALTH_BAR_HEIGHT = 5;
    private static final int TEX_HEALTH_BAR_X = 176;
    private static final int TEX_HEALTH_BAR_Y = 0;

    public BodyDamageScreen(boolean isBandageMode) {
        super(Text.translatable("screen.survivaloverhaul.body_damage"));
        this.isBandageMode = isBandageMode;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);

        int leftPos = (this.width - BG_WIDTH) / 2;
        int topPos = (this.height - BG_HEIGHT) / 2;

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        context.drawTexture(BG_TEXTURE, leftPos, topPos, 0, 0, BG_WIDTH, BG_HEIGHT);

        if (isBandageMode) {
            context.drawCenteredTextWithShadow(this.textRenderer,
                    Text.literal("Bandage Mode: Heals Remaining " + bandageHealsRemaining), this.width / 2, topPos - 15,
                    0x00FF00);
        }

        // Render health bars
        drawHealthBar(context, leftPos + 72, topPos + 46, BodyPart.HEAD, mouseX, mouseY);
        drawHealthBar(context, leftPos + 72, topPos + 92, BodyPart.TORSO, mouseX, mouseY);
        drawHealthBar(context, leftPos + 27, topPos + 80, BodyPart.LEFT_ARM, mouseX, mouseY);
        drawHealthBar(context, leftPos + 118, topPos + 80, BodyPart.RIGHT_ARM, mouseX, mouseY);
        drawHealthBar(context, leftPos + 39, topPos + 134, BodyPart.LEFT_LEG, mouseX, mouseY);
        drawHealthBar(context, leftPos + 106, topPos + 134, BodyPart.RIGHT_LEG, mouseX, mouseY);

        // Render hover text (Tooltips)
        drawTooltip(context, leftPos + 68, topPos + 46, 38, 34, BodyPart.HEAD, mouseX, mouseY);
        drawTooltip(context, leftPos + 73, topPos + 79, 28, 38, BodyPart.TORSO, mouseX, mouseY);
        drawTooltip(context, leftPos + 23, topPos + 79, 50, 38, BodyPart.LEFT_ARM, mouseX, mouseY);
        drawTooltip(context, leftPos + 101, topPos + 79, 50, 38, BodyPart.RIGHT_ARM, mouseX, mouseY);
        drawTooltip(context, leftPos + 38, topPos + 117, 49, 46, BodyPart.LEFT_LEG, mouseX, mouseY);
        drawTooltip(context, leftPos + 87, topPos + 117, 49, 46, BodyPart.RIGHT_LEG, mouseX, mouseY);

        super.render(context, mouseX, mouseY, delta);
    }

    private void drawHealthBar(DrawContext context, int x, int y, BodyPart part, int mouseX, int mouseY) {
        float health = ClientBodyDamageManager.getHealth(part);
        float maxHealth = part.getMaxHealth();
        float ratio = health / maxHealth;

        int conditionY = 0;
        if (ratio <= 0.0f)
            conditionY = 3;
        else if (ratio < 0.33f)
            conditionY = 2; // heavily wounded
        else if (ratio < 0.66f)
            conditionY = 1; // wounded
        else
            conditionY = 0; // healthy

        // Draw empty health bar first (Dead/Empty background)
        context.drawTexture(BG_TEXTURE, x, y, TEX_HEALTH_BAR_X, TEX_HEALTH_BAR_Y + HEALTH_BAR_HEIGHT * 3,
                HEALTH_BAR_WIDTH, HEALTH_BAR_HEIGHT);

        int filledWidth = (int) Math.ceil(HEALTH_BAR_WIDTH * ratio);
        if (filledWidth > 0) {
            context.drawTexture(BG_TEXTURE, x, y, TEX_HEALTH_BAR_X, TEX_HEALTH_BAR_Y + HEALTH_BAR_HEIGHT * conditionY,
                    filledWidth, HEALTH_BAR_HEIGHT);
        }
    }

    private void drawTooltip(DrawContext context, int x, int y, int width, int height, BodyPart part, int mouseX,
            int mouseY) {
        if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height) {
            float health = ClientBodyDamageManager.getHealth(part);
            context.drawTooltip(this.textRenderer, Text.literal(part.getDisplayName() + ": "
                    + String.format("%.1f", health) + " / " + String.format("%.1f", part.getMaxHealth())), mouseX,
                    mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isBandageMode && bandageHealsRemaining > 0) {
            BodyPart clickedPart = getClickedPart((int) mouseX, (int) mouseY);
            if (clickedPart != null && ClientBodyDamageManager.getHealth(clickedPart) < clickedPart.getMaxHealth()) {
                // Heals the part client-side prediction
                ClientBodyDamageManager.setHealth(clickedPart, clickedPart.getMaxHealth());
                bandageHealsRemaining--;

                sendHealLimbPacket(clickedPart);

                if (bandageHealsRemaining <= 0) {
                    this.close();
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private BodyPart getClickedPart(int mouseX, int mouseY) {
        int leftPos = (this.width - BG_WIDTH) / 2;
        int topPos = (this.height - BG_HEIGHT) / 2;

        if (isInside(mouseX, mouseY, leftPos + 68, topPos + 46, 38, 34))
            return BodyPart.HEAD;
        if (isInside(mouseX, mouseY, leftPos + 73, topPos + 79, 28, 38))
            return BodyPart.TORSO;
        if (isInside(mouseX, mouseY, leftPos + 23, topPos + 79, 50, 38))
            return BodyPart.LEFT_ARM;
        if (isInside(mouseX, mouseY, leftPos + 101, topPos + 79, 50, 38))
            return BodyPart.RIGHT_ARM;
        if (isInside(mouseX, mouseY, leftPos + 38, topPos + 117, 49, 46))
            return BodyPart.LEFT_LEG;
        if (isInside(mouseX, mouseY, leftPos + 87, topPos + 117, 49, 46))
            return BodyPart.RIGHT_LEG;

        return null;
    }

    private boolean isInside(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    private void sendHealLimbPacket(BodyPart part) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(part.name());
        ClientPlayNetworking.send(NetworkingConstants.HEAL_LIMB_C2S_ID, buf);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
