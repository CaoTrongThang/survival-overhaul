package com.trongthang.survivaloverhaul.item;

import com.trongthang.survivaloverhaul.SurvivalOverhaul;
import com.trongthang.survivaloverhaul.block.ModBlocks;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItemGroup {
    public static final ItemGroup SURVIVAL_OVERHAUL_GROUP = Registry.register(Registries.ITEM_GROUP,
            new Identifier(SurvivalOverhaul.MOD_ID, "survival_overhaul"),
            FabricItemGroup.builder().displayName(Text.translatable("itemgroup.survival_overhaul"))
                    .icon(() -> new ItemStack(ModItems.BOWL_OF_FIRE)).entries((displayContext, entries) -> {
                        entries.add(ModItems.PURIFIED_WATER);
                        entries.add(ModItems.PURIFIED_WATER_BUCKET);
                        entries.add(ModItems.MEDKIT);
                        entries.add(ModItems.BANDAGE);
                        entries.add(ModBlocks.BOILER);
                        entries.add(ModBlocks.ICE_BOX);
                        entries.add(ModItems.BOWL_OF_FIRE);
                        entries.add(ModItems.BOWL_OF_ICE);
                    }).build());

    public static void registerItemGroups() {
        SurvivalOverhaul.LOGGER.info("Registering Item Groups for " + SurvivalOverhaul.MOD_ID);
    }
}
