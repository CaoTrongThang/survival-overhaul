package com.trongthang.survivaloverhaul.util;

import com.trongthang.survivaloverhaul.block.ModBlocks;
import com.trongthang.survivaloverhaul.block.custom.PurifiedWaterCauldronBlock;
import com.trongthang.survivaloverhaul.item.ModItems;
import net.minecraft.block.BlockState;

import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;

import java.util.Map;

public class ModCauldronBehavior {

    /**
     * Behavior map for the Purified Water Cauldron block.
     * Initialized early so ModBlocks can reference it during static init.
     */
    public static final Map<Item, CauldronBehavior> PURIFIED_WATER_CAULDRON_BEHAVIOR = CauldronBehavior.createMap();

    public static void register() {
        // --- Purified Water Bucket -> empty cauldron creates a Purified Water Cauldron
        // ---
        CauldronBehavior.EMPTY_CAULDRON_BEHAVIOR.put(ModItems.PURIFIED_WATER_BUCKET,
                (state, world, pos, player, hand, stack) -> {
                    if (!world.isClient) {
                        player.setStackInHand(hand,
                                ItemUsage.exchangeStack(stack, player, new ItemStack(Items.BUCKET)));
                        player.incrementStat(Stats.FILL_CAULDRON);
                        world.setBlockState(pos,
                                ModBlocks.PURIFIED_WATER_CAULDRON.getDefaultState()
                                        .with(PurifiedWaterCauldronBlock.LEVEL, 3));
                        world.playSound(null, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0f, 1.0f);
                    }
                    return ActionResult.success(world.isClient);
                });

        // --- Empty canteen + water cauldron -> filled (unpurified) canteen ---
        registerEmptyCanteenBehavior(ModItems.EMPTY_CANTEEN, ModItems.CANTEEN,
                CauldronBehavior.WATER_CAULDRON_BEHAVIOR);
        registerEmptyCanteenBehavior(ModItems.EMPTY_LARGE_CANTEEN, ModItems.LARGE_CANTEEN,
                CauldronBehavior.WATER_CAULDRON_BEHAVIOR);

        // --- Empty canteen + purified water cauldron -> filled (purified) canteen ---
        registerEmptyCanteenBehavior(ModItems.EMPTY_CANTEEN, ModItems.PURIFIED_CANTEEN,
                PURIFIED_WATER_CAULDRON_BEHAVIOR);
        registerEmptyCanteenBehavior(ModItems.EMPTY_LARGE_CANTEEN, ModItems.PURIFIED_LARGE_CANTEEN,
                PURIFIED_WATER_CAULDRON_BEHAVIOR);

        // --- Canteen refill from water cauldron ---
        registerRefillBehavior(ModItems.CANTEEN, CauldronBehavior.WATER_CAULDRON_BEHAVIOR);
        registerRefillBehavior(ModItems.PURIFIED_CANTEEN, CauldronBehavior.WATER_CAULDRON_BEHAVIOR);
        registerRefillBehavior(ModItems.LARGE_CANTEEN, CauldronBehavior.WATER_CAULDRON_BEHAVIOR);
        registerRefillBehavior(ModItems.PURIFIED_LARGE_CANTEEN, CauldronBehavior.WATER_CAULDRON_BEHAVIOR);

        // --- Canteen refill from purified water cauldron ---
        registerRefillBehavior(ModItems.CANTEEN, PURIFIED_WATER_CAULDRON_BEHAVIOR);
        registerRefillBehavior(ModItems.PURIFIED_CANTEEN, PURIFIED_WATER_CAULDRON_BEHAVIOR);
        registerRefillBehavior(ModItems.LARGE_CANTEEN, PURIFIED_WATER_CAULDRON_BEHAVIOR);
        registerRefillBehavior(ModItems.PURIFIED_LARGE_CANTEEN, PURIFIED_WATER_CAULDRON_BEHAVIOR);

        // --- Use empty bucket on full purified cauldron to get purified water bucket
        // back ---
        PURIFIED_WATER_CAULDRON_BEHAVIOR.put(Items.BUCKET, (state, world, pos, player, hand, stack) -> {
            if (state.get(PurifiedWaterCauldronBlock.LEVEL) < 3) {
                return ActionResult.PASS;
            }
            if (!world.isClient) {
                player.setStackInHand(hand,
                        ItemUsage.exchangeStack(stack, player, new ItemStack(ModItems.PURIFIED_WATER_BUCKET)));
                player.incrementStat(Stats.USED.getOrCreateStat(Items.BUCKET));
                world.setBlockState(pos, net.minecraft.block.Blocks.CAULDRON.getDefaultState());
                world.playSound(null, pos, SoundEvents.ITEM_BUCKET_FILL, SoundCategory.BLOCKS, 1.0f, 1.0f);
            }
            return ActionResult.success(world.isClient);
        });
    }

    private static void registerEmptyCanteenBehavior(Item empty, Item filled, Map<Item, CauldronBehavior> map) {
        map.put(empty, (state, world, pos, player, hand, stack) -> {
            if (!world.isClient) {
                player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, new ItemStack(filled)));
                player.incrementStat(Stats.USED.getOrCreateStat(empty));
                decrementLevel(state, world, pos);
                world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.BLOCKS, 1.0f, 1.0f);
            }
            return ActionResult.success(world.isClient);
        });
    }

    private static void registerRefillBehavior(Item canteen, Map<Item, CauldronBehavior> map) {
        map.put(canteen, (state, world, pos, player, hand, stack) -> {
            if (stack.getDamage() <= 0)
                return ActionResult.PASS;
            if (!world.isClient) {
                stack.setDamage(Math.max(0, stack.getDamage() - 1));
                player.incrementStat(Stats.USED.getOrCreateStat(canteen));
                decrementLevel(state, world, pos);
                world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.BLOCKS, 1.0f, 1.0f);
            }
            return ActionResult.success(world.isClient);
        });
    }

    /**
     * Works for both LeveledCauldronBlock (water) and PurifiedWaterCauldronBlock
     * (level property).
     */
    private static void decrementLevel(BlockState state, net.minecraft.world.World world,
            net.minecraft.util.math.BlockPos pos) {
        int level = state.get(Properties.LEVEL_3);
        if (level <= 1) {
            world.setBlockState(pos, net.minecraft.block.Blocks.CAULDRON.getDefaultState());
        } else {
            world.setBlockState(pos, state.with(Properties.LEVEL_3, level - 1));
            world.updateComparators(pos, state.getBlock());
        }
    }
}
