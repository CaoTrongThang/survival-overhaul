package com.trongthang.survivaloverhaul.mechanics.thirst;

import com.trongthang.survivaloverhaul.config.ModConfig;
import com.trongthang.survivaloverhaul.effect.ModEffects;
import com.trongthang.survivaloverhaul.fluid.PurifiedWaterFluid;
import com.trongthang.survivaloverhaul.networking.ModNetworking;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ThirstInteractionHandler {

    public static void register() {
        UseBlockCallback.EVENT.register(ThirstInteractionHandler::onUseBlock);
        UseItemCallback.EVENT.register(ThirstInteractionHandler::onUseItem);
    }

    private static ActionResult onUseBlock(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        return tryDrink(player, world, hand, false);
    }

    private static TypedActionResult<ItemStack> onUseItem(PlayerEntity player, World world, Hand hand) {
        ActionResult result = tryDrink(player, world, hand, true);
        if (result.isAccepted()) {
            return TypedActionResult.success(player.getStackInHand(hand));
        }
        return TypedActionResult.pass(player.getStackInHand(hand));
    }

    public static void requestDrink(ServerPlayerEntity player) {
        tryDrink(player, player.getWorld(), Hand.MAIN_HAND, true);
    }

    private static ActionResult tryDrink(PlayerEntity player, World world, Hand hand, boolean allowRain) {
        if (!ModConfig.enableThirst) {
            return ActionResult.PASS;
        }

        if (hand != Hand.MAIN_HAND || !player.getStackInHand(hand).isEmpty()) {
            return ActionResult.PASS;
        }

        ThirstManager thirstManager = ((IThirstData) player).survivalOverhaul$getThirstManager();
        if (thirstManager.getThirstLevel() >= ModConfig.maxThirstLevel) {
            return ActionResult.PASS;
        }

        // --- Detect what the player is aiming at ---
        boolean drinkingRain = false;
        boolean drinkingWater = false;
        boolean isPurified = false;

        // 1. Check Rain
        if (allowRain && ModConfig.enableRainDrinking && world.hasRain(player.getBlockPos().up())) {
            drinkingRain = true;
        }

        // 2. Check Fluid Raycast (if not already drinking rain or specifically looking
        // at ground)
        if (!drinkingRain && ModConfig.enableHandDrinking) {
            HitResult hit = player.raycast(5.0, 0.0f, true);
            if (hit.getType() == HitResult.Type.BLOCK) {
                BlockPos pos = ((BlockHitResult) hit).getBlockPos();
                FluidState fluidState = world.getFluidState(pos);
                if (fluidState.isIn(FluidTags.WATER) && fluidState.isStill()) {
                    drinkingWater = true;
                    isPurified = fluidState.getFluid() instanceof PurifiedWaterFluid;
                }
            }
        }

        if (!drinkingRain && !drinkingWater) {
            return ActionResult.PASS;
        }

        // --- Server-only: apply thirst and effects ---
        if (!world.isClient) {
            // Already have thirstManager from above
            float thirstToAdd = drinkingRain ? ModConfig.thirstFromRain : ModConfig.thirstFromWater;
            thirstManager.add((int) thirstToAdd, thirstToAdd * 0.05f);

            if (!drinkingRain && !isPurified
                    && world.random.nextFloat() < ModConfig.dehydrationChanceFromSources) {
                player.addStatusEffect(new StatusEffectInstance(ModEffects.THIRST, 400, 0));
            }

            ModNetworking.sync((ServerPlayerEntity) player, (IThirstData) player);

            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENTITY_GENERIC_DRINK, SoundCategory.PLAYERS, 0.5f,
                    world.random.nextFloat() * 0.1f + 0.9f);
        }

        // Client: animate the hand (runs on both sides, Fabric fires events on both)
        player.swingHand(hand, true);
        return ActionResult.SUCCESS;
    }
}
