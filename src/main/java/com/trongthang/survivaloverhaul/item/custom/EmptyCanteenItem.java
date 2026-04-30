package com.trongthang.survivaloverhaul.item.custom;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public class EmptyCanteenItem extends Item {
    private Item filledVariant;
    private Item purifiedFilledVariant;

    public EmptyCanteenItem(Settings settings) {
        super(settings);
    }

    public void setFilledVariant(Item filledVariant) {
        this.filledVariant = filledVariant;
    }

    public void setPurifiedFilledVariant(Item purifiedFilledVariant) {
        this.purifiedFilledVariant = purifiedFilledVariant;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        BlockHitResult hitResult = raycast(world, user, RaycastContext.FluidHandling.SOURCE_ONLY);
        if (hitResult.getType() == HitResult.Type.MISS) {
            return TypedActionResult.pass(itemStack);
        } else if (hitResult.getType() == HitResult.Type.BLOCK) {
            net.minecraft.util.math.BlockPos blockPos = hitResult.getBlockPos();
            if (!world.canPlayerModifyAt(user, blockPos)) {
                return TypedActionResult.pass(itemStack);
            }
            if (world.getFluidState(blockPos).isIn(FluidTags.WATER)) {
                world.playSound(user, user.getX(), user.getY(), user.getZ(), SoundEvents.ITEM_BOTTLE_FILL,
                        SoundCategory.NEUTRAL, 1.0f, 1.0f);
                user.incrementStat(Stats.USED.getOrCreateStat(this));

                boolean isPurified = world.getFluidState(blockPos)
                        .getFluid() instanceof com.trongthang.survivaloverhaul.fluid.PurifiedWaterFluid;
                Item resultItem = (isPurified && purifiedFilledVariant != null) ? purifiedFilledVariant : filledVariant;

                ItemStack filledStack = new ItemStack(resultItem);
                filledStack.setDamage(resultItem.getMaxDamage() - 1);
                if (itemStack.getCount() == 1) {
                    return TypedActionResult.success(filledStack, world.isClient());
                } else {
                    itemStack.decrement(1);
                    if (!user.getInventory().insertStack(filledStack)) {
                        user.dropItem(filledStack, false);
                    }
                    return TypedActionResult.success(itemStack, world.isClient());
                }
            }
        }
        return TypedActionResult.pass(itemStack);
    }
}
