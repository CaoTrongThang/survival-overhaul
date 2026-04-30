package com.trongthang.survivaloverhaul.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class FeelingGoodEffect extends StatusEffect {
    public FeelingGoodEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (!entity.getWorld().isClient) {
            entity.heal(0.5f);
        }
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        // Apply effect every 60 ticks (3 seconds)
        return duration % 60 == 0;
    }
}
