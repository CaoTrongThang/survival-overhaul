package com.trongthang.survivaloverhaul.client.hud;

import com.trongthang.survivaloverhaul.mechanics.temperature.ITemperatureData;
import com.trongthang.survivaloverhaul.mechanics.temperature.TemperatureManager;
import com.trongthang.survivaloverhaul.config.ModConfig;
import com.trongthang.survivaloverhaul.item.ModItems;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;

public class ThermometerHudOverlay {

    public static void render(DrawContext context, MinecraftClient client, int scaledWidth, int scaledHeight) {
        if (client == null || client.player == null)
            return;
        if (client.options.hudHidden || client.player.getAbilities().creativeMode || client.player.isSpectator())
            return;

        PlayerEntity player = client.player;

        boolean hasThermometer = player.getMainHandStack().getItem() == ModItems.THERMOMETER
                || player.getOffHandStack().getItem() == ModItems.THERMOMETER;

        if (!hasThermometer)
            return;

        TemperatureManager manager = ((ITemperatureData) player).survivalOverhaul$getTemperatureManager();
        float body = manager.getBodyTemperature();
        float ambient = manager.getAmbientTemperature();
        float biome = manager.getBiomeContribution();
        float env = manager.getEnvironmentModifier();
        float thermal = manager.getThermalModifier();
        float equip = manager.getEquipmentModifier();

        // Calculate position based on config
        int xBase = 0;
        int yBase = 0;

        switch (ModConfig.thermometerHudPosition) {
            case BOTTOM_RIGHT -> {
                xBase = scaledWidth - 80;
                yBase = scaledHeight - 45;
            }
            case BOTTOM_LEFT -> {
                xBase = 10;
                yBase = scaledHeight - 45;
            }
            case TOP_RIGHT -> {
                xBase = scaledWidth - 80;
                yBase = 10;
            }
            case TOP_LEFT -> {
                xBase = 10;
                yBase = 10;
            }
            case MIDDLE_RIGHT -> {
                xBase = scaledWidth - 80;
                yBase = scaledHeight / 2 - 18;
            }
            case MIDDLE_LEFT -> {
                xBase = 10;
                yBase = scaledHeight / 2 - 18;
            }
            case TOP_MIDDLE -> {
                xBase = scaledWidth / 2 - 40;
                yBase = 10;
            }
        }

        context.getMatrices().push();
        context.getMatrices().translate(xBase + ModConfig.thermometerHudXOffset,
                yBase + ModConfig.thermometerHudYOffset, 0);
        context.getMatrices().scale(ModConfig.thermometerHudScale, ModConfig.thermometerHudScale, 1.0f);

        int bodyColor = body < 10f ? 0x88CCFF : (body > 28f ? 0xFF6644 : 0xFFFFFF);
        int lineY = 0;
        context.drawTextWithShadow(client.textRenderer,
                String.format("Body: %.1f°  Ambient: %.1f°", body, ambient), 0, lineY, bodyColor);
        lineY += 10;
        context.drawTextWithShadow(client.textRenderer,
                String.format("Biome: %+.1f°", biome), 0, lineY, 0xAADDFF);
        lineY += 10;
        context.drawTextWithShadow(client.textRenderer,
                String.format("Environment: %+.1f°", env), 0, lineY, env < 0 ? 0x88CCFF : 0xFFDDAA);
        lineY += 10;
        if (thermal != 0f) {
            context.drawTextWithShadow(client.textRenderer,
                    String.format("Nearby blocks: %+.1f°", thermal), 0, lineY, thermal > 0 ? 0xFF9944 : 0x99DDFF);
            lineY += 10;
        }
        if (equip != 0f) {
            context.drawTextWithShadow(client.textRenderer,
                    String.format("Equipment: %+.1f°", equip), 0, lineY, equip > 0 ? 0xFFCC55 : 0xAAEEFF);
        }

        context.getMatrices().pop();
    }
}
