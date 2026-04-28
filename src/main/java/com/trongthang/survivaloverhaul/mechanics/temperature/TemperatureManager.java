package com.trongthang.survivaloverhaul.mechanics.temperature;

import com.trongthang.survivaloverhaul.block.custom.BoilerBlock;
import com.trongthang.survivaloverhaul.block.custom.IceBoxBlock;
import com.trongthang.survivaloverhaul.config.ModConfig;
import com.trongthang.survivaloverhaul.effect.ModEffects;

import com.trongthang.survivaloverhaul.mechanics.thirst.IThirstData;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class TemperatureManager {
    private final LivingEntity entity;

    public static final TrackedData<Float> TEMPERATURE = DataTracker.registerData(PlayerEntity.class,
            TrackedDataHandlerRegistry.FLOAT);
    public static final TrackedData<Float> TARGET_TEMPERATURE = DataTracker.registerData(PlayerEntity.class,
            TrackedDataHandlerRegistry.FLOAT);

    public static final float MIN_TEMP = 0.0f;
    public static final float MAX_TEMP = 40.0f;
    public static final float NORMAL_TEMP = 20.0f;

    public TemperatureManager(LivingEntity entity) {
        this.entity = entity;
    }

    public void update() {
        if (!entity.getWorld().isClient) {
            // Run calculation every second
            if (entity.age % 20 != 0)
                return;

            float current = getTemperature();
            float target = NORMAL_TEMP;

            // Environment Modifiers
            World world = entity.getWorld();
            BlockPos pos = entity.getBlockPos();

            float biomeTemp = world.getBiome(pos).value().getTemperature();
            target = 20.0f + (biomeTemp - 0.8f) * 20.0f;

            if (!world.isDay())
                target -= 5.0f;
            if (world.isRaining())
                target -= 5.0f;
            if (entity.isSubmergedInWater())
                target -= 10.0f;

            float totalHeatInfluence = 0;
            float totalColdInfluence = 0;
            int range = ModConfig.temperatureDetectionRange;

            for (BlockPos iterPos : BlockPos.iterate(pos.add(-range, -range, -range), pos.add(range, range, range))) {
                BlockState state = world.getBlockState(iterPos);
                if (state.isOf(Blocks.CAMPFIRE) || state.isOf(Blocks.LAVA) || state.isOf(Blocks.FIRE)
                        || (state.isOf(com.trongthang.survivaloverhaul.block.ModBlocks.BOILER)
                                && state.get(BoilerBlock.LIT))) {
                    double distanceSq = pos.getSquaredDistance(iterPos);
                    if (distanceSq < 1.0)
                        distanceSq = 1.0;
                    totalHeatInfluence += 1.0f / (float) distanceSq;
                    if (state.isOf(com.trongthang.survivaloverhaul.block.ModBlocks.BOILER)) {
                        totalHeatInfluence += 0.5f / (float) distanceSq; // Boiler is stronger than regular sources?
                    }
                } else if (state.isOf(Blocks.ICE) || state.isOf(Blocks.PACKED_ICE) || state.isOf(Blocks.BLUE_ICE)
                        || (state.isOf(com.trongthang.survivaloverhaul.block.ModBlocks.ICE_BOX)
                                && state.get(IceBoxBlock.LIT))) {
                    double distanceSq = pos.getSquaredDistance(iterPos);
                    if (distanceSq < 1.0)
                        distanceSq = 1.0;
                    totalColdInfluence += 1.0f / (float) distanceSq;
                    if (state.isOf(com.trongthang.survivaloverhaul.block.ModBlocks.ICE_BOX)) {
                        totalColdInfluence += 0.5f / (float) distanceSq;
                    }
                }
            }

            if (totalHeatInfluence > 0)
                target += Math.min(20.0f, totalHeatInfluence * 10.0f);
            if (totalColdInfluence > 0)
                target -= Math.min(15.0f, totalColdInfluence * 10.0f);

            float changeRate = ModConfig.temperatureChangeSpeed;

            if (entity.isInLava()) {
                target = MAX_TEMP;
                changeRate = 2.0f; // Very fast
            } else if (entity.isOnFire()) {
                target = Math.max(target, 35.0f);
                changeRate = Math.max(changeRate, 0.5f);
            }

            target = MathHelper.clamp(target, MIN_TEMP, MAX_TEMP);
            setTargetTemperature(target);

            // Scaled change rate based on distance to target
            float diff = Math.abs(current - target);
            if (diff > 5.0f) {
                changeRate *= (diff / 5.0f);
            }

            if (current < target) {
                setTemperature(current + changeRate);
            } else if (current > target) {
                setTemperature(current - changeRate);
            }

            // Status effects loop
            float newTemp = getTemperature();
            if (newTemp <= 5.0f) {
                entity.addStatusEffect(new StatusEffectInstance(
                        ModEffects.FROSTBITE, 100, 0, false, false, true));
            } else if (newTemp >= 32.5f) {
                entity.addStatusEffect(new StatusEffectInstance(
                        ModEffects.HEATSTROKE, 100, 0, false, false, true));
            }

            // Apply gameplay effects for "Frozen" and "Hot" conditions
            // These align with the HUD icons changing
            if (entity instanceof PlayerEntity player && !player.getAbilities().invulnerable) {
                // Frozen Food (Hunger) - Temperature < 15.0
                if (newTemp < 15.0f) {
                    player.getHungerManager().addExhaustion(0.1f);
                }

                // Hot Thirst - Temperature > 25.0
                if (newTemp > 25.0f) {
                    ((IThirstData) player).survivalOverhaul$getThirstManager().addExhaustion(0.2f);
                }
            }
        }
    }

    public float getTemperature() {
        return entity.getDataTracker().get(TEMPERATURE);
    }

    public void setTemperature(float temperature) {
        float newTemp = MathHelper.clamp(temperature, MIN_TEMP, MAX_TEMP);
        entity.getDataTracker().set(TEMPERATURE, newTemp);
    }

    public float getTargetTemperature() {
        return entity.getDataTracker().get(TARGET_TEMPERATURE);
    }

    public void setTargetTemperature(float target) {
        entity.getDataTracker().set(TARGET_TEMPERATURE, MathHelper.clamp(target, MIN_TEMP, MAX_TEMP));
    }

    public void readNbt(NbtCompound nbt) {
        if (nbt.contains("Temperature")) {
            setTemperature(nbt.getFloat("Temperature"));
            setTargetTemperature(nbt.getFloat("Temperature"));
        } else {
            setTemperature(NORMAL_TEMP);
            setTargetTemperature(NORMAL_TEMP);
        }
    }

    public void writeNbt(NbtCompound nbt) {
        nbt.putFloat("Temperature", getTemperature());
    }
}
