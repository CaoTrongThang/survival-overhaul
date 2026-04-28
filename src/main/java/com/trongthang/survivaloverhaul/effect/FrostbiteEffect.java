package com.trongthang.survivaloverhaul.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public class FrostbiteEffect extends StatusEffect {
    public FrostbiteEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (!entity.getWorld().isClient) {
            // Freeze damage - 1 heart (2.0f) every 2 seconds
            entity.damage(entity.getDamageSources().freeze(), 2.0f * (amplifier + 1));

            // Slowness
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, amplifier, false, false, true));
        }
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        // Apply effect every 40 ticks (2 seconds)
        return duration % 40 == 0;
    }
}
