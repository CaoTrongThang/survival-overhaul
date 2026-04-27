package com.trongthang.survivaloverhaul.block;

import com.trongthang.survivaloverhaul.SurvivalOverhaul;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlocks {

    // Example block
    public static final Block TEST_BLOCK = registerBlock("test_block",
            new Block(FabricBlockSettings.create().strength(4.0f).requiresTool()));
    public static final Block PURIFIED_WATER = Registry.register(Registries.BLOCK,
            new Identifier(SurvivalOverhaul.MOD_ID, "purified_water"),
            new net.minecraft.block.FluidBlock(com.trongthang.survivaloverhaul.init.FluidInit.PURIFIED_WATER,
                    net.minecraft.block.AbstractBlock.Settings.copy(net.minecraft.block.Blocks.WATER)));

    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, new Identifier(SurvivalOverhaul.MOD_ID, name), block);
    }

    private static Item registerBlockItem(String name, Block block) {
        return Registry.register(Registries.ITEM, new Identifier(SurvivalOverhaul.MOD_ID, name),
                new BlockItem(block, new FabricItemSettings()));
    }

    public static void registerModBlocks() {
        SurvivalOverhaul.LOGGER.info("Registering Mod Blocks for " + SurvivalOverhaul.MOD_ID);
    }
}
