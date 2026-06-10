package com.trongthang.survivaloverhaul.mechanics.poop;

import com.trongthang.survivaloverhaul.config.ModConfig;
import com.trongthang.survivaloverhaul.networking.ModNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;

public class PoopManager {

    // 0 = empty, 20 = full (must poop)
    private int poopLevel = 0;

    public void onFoodEaten(PlayerEntity player, int foodPoints) {
        if (!ModConfig.enablePoop) return;

        // Add 1/10th of the food eaten to the poop level
        int gain = Math.max(1, foodPoints / 10);
        int oldLevel = this.poopLevel;
        this.poopLevel = Math.min(this.poopLevel + gain, 20);

        if (player instanceof ServerPlayerEntity serverPlayer && oldLevel != this.poopLevel) {
            ModNetworking.sendPoopSync(serverPlayer, this.poopLevel);
        }
    }

    public void readNbt(NbtCompound nbt) {
        if (nbt.contains("PoopLevel", 99)) {
            this.poopLevel = nbt.getInt("PoopLevel");
        }
    }

    public void writeNbt(NbtCompound nbt) {
        nbt.putInt("PoopLevel", this.poopLevel);
    }

    public int getPoopLevel() {
        return poopLevel;
    }

    public void setPoopLevel(int poopLevel) {
        this.poopLevel = poopLevel;
    }
}
