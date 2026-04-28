package com.trongthang.survivaloverhaul.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class FeelingGoodEffect extends StatusEffect {
    public FeelingGoodEffect(StatusEffectCategory category, int color) {
        super(category, color);
        // Add a small attack damage boost: +1.0 damage (0.5 hearts) per level
        this.addAttributeModifier(EntityAttributes.GENERIC_ATTACK_DAMAGE, "6438118b-07e3-47e8-9efb-ca0d8a0d0cfd", 1.0,
                EntityAttributeModifier.Operation.ADDITION);
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
