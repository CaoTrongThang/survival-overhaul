package com.trongthang.survivaloverhaul;

import com.trongthang.survivaloverhaul.datagen.ModBlockLootTableProvider;
import com.trongthang.survivaloverhaul.datagen.ModBlockTagProvider;
import com.trongthang.survivaloverhaul.datagen.ModModelProvider;
import com.trongthang.survivaloverhaul.datagen.ModRecipeProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class SurvivalOverhaulDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

		pack.addProvider(ModModelProvider::new);
		pack.addProvider(ModRecipeProvider::new);
		pack.addProvider(ModBlockLootTableProvider::new);
		pack.addProvider(ModBlockTagProvider::new);
	}
}
