package com.trongthang.survivaloverhaul.client;

import com.trongthang.survivaloverhaul.SurvivalOverhaul;
import com.trongthang.survivaloverhaul.client.render.ScreenTemperatureOverlay;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class ModClient {
    public static void registerClient() {
        SurvivalOverhaul.LOGGER.info("ModClient.registerClient() called!");
        SurvivalOverhaul.LOGGER.info("Initializing Mod Client for " + SurvivalOverhaul.MOD_ID);
        Keybindings.register();
        HudRenderCallback.EVENT.register(ScreenTemperatureOverlay::render);

        net.minecraft.client.gui.screen.ingame.HandledScreens.register(
                com.trongthang.survivaloverhaul.screen.ModScreenHandlers.BOILER_SCREEN_HANDLER,
                com.trongthang.survivaloverhaul.screen.BoilerScreen::new);
        net.minecraft.client.gui.screen.ingame.HandledScreens.register(
                com.trongthang.survivaloverhaul.screen.ModScreenHandlers.ICE_BOX_SCREEN_HANDLER,
                com.trongthang.survivaloverhaul.screen.IceBoxScreen::new);
    }
}
