package com.trongthang.survivaloverhaul.item.custom;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.trongthang.survivaloverhaul.effect.ModEffects;
import com.trongthang.survivaloverhaul.mechanics.thirst.IThirstData;
import com.trongthang.survivaloverhaul.networking.ModNetworking;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.RaycastContext.FluidHandling;

public class CanteenItem extends Item {
    private final boolean isPurified;
    private Item emptyVariant;

    public CanteenItem(Settings settings, boolean isPurified) {
        super(settings);
        this.isPurified = isPurified;
    }

    public void setEmptyVariant(Item emptyVariant) {
        this.emptyVariant = emptyVariant;
    }

    public boolean isPurified() {
        return this.isPurified;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (user instanceof ServerPlayerEntity serverPlayer) {
            Criteria.CONSUME_ITEM.trigger(serverPlayer, stack);
            serverPlayer.incrementStat(Stats.USED.getOrCreateStat(this));
        }

        if (user instanceof PlayerEntity player) {
            if (!world.isClient) {
                float saturation = isPurified ? 2.5f : 0.5f;
                // Add 12 thirst per sip.
                ((IThirstData) player).survivalOverhaul$getThirstManager().add(12, saturation);

                if (!isPurified && world.random.nextFloat() < 0.3f) {
                    player.addStatusEffect(new StatusEffectInstance(ModEffects.THIRST, 600, 0));
                }

                ModNetworking.sync((ServerPlayerEntity) player, (IThirstData) player);
            }
            if (!player.getAbilities().creativeMode) {
                stack.damage(1, player, (p) -> p.sendToolBreakStatus(player.getActiveHand()));
                if (stack.getDamage() >= stack.getMaxDamage()) {
                    return new ItemStack(emptyVariant);
                }
            }
            player.getItemCooldownManager().set(this, 20); // 1s cooldown
        }
        return stack;
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 40;
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
        ItemStack itemStack = user.getStackInHand(hand);

        // Try filling if looking at water and not full
        if (itemStack.getDamage() > 0) {
            BlockHitResult hitResult = raycast(world, user,
                    FluidHandling.SOURCE_ONLY);
            if (hitResult.getType() == HitResult.Type.BLOCK) {
                BlockPos blockPos = hitResult.getBlockPos();
                if (world.canPlayerModifyAt(user, blockPos)
                        && world.getFluidState(blockPos).isIn(FluidTags.WATER)) {
                    world.playSound(user, user.getX(), user.getY(), user.getZ(), SoundEvents.ITEM_BOTTLE_FILL,
                            SoundCategory.NEUTRAL, 1.0f, 1.0f);

                    itemStack.setDamage(itemStack.getDamage() - 1);
                    return TypedActionResult.success(itemStack, world.isClient());
                }
            }
        }

        user.setCurrentHand(hand);
        return TypedActionResult.consume(itemStack);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world,
            List<Text> tooltip, TooltipContext context) {
        int usesLeft = stack.getMaxDamage() - stack.getDamage();
        tooltip.add(Text
                .translatable("tooltip.survivaloverhaul.canteen.uses", usesLeft, stack.getMaxDamage())
                .formatted(Formatting.GRAY));
        super.appendTooltip(stack, world, tooltip, context);
    }
}
