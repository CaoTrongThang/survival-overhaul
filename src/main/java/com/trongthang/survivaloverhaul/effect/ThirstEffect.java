package com.trongthang.survivaloverhaul.effect;

import com.trongthang.survivaloverhaul.mechanics.thirst.IThirstData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.player.PlayerEntity;

public class ThirstEffect extends StatusEffect {
    public ThirstEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (entity instanceof PlayerEntity player && !player.getWorld().isClient) {
            // Gradually increases exhaustion, causing thirst to drop over time
            ((IThirstData) player).survivalOverhaul$getThirstManager().addExhaustion(0.7F * (amplifier + 1));
        }
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }
}
