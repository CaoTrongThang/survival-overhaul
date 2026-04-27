package com.trongthang.survivaloverhaul.mechanics.thirst;

import com.trongthang.survivaloverhaul.config.ModConfig;
import com.trongthang.survivaloverhaul.networking.ModNetworking;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.Difficulty;

import net.minecraft.server.network.ServerPlayerEntity;

import static com.trongthang.survivaloverhaul.SurvivalOverhaul.LOGGER;

public class ThirstManager {

    private int thirstLevel = 20;
    private float thirstSaturationLevel = 5.0f;
    private float exhaustion = 0.0f;
    private int thirstTickTimer = 0;

    public void update(PlayerEntity player) {
        if (!ModConfig.enableThirst)
            return;

        int oldThirst = this.thirstLevel;
        float oldSat = this.thirstSaturationLevel;

        Difficulty difficulty = player.getWorld().getDifficulty();

        if (this.exhaustion > ModConfig.thirstExhaustionThreshold) {
            this.exhaustion -= ModConfig.thirstExhaustionThreshold;
            if (this.thirstSaturationLevel > 0.0F) {
                this.thirstSaturationLevel = Math.max(this.thirstSaturationLevel - 1.0F, 0.0F);
            } else if (difficulty != Difficulty.PEACEFUL) {
                this.thirstLevel = Math.max(this.thirstLevel - 1, 0);
            }
        }

        if (this.thirstTickTimer >= ModConfig.thirstInterval) {
            this.thirstTickTimer = 0;

            if (this.thirstLevel <= 0 && ModConfig.enableThirstDamage) {
                if (player.getHealth() > 10.0F || difficulty == Difficulty.HARD
                        || (player.getHealth() > 1.0F && difficulty == Difficulty.NORMAL)) {
                    player.damage(player.getDamageSources().starve(), ModConfig.thirstDamage);
                }
            }

            if (difficulty == Difficulty.PEACEFUL && this.thirstLevel < ModConfig.maxThirstLevel) {
                this.thirstLevel = Math.min(this.thirstLevel + 8, ModConfig.maxThirstLevel);
            }

        } else {
            this.thirstTickTimer++;
        }

        if (player instanceof ServerPlayerEntity serverPlayer) {
            if (oldThirst != this.thirstLevel || oldSat != this.thirstSaturationLevel) {
                ModNetworking.sendThirstSync(serverPlayer, this.thirstLevel,
                        this.thirstSaturationLevel);
            }
        }
    }

    public void readNbt(NbtCompound nbt) {
        if (nbt.contains("ThirstLevel", 99)) {
            this.thirstLevel = nbt.getInt("ThirstLevel");
            this.thirstTickTimer = nbt.getInt("ThirstTickTimer");
            this.thirstSaturationLevel = nbt.getFloat("ThirstSaturationLevel");
            this.exhaustion = nbt.getFloat("ThirstExhaustionLevel");
        }
    }

    public void writeNbt(NbtCompound nbt) {
        nbt.putInt("ThirstLevel", this.thirstLevel);
        nbt.putInt("ThirstTickTimer", this.thirstTickTimer);
        nbt.putFloat("ThirstSaturationLevel", this.thirstSaturationLevel);
        nbt.putFloat("ThirstExhaustionLevel", this.exhaustion);
    }

    public void addExhaustion(float exhaustion) {
        this.exhaustion = Math.min(this.exhaustion + exhaustion, 40.0F);
    }

    public void add(int thirst, float saturation) {
        this.thirstLevel = Math.min(thirst + this.thirstLevel,
                ModConfig.maxThirstLevel);
        this.thirstSaturationLevel = Math.min(this.thirstSaturationLevel + saturation, (float) this.thirstLevel);
    }

    public void applyItemThirst(net.minecraft.item.ItemStack stack) {
        if (!ModConfig.enableThirst || stack.isEmpty())
            return;

        String itemId = net.minecraft.registry.Registries.ITEM.getId(stack.getItem()).toString();

        for (String entry : ModConfig.itemThirstValues) {
            String[] parts = entry.split("=");
            if (parts.length == 2 && parts[0].trim().equals(itemId)) {
                try {
                    int amount = Integer.parseInt(parts[1].trim());
                    // Add thirst and some saturation (0.1f * amount as a base)
                    this.add(amount, amount * 0.1f);
                    break;
                } catch (NumberFormatException ignored) {
                }
            }
        }
    }

    public int getThirstLevel() {
        return thirstLevel;
    }

    public void setThirstLevel(int thirstLevel) {
        this.thirstLevel = thirstLevel;
    }

    public float getThirstSaturationLevel() {
        return thirstSaturationLevel;
    }

    public void setThirstSaturationLevel(float thirstSaturationLevel) {
        this.thirstSaturationLevel = thirstSaturationLevel;
    }

    public float getExhaustion() {
        return exhaustion;
    }

    public void setExhaustion(float exhaustion) {
        this.exhaustion = exhaustion;
    }
}
