package com.trongthang.survivaloverhaul.block.entity;

import com.trongthang.survivaloverhaul.screen.IceBoxScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class IceBoxBlockEntity extends AbstractThermalBlockEntity {
    public IceBoxBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ICE_BOX_BLOCK_ENTITY, pos, state);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return stack.isOf(Items.ICE) || stack.isOf(Items.PACKED_ICE) || stack.isOf(Items.BLUE_ICE)
                || stack.isOf(Items.SNOWBALL) || stack.isOf(Items.SNOW_BLOCK);
    }

    @Override
    public int getFuelDuration(ItemStack stack) {
        if (stack.isOf(Items.ICE))
            return 1600;
        if (stack.isOf(Items.PACKED_ICE))
            return 4800;
        if (stack.isOf(Items.BLUE_ICE))
            return 14400;
        if (stack.isOf(Items.SNOWBALL))
            return 200;
        if (stack.isOf(Items.SNOW_BLOCK))
            return 800;
        return 0;
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new IceBoxScreenHandler(syncId, playerInventory, this, this.propertyDelegate);
    }
}
