package com.trongthang.survivaloverhaul.client.hud;

import com.trongthang.survivaloverhaul.mechanics.bodyparts.BodyPart;
import com.trongthang.survivaloverhaul.mechanics.bodyparts.IBodyDamageData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import com.mojang.blaze3d.systems.RenderSystem;

public class BodyDamageHud {
    private static final Identifier ICONS = new Identifier("survivaloverhaul", "textures/gui/overlay.png");
    private static final int TEX_WIDTH = 16;

    public static void render(DrawContext context, MinecraftClient client, int scaledWidth, int scaledHeight) {
        if (client.player == null || client.options.hudHidden)
            return;

        // Match LSO general placement above hunger bar (right side)
        int xBase = scaledWidth / 2 + 92;
        int yBase = scaledHeight - 33;

        RenderSystem.enableBlend();

        // Draw each limb
        drawLimb(context, xBase, yBase, BodyPart.HEAD);
        drawLimb(context, xBase, yBase, BodyPart.TORSO);
        drawLimb(context, xBase, yBase, BodyPart.LEFT_ARM);
        drawLimb(context, xBase, yBase, BodyPart.RIGHT_ARM);
        drawLimb(context, xBase, yBase, BodyPart.LEFT_LEG);
        drawLimb(context, xBase, yBase, BodyPart.RIGHT_LEG);
        drawLimb(context, xBase, yBase, BodyPart.LEFT_FOOT);
        drawLimb(context, xBase, yBase, BodyPart.RIGHT_FOOT);

        RenderSystem.disableBlend();
    }

    private static void drawLimb(DrawContext context, int xBase, int yBase, BodyPart part) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null)
            return;

        IBodyDamageData data = (IBodyDamageData) client.player;
        float health = data.survivalOverhaul$getBodyDamageManager().getHealth(part);
        float healthRatio = health / part.getMaxHealth();

        int conditionX = 0;
        if (healthRatio <= 0.0f)
            conditionX = 4;
        else if (healthRatio < 0.33f)
            conditionX = 3;
        else if (healthRatio < 0.66f)
            conditionX = 2;
        else if (healthRatio < 1.0f)
            conditionX = 1;

        int offsetX = conditionX * TEX_WIDTH;

        // Render parameters variables
        int x = xBase;
        int y = yBase;
        int texX = 0;
        int texY = 0;
        int width = 0;
        int height = 0;

        if (part == BodyPart.HEAD) {
            x += 4;
            y += 0;
            texX = 4;
            texY = 136;
            width = 8;
            height = 8;
        } else if (part == BodyPart.LEFT_ARM) {
            x += 0;
            y += 8;
            texX = 0;
            texY = 144;
            width = 4;
            height = 12;
        } else if (part == BodyPart.RIGHT_ARM) {
            x += 12;
            y += 8;
            texX = 12;
            texY = 144;
            width = 4;
            height = 12;
        } else if (part == BodyPart.TORSO) {
            x += 4;
            y += 8;
            texX = 4;
            texY = 144;
            width = 8;
            height = 12;
        } else if (part == BodyPart.LEFT_LEG) {
            x += 4;
            y += 20;
            texX = 4;
            texY = 156;
            width = 4;
            height = 8;
        } else if (part == BodyPart.RIGHT_LEG) {
            x += 8;
            y += 20;
            texX = 8;
            texY = 156;
            width = 4;
            height = 8;
        } else if (part == BodyPart.LEFT_FOOT) {
            x += 4;
            y += 28;
            texX = 4;
            texY = 164;
            width = 4;
            height = 4;
        } else if (part == BodyPart.RIGHT_FOOT) {
            x += 8;
            y += 28;
            texX = 8;
            texY = 164;
            width = 4;
            height = 4;
        }

        context.drawTexture(ICONS, x, y, offsetX + texX, texY, width, height);
    }
}
