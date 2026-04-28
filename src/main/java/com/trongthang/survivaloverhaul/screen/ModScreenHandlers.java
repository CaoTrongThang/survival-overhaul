package com.trongthang.survivaloverhaul.screen;

import com.trongthang.survivaloverhaul.SurvivalOverhaul;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.resource.featuretoggle.FeatureSet;

public class ModScreenHandlers {
    public static final ScreenHandlerType<BoilerScreenHandler> BOILER_SCREEN_HANDLER = Registry.register(
            Registries.SCREEN_HANDLER,
            new Identifier(SurvivalOverhaul.MOD_ID, "boiler"),
            new ScreenHandlerType<BoilerScreenHandler>(BoilerScreenHandler::new, FeatureSet.empty()));

    public static final ScreenHandlerType<IceBoxScreenHandler> ICE_BOX_SCREEN_HANDLER = Registry.register(
            Registries.SCREEN_HANDLER,
            new Identifier(SurvivalOverhaul.MOD_ID, "ice_box"),
            new ScreenHandlerType<IceBoxScreenHandler>(IceBoxScreenHandler::new, FeatureSet.empty()));

    public static void registerScreenHandlers() {
        SurvivalOverhaul.LOGGER.info("Registering Screen Handlers for " + SurvivalOverhaul.MOD_ID);
    }
}
