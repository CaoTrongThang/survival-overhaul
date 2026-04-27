package com.trongthang.survivaloverhaul.client;

import com.trongthang.survivaloverhaul.SurvivalOverhaul;

public class ModClient {
    public static void registerClient() {
        SurvivalOverhaul.LOGGER.info("ModClient.registerClient() called!");
        SurvivalOverhaul.LOGGER.info("Initializing Mod Client for " + SurvivalOverhaul.MOD_ID);
        Keybindings.register();
        // HUD is now handled by InGameHudMixin
    }
}
