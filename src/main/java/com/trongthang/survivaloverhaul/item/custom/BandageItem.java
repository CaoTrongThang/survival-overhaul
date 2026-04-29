package com.trongthang.survivaloverhaul.item.custom;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.minecraft.client.MinecraftClient;
import com.trongthang.survivaloverhaul.client.screen.BodyDamageScreen;
import com.trongthang.survivaloverhaul.config.ModConfig;

public class BandageItem extends Item {
    public BandageItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (world.isClient) {
            if (ModConfig.enableBodyDamage) {
                MinecraftClient.getInstance().setScreen(new BodyDamageScreen(true));
            }
        } else {
            user.getItemCooldownManager().set(this, 40);
        }

        return TypedActionResult.success(stack, world.isClient());
    }
}
