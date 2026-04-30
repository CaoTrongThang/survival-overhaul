package com.trongthang.survivaloverhaul.client;

import com.trongthang.survivaloverhaul.config.ModConfig;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class ThirstTooltipHandler {
    public static void appendTooltip(ItemStack stack, TooltipContext context, List<Text> lines) {
        if (!ModConfig.enableThirst || stack.isEmpty())
            return;

        String itemId = Registries.ITEM.getId(stack.getItem()).toString();
        for (String entry : ModConfig.itemThirstValues) {
            String[] parts = entry.split("=");
            if (parts.length == 2 && parts[0].trim().equals(itemId)) {
                try {
                    int amount = Integer.parseInt(parts[1].trim());
                    lines.add(Text.translatable("tooltip.survivaloverhaul.thirst_gain", amount)
                            .formatted(Formatting.AQUA));
                    break;
                } catch (NumberFormatException ignored) {
                }
            }
        }
    }
}
