package com.trongthang.survivaloverhaul.client;

import com.trongthang.survivaloverhaul.SurvivalOverhaul;
import com.trongthang.survivaloverhaul.client.render.ScreenTemperatureOverlay;
import com.trongthang.survivaloverhaul.client.tooltip.HydrationClientTooltipComponent;
import com.trongthang.survivaloverhaul.client.tooltip.HydrationTooltipData;
import com.trongthang.survivaloverhaul.screen.BoilerScreen;
import com.trongthang.survivaloverhaul.screen.IceBoxScreen;
import com.trongthang.survivaloverhaul.screen.SewingTableScreen;
import com.trongthang.survivaloverhaul.screen.ModScreenHandlers;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import com.trongthang.survivaloverhaul.block.ModBlocks;
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

        net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback.EVENT.register((stack, context, lines) -> {
            net.minecraft.nbt.NbtCompound nbt = stack.getSubNbt(SurvivalOverhaul.MOD_ID);
            if (nbt != null && nbt.contains("CoatType")) {
                String coatType = nbt.getString("CoatType");
                if (coatType.equals("warming")) {
                    lines.add(net.minecraft.text.Text.translatable("tooltip.survivaloverhaul.coat.warming")
                            .formatted(net.minecraft.util.Formatting.GOLD));
                } else if (coatType.equals("cooling")) {
                    lines.add(net.minecraft.text.Text.translatable("tooltip.survivaloverhaul.coat.cooling")
                            .formatted(net.minecraft.util.Formatting.AQUA));
                }
            }
        });

        HandledScreens.register(
                ModScreenHandlers.BOILER_SCREEN_HANDLER,
                BoilerScreen::new);
        HandledScreens.register(
                ModScreenHandlers.ICE_BOX_SCREEN_HANDLER,
                IceBoxScreen::new);
        HandledScreens.register(
                ModScreenHandlers.SEWING_TABLE_SCREEN_HANDLER,
                SewingTableScreen::new);

        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> 0x85f7ff,
                ModBlocks.PURIFIED_WATER_CAULDRON);
    }
}
