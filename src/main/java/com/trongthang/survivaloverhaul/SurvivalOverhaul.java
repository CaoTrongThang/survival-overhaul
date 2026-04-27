package com.trongthang.survivaloverhaul;

import com.trongthang.survivaloverhaul.block.ModBlocks;
import com.trongthang.survivaloverhaul.config.ModConfig;
import com.trongthang.survivaloverhaul.effect.ModEffects;
import com.trongthang.survivaloverhaul.item.ModItemGroup;
import com.trongthang.survivaloverhaul.item.ModItems;
import com.trongthang.survivaloverhaul.mechanics.thirst.IThirstData;
import com.trongthang.survivaloverhaul.mechanics.thirst.ThirstInteractionHandler;
import com.trongthang.survivaloverhaul.networking.ModNetworking;
import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SurvivalOverhaul implements ModInitializer {
	public static final String MOD_ID = "survivaloverhaul";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModItemGroup.registerItemGroups();
		ModItems.registerModItems();
		ModBlocks.registerModBlocks();
		com.trongthang.survivaloverhaul.init.FluidInit.init();
		ModEffects.registerEffects();
		ThirstInteractionHandler.register();

		MidnightConfig.init(MOD_ID, ModConfig.class);
		ModNetworking.registerServerReceivers();

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayerEntity player = handler.getPlayer();
			ModNetworking.sync(player, (IThirstData) player);
		});

		LOGGER.info("Survival Overhaul Initialized!");
	}
}