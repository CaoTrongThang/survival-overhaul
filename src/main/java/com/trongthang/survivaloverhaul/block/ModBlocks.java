package com.trongthang.survivaloverhaul.block;

import com.trongthang.survivaloverhaul.SurvivalOverhaul;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.FluidBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import com.trongthang.survivaloverhaul.init.FluidInit;

public class ModBlocks {

    public static final Block PURIFIED_WATER = Registry.register(Registries.BLOCK,
            new Identifier(SurvivalOverhaul.MOD_ID, "purified_water"),
            new FluidBlock(FluidInit.PURIFIED_WATER,
                    FabricBlockSettings.copy(net.minecraft.block.Blocks.WATER)));

    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, new Identifier(SurvivalOverhaul.MOD_ID, name), block);
    }

    private static Item registerBlockItem(String name, Block block) {
        return Registry.register(Registries.ITEM, new Identifier(SurvivalOverhaul.MOD_ID, name),
                new BlockItem(block, new FabricItemSettings()));
    }

    public static final Block BOILER = registerBlock("boiler",
            new com.trongthang.survivaloverhaul.block.custom.BoilerBlock(
                    FabricBlockSettings.copy(net.minecraft.block.Blocks.IRON_BLOCK).nonOpaque()
                            .luminance(state -> state.get(com.trongthang.survivaloverhaul.block.custom.BoilerBlock.LIT)
                                    ? 13
                                    : 0)));

    public static final Block ICE_BOX = registerBlock("ice_box",
            new com.trongthang.survivaloverhaul.block.custom.IceBoxBlock(
                    FabricBlockSettings.copy(net.minecraft.block.Blocks.OAK_PLANKS).nonOpaque()));

    public static void registerModBlocks() {
        SurvivalOverhaul.LOGGER.info("Registering Mod Blocks for " + SurvivalOverhaul.MOD_ID);
    }
}
