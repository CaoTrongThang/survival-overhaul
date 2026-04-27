package com.trongthang.survivaloverhaul.client.render;

import com.trongthang.survivaloverhaul.init.FluidInit;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.minecraft.util.Identifier;

public class FluidRenderInit {
    public static void registerFluidRenders() {
        // Use regular water textures but with a lighter/cleaner tint
        FluidRenderHandlerRegistry.INSTANCE.register(FluidInit.PURIFIED_WATER, FluidInit.PURIFIED_FLOWING_WATER,
                new SimpleFluidRenderHandler(
                        new Identifier("minecraft:block/water_still"),
                        new Identifier("minecraft:block/water_flow"),
                        0x85f7ff // Light blue tint for purified water
                ));
    }
}
