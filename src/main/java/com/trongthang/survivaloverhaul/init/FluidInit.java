package com.trongthang.survivaloverhaul.init;

import com.trongthang.survivaloverhaul.SurvivalOverhaul;
import com.trongthang.survivaloverhaul.fluid.PurifiedWaterFluid;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class FluidInit {
    public static final FlowableFluid PURIFIED_WATER = register("purified_water", new PurifiedWaterFluid.Still());
    public static final FlowableFluid PURIFIED_FLOWING_WATER = register("purified_flowing_water",
            new PurifiedWaterFluid.Flowing());

    private static <T extends FlowableFluid> T register(String name, T fluid) {
        return Registry.register(Registries.FLUID, new Identifier(SurvivalOverhaul.MOD_ID, name), fluid);
    }

    public static void init() {
    }
}
