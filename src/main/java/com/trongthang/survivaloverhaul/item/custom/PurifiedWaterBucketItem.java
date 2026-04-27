package com.trongthang.survivaloverhaul.item.custom;

import com.trongthang.survivaloverhaul.mechanics.thirst.IThirstData;
import com.trongthang.survivaloverhaul.config.ModConfig;
import com.trongthang.survivaloverhaul.mechanics.thirst.ThirstManager;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
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

import net.minecraft.item.BucketItem;
import net.minecraft.item.ItemUsage;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.RaycastContext;

public class PurifiedWaterBucketItem extends BucketItem {
    public PurifiedWaterBucketItem(Settings settings) {
        super(com.trongthang.survivaloverhaul.init.FluidInit.PURIFIED_WATER, settings);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (user instanceof ServerPlayerEntity serverPlayer) {
            Criteria.CONSUME_ITEM.trigger(serverPlayer, stack);
            serverPlayer.incrementStat(Stats.USED.getOrCreateStat(this));
        }

        if (user instanceof PlayerEntity player) {
            if (!world.isClient) {
                // Restores full thirst
                ThirstManager manager = ((IThirstData) player).survivalOverhaul$getThirstManager();
                manager.add(ModConfig.maxThirstLevel, 10.0f);
            }
            if (!player.getAbilities().creativeMode) {
                return new ItemStack(Items.BUCKET);
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
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        BlockHitResult blockHitResult = raycast(world, user, RaycastContext.FluidHandling.NONE);

        if (blockHitResult.getType() == HitResult.Type.MISS || user.isSneaking()) {
            return ItemUsage.consumeHeldItem(world, user, hand);
        }

        TypedActionResult<ItemStack> result = super.use(world, user, hand);
        if (result.getResult().isAccepted()) {
            return result;
        }

        return ItemUsage.consumeHeldItem(world, user, hand);
    }
}
