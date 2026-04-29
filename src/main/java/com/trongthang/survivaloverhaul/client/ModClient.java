package com.trongthang.survivaloverhaul.client;

import com.trongthang.survivaloverhaul.SurvivalOverhaul;
import com.trongthang.survivaloverhaul.client.render.ScreenTemperatureOverlay;
import com.trongthang.survivaloverhaul.screen.BoilerScreen;
import com.trongthang.survivaloverhaul.screen.IceBoxScreen;
import com.trongthang.survivaloverhaul.screen.ModScreenHandlers;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class ModClient {
    public static void registerClient() {
        SurvivalOverhaul.LOGGER.info("ModClient.registerClient() called!");
        SurvivalOverhaul.LOGGER.info("Initializing Mod Client for " + SurvivalOverhaul.MOD_ID);
        Keybindings.register();
        HudRenderCallback.EVENT.register(ScreenTemperatureOverlay::render);

        net.minecraft.client.gui.screen.ingame.HandledScreens.register(
                ModScreenHandlers.BOILER_SCREEN_HANDLER,
                BoilerScreen::new);
        net.minecraft.client.gui.screen.ingame.HandledScreens.register(
                ModScreenHandlers.ICE_BOX_SCREEN_HANDLER,
                IceBoxScreen::new);
    }
}
