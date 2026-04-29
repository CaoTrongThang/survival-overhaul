package com.trongthang.survivaloverhaul.client.hud;

import com.trongthang.survivaloverhaul.mechanics.bodyparts.BodyPart;
import com.trongthang.survivaloverhaul.mechanics.bodyparts.IBodyDamageData;
import com.trongthang.survivaloverhaul.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.EnumMap;
import java.util.Map;

public class BodyDamageHud {
    private static final Identifier ICONS = new Identifier("survivaloverhaul", "textures/gui/overlay.png");
    private static final int TEX_WIDTH = 16;

    private static final Map<BodyPart, Float> lastHealth = new EnumMap<>(BodyPart.class);
    private static final Map<BodyPart, Long> impactTime = new EnumMap<>(BodyPart.class);

    public static void render(DrawContext context, MinecraftClient client, int scaledWidth, int scaledHeight) {
        if (!ModConfig.enableBodyDamage || client.player == null || client.options.hudHidden)
            return;

        // Calculate position based on config
        int xBase = 0;
        int yBase = 0;

        switch (ModConfig.hudPosition) {
            case BOTTOM_RIGHT -> {
                xBase = scaledWidth / 2 + 92;
                yBase = scaledHeight - 36;
            }
            case BOTTOM_LEFT -> {
                xBase = scaledWidth / 2 - 116;
                yBase = scaledHeight - 36;
            }
            case TOP_RIGHT -> {
                xBase = scaledWidth - 34;
                yBase = 10;
            }
            case TOP_LEFT -> {
                xBase = 10;
                yBase = 10;
            }
            case MIDDLE_RIGHT -> {
                xBase = scaledWidth - 34;
                yBase = scaledHeight / 2 - 18;
            }
            case MIDDLE_LEFT -> {
                xBase = 10;
                yBase = scaledHeight / 2 - 18;
            }
            case TOP_MIDDLE -> {
                xBase = scaledWidth / 2 - 12;
                yBase = 10;
            }
        }

        context.getMatrices().push();
        context.getMatrices().translate(xBase + ModConfig.hudXOffset, yBase + ModConfig.hudYOffset, 0);
        context.getMatrices().scale(ModConfig.hudScale, ModConfig.hudScale, 1.0f);

        RenderSystem.enableBlend();

        long currentTime = net.minecraft.util.Util.getMeasuringTimeMs();

        // Draw each limb relative to (0,0) due to translation
        drawLimb(context, 0, 0, BodyPart.HEAD, currentTime);
        drawLimb(context, 0, 0, BodyPart.TORSO, currentTime);
        drawLimb(context, 0, 0, BodyPart.LEFT_ARM, currentTime);
        drawLimb(context, 0, 0, BodyPart.RIGHT_ARM, currentTime);
        drawLimb(context, 0, 0, BodyPart.LEFT_LEG, currentTime);
        drawLimb(context, 0, 0, BodyPart.RIGHT_LEG, currentTime);
        drawLimb(context, 0, 0, BodyPart.LEFT_FOOT, currentTime);
        drawLimb(context, 0, 0, BodyPart.RIGHT_FOOT, currentTime);

        RenderSystem.disableBlend();
        context.getMatrices().pop();
    }

    private static void drawLimb(DrawContext context, int xBase, int yBase, BodyPart part, long currentTime) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null)
            return;

        IBodyDamageData data = (IBodyDamageData) client.player;
        float health = data.survivalOverhaul$getBodyDamageManager().getHealth(part);

        float lastH = lastHealth.getOrDefault(part, -1f);
        if (lastH == -1f) {
            lastHealth.put(part, health);
            lastH = health;
        }

        // If health dropped, trigger impact effect
        if (health < lastH) {
            impactTime.put(part, currentTime);
        }

        lastHealth.put(part, health);

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

        // Apply visual impact effect (shake and flash red)
        long lastImpact = impactTime.getOrDefault(part, 0L);
        long timeSinceImpact = currentTime - lastImpact;
        long impactDuration = 350; // 350 ms duration for the effect

        boolean hasImpact = timeSinceImpact < impactDuration;

        if (hasImpact) {
            float progress = (float) timeSinceImpact / impactDuration; // 0.0 to 1.0

            // Highlight red: start at bright red/orange, fade back to white (normal)
            float gb = 0.2f + (0.8f * progress);
            RenderSystem.setShaderColor(1.0f, gb, gb, 1.0f);

            // Random shake that decays over time
            float intensity = 1.0f - progress;
            int shakeX = (int) (Math.sin(currentTime / 15.0) * 2.5 * intensity);
            int shakeY = (int) (Math.cos(currentTime / 12.0) * 2.5 * intensity);

            x += shakeX;
            y += shakeY;
        }

        context.drawTexture(ICONS, x, y, offsetX + texX, texY, width, height);

        if (hasImpact) {
            // Reset color so other limbs/HUD elements aren't tinted
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        }
    }
}
