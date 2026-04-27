package com.trongthang.survivaloverhaul.item.custom;

import com.trongthang.survivaloverhaul.mechanics.bodyparts.IBodyDamageData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class MedkitItem extends Item {
    public MedkitItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!world.isClient) {
            if (user instanceof IBodyDamageData dataData) {
                dataData.survivalOverhaul$getBodyDamageManager().healAll();

                // Play sound
                world.playSound(null, user.getBlockPos(), SoundEvents.ENTITY_PLAYER_HURT_SWEET_BERRY_BUSH,
                        SoundCategory.PLAYERS, 1.0F, 1.0F); // Placeholder sound for healing

                // Consume item
                if (!user.getAbilities().creativeMode) {
                    stack.decrement(1);
                }
            }
        }

        return TypedActionResult.success(stack, world.isClient());
    }
}
