package com.trongthang.survivaloverhaul.block.entity;

import com.trongthang.survivaloverhaul.item.custom.CoatItem;
import com.trongthang.survivaloverhaul.screen.SewingTableScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

public class SewingTableBlockEntity extends BlockEntity implements SidedInventory, NamedScreenHandlerFactory {
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(3, ItemStack.EMPTY);

    public SewingTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SEWING_TABLE_BLOCK_ENTITY, pos, state);
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("block.survivaloverhaul.sewing_table");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new SewingTableScreenHandler(syncId, playerInventory, this);
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return new int[] { 0, 1, 2 };
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        if (slot == 2)
            return false;
        if (slot == 0)
            return stack.getItem() instanceof ArmorItem;
        if (slot == 1)
            return stack.getItem() instanceof CoatItem;
        return true;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return slot == 2;
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
        ItemStack result = Inventories.splitStack(inventory, slot, amount);
        if (!result.isEmpty()) {
            markDirty();
            if (slot != 2)
                updateResult();
        }
        return result;
    }

    @Override
    public ItemStack removeStack(int slot) {
        ItemStack result = Inventories.removeStack(inventory, slot);
        markDirty();
        if (slot != 2)
            updateResult();
        return result;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        inventory.set(slot, stack);
        if (stack.getCount() > stack.getMaxCount()) {
            stack.setCount(stack.getMaxCount());
        }
        markDirty();
        if (slot != 2)
            updateResult();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void clear() {
        inventory.clear();
        markDirty();
        updateResult();
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, inventory);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, inventory);
    }

    public void updateResult() {
        ItemStack armor = inventory.get(0);
        ItemStack coat = inventory.get(1);

        if (armor.isEmpty() || coat.isEmpty() || !(coat.getItem() instanceof CoatItem coatItem)) {
            inventory.set(2, ItemStack.EMPTY);
            return;
        }

        String coatType = coatItem.getCoatType();
        NbtCompound nbt = armor.getOrCreateSubNbt("survivaloverhaul");
        if (nbt.contains("CoatType") && nbt.getString("CoatType").equals(coatType)) {
            inventory.set(2, ItemStack.EMPTY);
            return;
        }

        ItemStack result = armor.copy();
        result.getOrCreateSubNbt("survivaloverhaul").putString("CoatType", coatType);
        inventory.set(2, result);
    }
}
