package com.trongthang.survivaloverhaul.effect;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;
import com.trongthang.survivaloverhaul.SurvivalOverhaul;

public class ModEffects {
    public static final StatusEffect THIRST = registerStatusEffect("thirst",
            new ThirstEffect(StatusEffectCategory.HARMFUL, 0x5D822C));

    public static StatusEffect registerStatusEffect(String name, StatusEffect statusEffect) {
        return Registry.register(Registries.STATUS_EFFECT, new Identifier(SurvivalOverhaul.MOD_ID, name), statusEffect);
    }

    public static void registerEffects() {
        SurvivalOverhaul.LOGGER.info("Registering ModEffects for " + SurvivalOverhaul.MOD_ID);
    }
}
