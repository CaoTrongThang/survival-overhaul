package com.trongthang.survivaloverhaul.block.custom;

import com.trongthang.survivaloverhaul.block.entity.BoilerBlockEntity;
import com.trongthang.survivaloverhaul.block.entity.ModBlockEntities;
import net.minecraft.block.*;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class BoilerBlock extends BlockWithEntity {
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = Properties.LIT;
    public static final EnumProperty<DoubleBlockHalf> HALF = Properties.DOUBLE_BLOCK_HALF;

    public BoilerBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState()
                .with(FACING, Direction.NORTH)
                .with(LIT, false)
                .with(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT, HALF);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockPos blockPos = ctx.getBlockPos();
        World world = ctx.getWorld();
        if (blockPos.getY() < world.getTopY() - 1 && world.getBlockState(blockPos.up()).canReplace(ctx)) {
            return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite()).with(LIT, false)
                    .with(HALF, DoubleBlockHalf.LOWER);
        }
        return null;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        world.setBlockState(pos.up(), state.with(HALF, DoubleBlockHalf.UPPER), 3);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState,
            WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        DoubleBlockHalf doubleBlockHalf = state.get(HALF);
        if (direction.getAxis() == Direction.Axis.Y
                && doubleBlockHalf == DoubleBlockHalf.LOWER == (direction == Direction.UP)) {
            if (neighborState.isOf(this) && neighborState.get(HALF) != doubleBlockHalf) {
                return state;
            }
            return Blocks.AIR.getDefaultState();
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (state.get(LIT)) {
            if (random.nextDouble() < 0.1) {
                world.playSound((double) pos.getX() + 0.5, (double) pos.getY() + 0.5, (double) pos.getZ() + 0.5,
                        SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS, 1.0f, 1.0f, false);
            }

            if (state.get(HALF) == DoubleBlockHalf.UPPER) {
                // Smoke from top
                double d = (double) pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.4;
                double e = (double) pos.getY() + 0.8;
                double f = (double) pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.4;
                world.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, d, e, f, 0.0, 0.05, 0.0);
            } else {
                // Flame at base
                double d = (double) pos.getX() + 0.5;
                double e = (double) pos.getY() + 0.3;
                double f = (double) pos.getZ() + 0.5;

                Direction direction = state.get(FACING);
                Direction.Axis axis = direction.getAxis();
                double g = random.nextDouble() * 0.6 - 0.3;
                double h = axis == Direction.Axis.X ? (double) direction.getOffsetX() * 0.52 : g;
                double i = random.nextDouble() * 6.0 / 16.0;
                double j = axis == Direction.Axis.Z ? (double) direction.getOffsetZ() * 0.52 : g;
                world.addParticle(ParticleTypes.SMOKE, d + h, e + i, f + j, 0.0, 0.0, 0.0);
                world.addParticle(ParticleTypes.FLAME, d + h, e + i, f + j, 0.0, 0.0, 0.0);
            }
        }
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
            BlockHitResult hit) {
        if (!world.isClient) {
            BlockPos entityPos = state.get(HALF) == DoubleBlockHalf.UPPER ? pos.down() : pos;
            BlockEntity blockEntity = world.getBlockEntity(entityPos);
            if (blockEntity instanceof BoilerBlockEntity) {
                player.openHandledScreen((NamedScreenHandlerFactory) blockEntity);
            }
        }
        return ActionResult.SUCCESS;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        if (state.get(HALF) == DoubleBlockHalf.LOWER) {
            return new BoilerBlockEntity(pos, state);
        }
        return null;
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            if (state.get(HALF) == DoubleBlockHalf.LOWER) {
                BlockEntity blockEntity = world.getBlockEntity(pos);
                if (blockEntity instanceof BoilerBlockEntity) {
                    ItemScatterer.spawn(world, pos, (BoilerBlockEntity) blockEntity);
                    world.updateComparators(pos, this);
                }
            }
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state,
            BlockEntityType<T> type) {
        return state.get(HALF) == DoubleBlockHalf.LOWER
                ? checkType(type, ModBlockEntities.BOILER_BLOCK_ENTITY, BoilerBlockEntity::tick)
                : null;
    }
}
