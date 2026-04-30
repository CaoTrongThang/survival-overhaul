package com.trongthang.survivaloverhaul.item.custom;

import com.trongthang.survivaloverhaul.mechanics.thirst.IThirstData;
import com.trongthang.survivaloverhaul.networking.ModNetworking;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

public class PurifiedWaterItem extends Item {
    public PurifiedWaterItem(Settings settings) {
        super(settings);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (user instanceof ServerPlayerEntity serverPlayer) {
            Criteria.CONSUME_ITEM.trigger(serverPlayer, stack);
            serverPlayer.incrementStat(Stats.USED.getOrCreateStat(this));
        }

        if (user instanceof PlayerEntity player) {
            if (!world.isClient) {
                ((IThirstData) player).survivalOverhaul$getThirstManager().add(4, 2.0f);
                ModNetworking.sync((ServerPlayerEntity) player, (IThirstData) player);
            }
            if (!player.getAbilities().creativeMode) {
                stack.decrement(1);
                if (stack.isEmpty()) {
                    return new ItemStack(Items.GLASS_BOTTLE);
                }
                player.getInventory().insertStack(new ItemStack(Items.GLASS_BOTTLE));
            }
        }
        return stack;
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 32;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.DRINK;
    }

    @Override
    public SoundEvent getDrinkSound() {
        return SoundEvents.ENTITY_GENERIC_DRINK;
    }

    @Override
    public SoundEvent getEatSound() {
        return SoundEvents.ENTITY_GENERIC_DRINK;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        user.setCurrentHand(hand);
        return TypedActionResult.consume(user.getStackInHand(hand));
    }
}
