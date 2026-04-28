package com.trongthang.survivaloverhaul.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractThermalBlockEntity extends BlockEntity
        implements SidedInventory, NamedScreenHandlerFactory {
    protected final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(4, ItemStack.EMPTY);
    protected int fuelTime;
    protected int fuelDuration;

    protected final PropertyDelegate propertyDelegate;

    public AbstractThermalBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.propertyDelegate = new PropertyDelegate() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> AbstractThermalBlockEntity.this.fuelTime;
                    case 1 -> AbstractThermalBlockEntity.this.fuelDuration;
                    case 2 -> AbstractThermalBlockEntity.this.isPowered() ? 1 : 0;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> AbstractThermalBlockEntity.this.fuelTime = value;
                    case 1 -> AbstractThermalBlockEntity.this.fuelDuration = value;
                }
            }

            @Override
            public int size() {
                return 3;
            }
        };
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable(getCachedState().getBlock().getTranslationKey());
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return new int[] { 0, 1, 2, 3 };
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return isItemValid(stack);
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return true;
    }

    @Override
    public int size() {
        return inventory.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : inventory) {
            if (!stack.isEmpty())
                return false;
        }
        return true;
    }

    @Override
    public ItemStack getStack(int slot) {
        return inventory.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return Inventories.splitStack(inventory, slot, amount);
    }

    @Override
    public ItemStack removeStack(int slot) {
        return Inventories.removeStack(inventory, slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        inventory.set(slot, stack);
        if (stack.getCount() > stack.getMaxCount()) {
            stack.setCount(stack.getMaxCount());
        }
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void clear() {
        inventory.clear();
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, inventory);
        nbt.putInt("fuelTime", fuelTime);
        nbt.putInt("fuelDuration", fuelDuration);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, inventory);
        fuelTime = nbt.getInt("fuelTime");
        fuelDuration = nbt.getInt("fuelDuration");
    }

    public abstract boolean isItemValid(ItemStack stack);

    public abstract int getFuelDuration(ItemStack stack);

    public boolean isLit() {
        return fuelTime > 0;
    }

    public boolean isPowered() {
        if (world == null)
            return false;
        return world.isReceivingRedstonePower(pos);
    }

    public static void tick(World world, BlockPos pos, BlockState state, AbstractThermalBlockEntity entity) {
        if (world.isClient)
            return;

        boolean isPowered = world.isReceivingRedstonePower(pos);
        boolean wasLit = entity.isLit();
        boolean isLit = wasLit;

        if (isLit && isPowered) {
            entity.fuelTime--;
            if (entity.fuelTime <= 0) {
                isLit = false;
            }
        }

        if (!isLit && isPowered) {
            for (int i = 0; i < entity.size(); i++) {
                ItemStack stack = entity.getStack(i);
                if (!stack.isEmpty() && entity.isItemValid(stack)) {
                    int duration = entity.getFuelDuration(stack);
                    if (duration > 0) {
                        entity.fuelTime = duration;
                        entity.fuelDuration = duration;
                        stack.decrement(1);
                        isLit = true;
                        break;
                    }
                }
            }
        }

        if (wasLit != isLit || state.get(Properties.LIT) != (isLit && isPowered)) {
            boolean newLit = isLit && isPowered;
            if (state.get(Properties.LIT) != newLit) {
                world.setBlockState(pos, state.with(Properties.LIT, newLit), 3);

                if (state.contains(Properties.DOUBLE_BLOCK_HALF)) {
                    DoubleBlockHalf half = state.get(Properties.DOUBLE_BLOCK_HALF);
                    BlockPos otherPos = half == DoubleBlockHalf.LOWER ? pos.up() : pos.down();
                    BlockState otherState = world.getBlockState(otherPos);
                    if (otherState.isOf(state.getBlock())) {
                        world.setBlockState(otherPos, otherState.with(Properties.LIT, newLit), 3);
                    }
                }
            }
            entity.markDirty();
        }
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }
}
