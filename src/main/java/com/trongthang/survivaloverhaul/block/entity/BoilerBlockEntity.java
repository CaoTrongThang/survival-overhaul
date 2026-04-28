package com.trongthang.survivaloverhaul.block.entity;

import com.trongthang.survivaloverhaul.screen.BoilerScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class BoilerBlockEntity extends AbstractThermalBlockEntity {
    public BoilerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BOILER_BLOCK_ENTITY, pos, state);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return stack.isOf(Items.COAL) || stack.isOf(Items.CHARCOAL) || stack.isOf(Items.COAL_BLOCK)
                || stack.isOf(Items.BLAZE_ROD);
    }

    @Override
    public int getFuelDuration(ItemStack stack) {
        if (stack.isOf(Items.COAL))
            return 1600;
        if (stack.isOf(Items.CHARCOAL))
            return 1600;
        if (stack.isOf(Items.COAL_BLOCK))
            return 16000;
        if (stack.isOf(Items.BLAZE_ROD))
            return 2400;
        return 0;
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new BoilerScreenHandler(syncId, playerInventory, this, this.propertyDelegate);
    }
}
