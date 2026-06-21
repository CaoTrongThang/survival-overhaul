package com.trongthang.survivaloverhaul.item.custom;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CoatItem extends Item {
    private final String coatType;

    public CoatItem(String coatType, Settings settings) {
        super(settings);
        this.coatType = coatType;
    }

    public String getCoatType() {
        return coatType;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if (coatType.equals("warming")) {
            tooltip.add(Text.translatable("tooltip.survivaloverhaul.coat.item.warming").formatted(Formatting.GOLD));
        } else if (coatType.equals("cooling")) {
            tooltip.add(Text.translatable("tooltip.survivaloverhaul.coat.item.cooling").formatted(Formatting.AQUA));
        }
    }
}
