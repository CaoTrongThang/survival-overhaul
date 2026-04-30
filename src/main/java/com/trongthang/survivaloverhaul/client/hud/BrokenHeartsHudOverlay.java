package com.trongthang.survivaloverhaul.client.hud;

import com.trongthang.survivaloverhaul.SurvivalOverhaul;
import com.trongthang.survivaloverhaul.mechanics.bodyparts.BodyPart;
import com.trongthang.survivaloverhaul.mechanics.bodyparts.IBodyDamageData;
import com.trongthang.survivaloverhaul.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Random;

public class BrokenHeartsHudOverlay {
    private static final Identifier ICONS = new Identifier(SurvivalOverhaul.MOD_ID, "textures/gui/overlay.png");
    private static final Random random = new Random();

    public static void render(DrawContext context, MinecraftClient client, int width, int height) {
        PlayerEntity player = client.player;
        if (player == null || !ModConfig.enableBrokenLimbMaxHealthReduction)
            return;

        IBodyDamageData data = (IBodyDamageData) player;
        int brokenLimbs = 0;
        for (BodyPart part : BodyPart.values()) {
            if (data.survivalOverhaul$getBodyDamageManager().getHealth(part) <= 0) {
                brokenLimbs++;
            }
        }

        if (brokenLimbs == 0)
            return;

        float maxHealth = player.getMaxHealth();
        float reduction = brokenLimbs * ModConfig.brokenLimbMaxHealthReduction;

        reduction = Math.min(0.9f, Math.max(0.0f, reduction));

        float originalMaxHealth = maxHealth / (1.0f - reduction);

        int currentHearts = net.minecraft.util.math.MathHelper.ceil(maxHealth / 2.0f);
        int totalHearts = net.minecraft.util.math.MathHelper.ceil(originalMaxHealth / 2.0f);

        int brokenHeartsToDraw = totalHearts - currentHearts;
        if (brokenHeartsToDraw <= 0)
            return;

        int left = width / 2 - 91;
        int top = height - 39;

        random.setSeed(player.age * 312284L);
        boolean jitter = player.getHealth() + player.getAbsorptionAmount() <= 4.0f;
        boolean justHit = player.hurtTime > 0;

        int uOffset = justHit && (player.hurtTime % 10 < 5) ? 9 : 0;

        for (int i = 0; i < brokenHeartsToDraw; i++) {
            int drawIndex = currentHearts + i;
            int row = drawIndex / 10;
            int col = drawIndex % 10;
            int x = left + col * 8;
            int y = top - row * 10;

            if (jitter || justHit) {
                y -= random.nextInt(2);
            }

            context.drawTexture(ICONS, x, y, 144 + uOffset, 0, 9, 9, 256, 256);
        }
    }
}
