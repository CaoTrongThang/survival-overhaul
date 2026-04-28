package com.trongthang.survivaloverhaul.block.entity;

import com.trongthang.survivaloverhaul.SurvivalOverhaul;
import com.trongthang.survivaloverhaul.block.ModBlocks;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlockEntities {
    public static final BlockEntityType<BoilerBlockEntity> BOILER_BLOCK_ENTITY = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            new Identifier(SurvivalOverhaul.MOD_ID, "boiler_be"),
            FabricBlockEntityTypeBuilder.create(BoilerBlockEntity::new, ModBlocks.BOILER).build());

    public static final BlockEntityType<IceBoxBlockEntity> ICE_BOX_BLOCK_ENTITY = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            new Identifier(SurvivalOverhaul.MOD_ID, "ice_box_be"),
            FabricBlockEntityTypeBuilder.create(IceBoxBlockEntity::new, ModBlocks.ICE_BOX).build());

    public static void registerBlockEntities() {
        SurvivalOverhaul.LOGGER.info("Registering Block Entities for " + SurvivalOverhaul.MOD_ID);
    }
}
