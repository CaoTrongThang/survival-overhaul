package com.trongthang.survivaloverhaul.block.custom;

import net.minecraft.block.AbstractCauldronBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Map;

public class PurifiedWaterCauldronBlock extends AbstractCauldronBlock {
    public static final IntProperty LEVEL = Properties.LEVEL_3;
    private static final int BASE_FLUID_HEIGHT = 6;
    private static final double FLUID_HEIGHT_PER_LEVEL = 3.0;

    public PurifiedWaterCauldronBlock(Settings settings, Map<Item, CauldronBehavior> behaviorMap) {
        super(settings, behaviorMap);
        setDefaultState(getStateManager().getDefaultState().with(LEVEL, 1));
    }

    @Override
    public boolean isFull(BlockState state) {
        return state.get(LEVEL) == 3;
    }

    @Override
    protected void appendProperties(StateManager.Builder<net.minecraft.block.Block, BlockState> builder) {
        builder.add(LEVEL);
    }

    @Override
    protected double getFluidHeight(BlockState state) {
        return (BASE_FLUID_HEIGHT + state.get(LEVEL) * FLUID_HEIGHT_PER_LEVEL) / 16.0;
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (!world.isClient && isEntityTouchingFluid(state, pos, entity)) {
            entity.extinguish();
        }
    }
}
