package com.trongthang.survivaloverhaul.mixin;

import com.trongthang.survivaloverhaul.config.ModConfig;
import com.trongthang.survivaloverhaul.effect.ModEffects;
import com.trongthang.survivaloverhaul.item.custom.PurifiedWaterBucketItem;
import com.trongthang.survivaloverhaul.item.custom.PurifiedWaterItem;
import com.trongthang.survivaloverhaul.mechanics.thirst.IThirstData;
import com.trongthang.survivaloverhaul.networking.ModNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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
}
