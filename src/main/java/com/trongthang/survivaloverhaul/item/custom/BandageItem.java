package com.trongthang.survivaloverhaul.item.custom;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.minecraft.client.MinecraftClient;
import com.trongthang.survivaloverhaul.client.screen.BodyDamageScreen;

public class BandageItem extends Item {
    public BandageItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (world.isClient) {
            // TODO: Open Body Damage UI in "Bandage Mode" on client
            // This will be handled in a separate client-side event/packet or directly here
            // if we use a client class (but to avoid Dedicated Server crash we need to
            // separate).
            // Actually, we can send a S2C packet indicating the UI should open in bandage
            // mode, or better, client handles item use directly if we register an event.
            MinecraftClient.getInstance().setScreen(new BodyDamageScreen(true));
        }

        return TypedActionResult.success(stack, world.isClient());
    }
}
