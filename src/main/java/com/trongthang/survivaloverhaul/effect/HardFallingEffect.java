package com.trongthang.survivaloverhaul.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class HardFallingEffect extends StatusEffect {
    public HardFallingEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        // Fall damage amplification is applied in LivingEntityMixin via
        // @ModifyVariable.
        // This effect class exists so the icon is shown in the HUD.
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }
}
