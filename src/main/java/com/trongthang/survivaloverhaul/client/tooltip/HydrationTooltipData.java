package com.trongthang.survivaloverhaul.client.tooltip;

import net.minecraft.client.item.TooltipData;

public record HydrationTooltipData(int hydration, float saturation) implements TooltipData {
}
