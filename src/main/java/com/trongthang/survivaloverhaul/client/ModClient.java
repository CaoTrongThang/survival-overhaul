package com.trongthang.survivaloverhaul.client;

import com.trongthang.survivaloverhaul.SurvivalOverhaul;
import com.trongthang.survivaloverhaul.client.render.ScreenTemperatureOverlay;
import com.trongthang.survivaloverhaul.client.tooltip.HydrationClientTooltipComponent;
import com.trongthang.survivaloverhaul.client.tooltip.HydrationTooltipData;
import com.trongthang.survivaloverhaul.screen.BoilerScreen;
import com.trongthang.survivaloverhaul.screen.IceBoxScreen;
import com.trongthang.survivaloverhaul.screen.ModScreenHandlers;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class ModClient {
    public static void registerClient() {
        SurvivalOverhaul.LOGGER.info("ModClient.registerClient() called!");
        SurvivalOverhaul.LOGGER.info("Initializing Mod Client for " + SurvivalOverhaul.MOD_ID);
        Keybindings.register();
        HudRenderCallback.EVENT.register(ScreenTemperatureOverlay::render);
        TooltipComponentCallback.EVENT.register(data -> {
            if (data instanceof HydrationTooltipData d) {
                return new HydrationClientTooltipComponent(d);
            }
            return null;
        });

        HandledScreens.register(
                ModScreenHandlers.BOILER_SCREEN_HANDLER,
                BoilerScreen::new);
        HandledScreens.register(
                ModScreenHandlers.ICE_BOX_SCREEN_HANDLER,
                IceBoxScreen::new);
    }
}
