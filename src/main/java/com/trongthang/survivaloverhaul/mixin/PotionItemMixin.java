package com.trongthang.survivaloverhaul.mixin;

import com.trongthang.survivaloverhaul.effect.ModEffects;
import com.trongthang.survivaloverhaul.mechanics.thirst.IThirstData;
import com.trongthang.survivaloverhaul.networking.ModNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PotionItem.class)
public class PotionItemMixin {

    @Inject(method = "finishUsing", at = @At("HEAD"))
    private void survivalOverhaul$drinkWater(ItemStack stack, World world, LivingEntity user,
            CallbackInfoReturnable<ItemStack> cir) {
        if (!world.isClient) {
            if (user instanceof IThirstData thirstPlayer) {
                thirstPlayer.survivalOverhaul$getThirstManager().applyItemThirst(stack);
                if (user instanceof ServerPlayerEntity serverPlayer) {
                    ModNetworking.sync(serverPlayer, thirstPlayer);
                }
            }

            if (PotionUtil.getPotion(stack) == Potions.WATER) {
                // Apply thirst effect for 30 seconds (600 ticks) when drinking standard water
                user.addStatusEffect(new StatusEffectInstance(ModEffects.THIRST, 600, 0));
            }
        }
    }

}
