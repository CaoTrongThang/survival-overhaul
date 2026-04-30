package com.trongthang.survivaloverhaul.mixin;

import com.trongthang.survivaloverhaul.config.ModConfig;
import com.trongthang.survivaloverhaul.effect.ModEffects;
import com.trongthang.survivaloverhaul.item.custom.PurifiedWaterBucketItem;
import com.trongthang.survivaloverhaul.item.custom.PurifiedWaterItem;
import com.trongthang.survivaloverhaul.mechanics.thirst.IThirstData;
import com.trongthang.survivaloverhaul.networking.ModNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.registry.tag.DamageTypeTags;
import com.trongthang.survivaloverhaul.mechanics.bodyparts.BodyPart;
import com.trongthang.survivaloverhaul.mechanics.bodyparts.HitLocationDetector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "eatFood", at = @At("HEAD"))
    private void survivalOverhaul$onEatFood(World world, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        if (!world.isClient && (Object) this instanceof PlayerEntity player) {
            ((IThirstData) player).survivalOverhaul$getThirstManager().applyItemThirst(stack);
            ModNetworking.sync((ServerPlayerEntity) player, (IThirstData) player);

            if (!(stack.getItem() instanceof PurifiedWaterItem) &&
                    !(stack.getItem() instanceof PurifiedWaterBucketItem)) {
                if (world.random.nextFloat() < ModConfig.dehydrationChanceFromItems) {
                    player.addStatusEffect(new StatusEffectInstance(
                            ModEffects.THIRST, 400, 0));
                }
            }
        }
    }

    /**
     * Proper implementation for Hard Falling and Vulnerability effects.
     * Hard Falling: +20% fall damage per level.
     * Vulnerability: +20% all non-fall damage per level.
     */
    @ModifyVariable(method = "applyDamage", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float survivalOverhaul$amplifyDamage(float amount, DamageSource source) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (entity.getWorld().isClient) {
            return amount;
        }

        if (source.isOf(DamageTypes.FALL)) {
            if (entity.hasStatusEffect(ModEffects.HARD_FALLING)) {
                int amplifier = entity.getStatusEffect(ModEffects.HARD_FALLING).getAmplifier();
                amount *= (1.0f + (amplifier + 1) * 0.2f);
            }
        } else if (!source.isOf(DamageTypes.OUT_OF_WORLD)) {
            if (entity.hasStatusEffect(ModEffects.VULNERABILITY)) {
                int amplifier = entity.getStatusEffect(ModEffects.VULNERABILITY).getAmplifier();
                amount *= (1.0f + (amplifier + 1) * 0.2f);
            }
        }

        if (ModConfig.enableHeadshotMultiplier) {
            boolean isHeadHit = source.isIn(DamageTypeTags.DAMAGES_HELMET) || source.isOf(DamageTypes.FLY_INTO_WALL);
            if (!isHeadHit && source.getAttacker() != null) {
                isHeadHit = HitLocationDetector.detect(entity, source) == BodyPart.HEAD;
            }
            if (isHeadHit) {
                if (!ModConfig.nullifyHeadshotWithHelmet || entity.getEquippedStack(EquipmentSlot.HEAD).isEmpty()) {
                    amount *= ModConfig.headshotDamageMultiplier;
                }
            }
        }

        if (source.getAttacker() instanceof LivingEntity attacker) {
            if (attacker.hasStatusEffect(ModEffects.FEELING_GOOD)) {
                int amplifier = attacker.getStatusEffect(ModEffects.FEELING_GOOD).getAmplifier();
                amount *= (1.0f + (amplifier + 1) * 0.05f);
            }
        }

        return amount;
    }
}
