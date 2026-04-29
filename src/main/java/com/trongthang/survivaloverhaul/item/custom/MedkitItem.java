package com.trongthang.survivaloverhaul.item.custom;

import com.trongthang.survivaloverhaul.SoundsManager;
import com.trongthang.survivaloverhaul.effect.ModEffects;
import com.trongthang.survivaloverhaul.mechanics.bodyparts.IBodyDamageData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

public class MedkitItem extends Item {
    public MedkitItem(Settings settings) {
        super(settings);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (!world.isClient && user instanceof PlayerEntity player) {
            if (player instanceof IBodyDamageData dataData) {
                dataData.survivalOverhaul$getBodyDamageManager().healAll();

                // Play sound
                SoundsManager.playRandomBandageSound(world, player);

                // Apply FEELING_GOOD effect (30 seconds)
                player.addStatusEffect(new StatusEffectInstance(ModEffects.FEELING_GOOD, 600));

                // Add Cooldown (5 seconds)
                player.getItemCooldownManager().set(this, 100);

                // Consume item
                if (!player.getAbilities().creativeMode) {
                    stack.decrement(1);
                }
            }
        }
        return super.finishUsing(stack, world, user);
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 32;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (user instanceof IBodyDamageData dataData) {
            // check if all the body parts are healed
            if (dataData.survivalOverhaul$getBodyDamageManager().isAllHealed()) {
                return TypedActionResult.fail(stack);
            }
        }

        user.setCurrentHand(hand);
        return TypedActionResult.consume(stack);
    }
}
