package com.trongthang.survivaloverhaul.effect;

import com.trongthang.survivaloverhaul.mechanics.thirst.IThirstData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.player.PlayerEntity;

public class HeatstrokeEffect extends StatusEffect {
    public HeatstrokeEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (!entity.getWorld().isClient) {
            if (entity instanceof PlayerEntity player) {
                // Hot temperatures cause massive dehydration
                ((IThirstData) player).survivalOverhaul$getThirstManager().addExhaustion(2.0f * (amplifier + 1));
            }
            // Damage over time - 1 heart every 2 seconds
            entity.damage(entity.getDamageSources().onFire(), 1.0f * (amplifier + 1));
        }
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return duration % 40 == 0;
    }
}
