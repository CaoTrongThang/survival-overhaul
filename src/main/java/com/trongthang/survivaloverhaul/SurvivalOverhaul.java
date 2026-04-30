package com.trongthang.survivaloverhaul;

import com.trongthang.survivaloverhaul.block.ModBlocks;
import com.trongthang.survivaloverhaul.block.entity.ModBlockEntities;
import com.trongthang.survivaloverhaul.config.ModConfig;
import com.trongthang.survivaloverhaul.effect.ModEffects;
import com.trongthang.survivaloverhaul.init.FluidInit;
import com.trongthang.survivaloverhaul.item.ModItemGroup;
import com.trongthang.survivaloverhaul.item.ModItems;
import com.trongthang.survivaloverhaul.mechanics.thirst.IThirstData;
import com.trongthang.survivaloverhaul.mechanics.thirst.ThirstInteractionHandler;
import com.trongthang.survivaloverhaul.networking.ModNetworking;
import com.trongthang.survivaloverhaul.screen.ModScreenHandlers;

import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.fabricmc.loader.api.FabricLoader;

public class SurvivalOverhaul implements ModInitializer {
	public static final String MOD_ID = "survivaloverhaul";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		if (!FabricLoader.getInstance().isModLoaded("welcometomyworld")) {
			throw new RuntimeException(
					"Survival Overhaul requires 'Welcome To My World' mod to function! Please install it.");
		}

		ModItemGroup.registerItemGroups();
		ModItems.registerModItems();
		ModBlocks.registerModBlocks();
		ModBlockEntities.registerBlockEntities();
		ModScreenHandlers.registerScreenHandlers();
		FluidInit.init();
		ModEffects.registerEffects();
		ThirstInteractionHandler.register();
		SoundsManager.registerSounds();

		MidnightConfig.init(MOD_ID, ModConfig.class);
		ModNetworking.registerServerReceivers();

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayerEntity player = handler.getPlayer();
			ModNetworking.sync(player, (IThirstData) player);
		});

		LOGGER.info("Survival Overhaul Initialized!");
	}
}