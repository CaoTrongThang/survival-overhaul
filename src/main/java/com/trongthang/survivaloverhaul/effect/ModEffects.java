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

        public static final StatusEffect HEADACHE = registerStatusEffect("headache",
                        new HeadacheEffect(StatusEffectCategory.HARMFUL, 0x8B4513));

        public static final StatusEffect HARD_FALLING = registerStatusEffect("hard_falling",
                        new HardFallingEffect(StatusEffectCategory.HARMFUL, 0x4A6FA5));

        public static final StatusEffect VULNERABILITY = registerStatusEffect("vulnerability",
                        new VulnerabilityEffect(StatusEffectCategory.HARMFUL, 0x8B0000));

        public static final StatusEffect FROSTBITE = registerStatusEffect("frostbite",
                        new FrostbiteEffect(StatusEffectCategory.HARMFUL, 0x87CEEB));

        public static final StatusEffect HEATSTROKE = registerStatusEffect("heatstroke",
                        new HeatstrokeEffect(StatusEffectCategory.HARMFUL, 0xFF4500));

        public static final StatusEffect FEELING_GOOD = registerStatusEffect("feeling_good",
                        new FeelingGoodEffect(StatusEffectCategory.BENEFICIAL, 0xFFD700));

        public static final StatusEffect WARMING = registerStatusEffect("warming",
                        new WarmingEffect(StatusEffectCategory.BENEFICIAL, 0xFF4500));

        public static final StatusEffect COOLING = registerStatusEffect("cooling",
                        new CoolingEffect(StatusEffectCategory.BENEFICIAL, 0x00FFFF));

        public static StatusEffect registerStatusEffect(String name, StatusEffect statusEffect) {
                return Registry.register(Registries.STATUS_EFFECT, new Identifier(SurvivalOverhaul.MOD_ID, name),
                                statusEffect);
        }

        public static void registerEffects() {
                SurvivalOverhaul.LOGGER.info("Registering ModEffects for " + SurvivalOverhaul.MOD_ID);
        }
}
