package com.trongthang.survivaloverhaul.mechanics.temperature;

import com.trongthang.survivaloverhaul.block.custom.BoilerBlock;
import com.trongthang.survivaloverhaul.block.custom.IceBoxBlock;
import com.trongthang.survivaloverhaul.compat.FabricSeasonsCompat;
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

    public static final TrackedData<Float> BODY_TEMPERATURE = DataTracker.registerData(PlayerEntity.class,
            TrackedDataHandlerRegistry.FLOAT);
    public static final TrackedData<Float> AMBIENT_TEMPERATURE = DataTracker.registerData(PlayerEntity.class,
            TrackedDataHandlerRegistry.FLOAT);
    public static final TrackedData<Integer> TEMPERATURE_STATE = DataTracker.registerData(PlayerEntity.class,
            TrackedDataHandlerRegistry.INTEGER);

    public static final float MIN_TEMP = 0.0f;
    public static final float MAX_TEMP = 40.0f;
    public static final float NORMAL_TEMP = 20.0f;

    public TemperatureManager(LivingEntity entity) {
        this.entity = entity;
    }

    /**
     * Main update loop for temperature mechanics.
     * Core concept: Body Temperature tries to reach Ambient Temperature over time.
     */
    public void update() {
        if (entity.getWorld().isClient)
            return;

        // Run calculation every second (20 ticks) to save performance
        if (entity.age % 20 != 0)
            return;

        float ambientTemp = calculateAmbientTemperature();
        setAmbientTemperature(ambientTemp);

        updateBodyTemperature(ambientTemp);

        // Apply effects based on the updated state
        applyTemperatureEffects();
    }

    /**
     * Calculates the "Target" temperature the player's body will try to reach.
     * Based on Biome, Day/Night, Rain, and nearby Thermal Sources (Fire, Ice, etc.)
     */
    private float calculateAmbientTemperature() {
        World world = entity.getWorld();
        BlockPos pos = entity.getBlockPos();

        // 1. Base Biome Temperature
        float biomeTemp = world.getBiome(pos).value().getTemperature();
        float ambient = 20.0f + (biomeTemp - 0.8f) * 20.0f;

        // 1b. Season modifier (optional Fabric Seasons compat)
        if (FabricSeasonsCompat.isLoaded()) {
            ambient += FabricSeasonsCompat.getSeasonTempModifier(world);
        }

        // 2. Environmental Modifiers
        if (!world.isDay())
            ambient -= 5.0f;
        if (world.isRaining())
            ambient -= 5.0f;
        if (entity.isSubmergedInWater())
            ambient -= 10.0f;

        // 3. Thermal Source Influence (Blocks)
        float thermalInfluence = calculateThermalSourceInfluence();
        ambient += thermalInfluence;

        // 4. Special cases (Lava/Fire)
        if (entity.isInLava()) {
            ambient = MAX_TEMP;
        } else if (entity.isOnFire()) {
            ambient = Math.max(ambient, 35.0f);
        }

        return MathHelper.clamp(ambient, MIN_TEMP, MAX_TEMP);
    }

    /**
     * Scans nearby blocks for heat or cold sources.
     * Returns a total temperature shift value.
     */
    private float calculateThermalSourceInfluence() {
        World world = entity.getWorld();
        BlockPos pos = entity.getBlockPos();
        float heat = 0;
        float cold = 0;
        int range = ModConfig.temperatureDetectionRange;

        for (BlockPos iterPos : BlockPos.iterate(pos.add(-range, -range, -range), pos.add(range, range, range))) {
            BlockState state = world.getBlockState(iterPos);
            double distanceSq = pos.getSquaredDistance(iterPos);
            if (distanceSq < 1.0)
                distanceSq = 1.0;

            if (isHeatSource(state)) {
                heat += 1.0f / (float) distanceSq;
                if (state.isOf(com.trongthang.survivaloverhaul.block.ModBlocks.BOILER)) {
                    heat += 0.5f / (float) distanceSq; // Boilers are more effective
                }
            } else if (isColdSource(state)) {
                cold += 1.0f / (float) distanceSq;
                if (state.isOf(com.trongthang.survivaloverhaul.block.ModBlocks.ICE_BOX)) {
                    cold += 0.5f / (float) distanceSq; // Ice Boxes are more effective
                }
            }
        }

        float totalModifier = 0;
        if (heat > 0)
            totalModifier += Math.min(20.0f, heat * 10.0f);
        if (cold > 0)
            totalModifier -= Math.min(15.0f, cold * 10.0f);

        return totalModifier;
    }

    private boolean isHeatSource(BlockState state) {
        return state.isOf(Blocks.CAMPFIRE) || state.isOf(Blocks.LAVA) || state.isOf(Blocks.FIRE)
                || (state.isOf(com.trongthang.survivaloverhaul.block.ModBlocks.BOILER) && state.get(BoilerBlock.LIT));
    }

    private boolean isColdSource(BlockState state) {
        return state.isOf(Blocks.ICE) || state.isOf(Blocks.PACKED_ICE) || state.isOf(Blocks.BLUE_ICE)
                || (state.isOf(com.trongthang.survivaloverhaul.block.ModBlocks.ICE_BOX) && state.get(IceBoxBlock.LIT));
    }

    /**
     * Moves the player's Body Temperature towards the Ambient Temperature.
     */
    private void updateBodyTemperature(float ambientTemp) {
        float currentBodyTemp = getBodyTemperature();
        float speed = ModConfig.temperatureChangeSpeed;

        // Lava/Fire makes temperature change much faster
        if (entity.isInLava()) {
            speed = 2.0f;
        } else if (entity.isOnFire()) {
            speed = Math.max(speed, 0.5f);
        }

        // Scaled change rate: the further you are from target, the faster you change
        float diff = Math.abs(currentBodyTemp - ambientTemp);
        if (diff > 5.0f) {
            speed *= (diff / 5.0f);
        }

        // Apply the change
        if (currentBodyTemp < ambientTemp) {
            setBodyTemperature(currentBodyTemp + speed);
        } else if (currentBodyTemp > ambientTemp) {
            setBodyTemperature(currentBodyTemp - speed);
        }

        // Update the discrete state (Normal, Cold, Freezing, etc.)
        updateStableState(getBodyTemperature());
    }

    /**
     * Applies status effects and exhaustion based on the current Temperature State.
     */
    private void applyTemperatureEffects() {
        TemperatureState state = getState();

        // 1. Status Effects (Dying conditions)
        if (state == TemperatureState.FREEZING) {
            entity.addStatusEffect(new StatusEffectInstance(ModEffects.FROSTBITE, 100, 0, false, false, true));
        } else if (state == TemperatureState.HOT) {
            entity.addStatusEffect(new StatusEffectInstance(ModEffects.HEATSTROKE, 100, 0, false, false, true));
        }

        // 2. Gameplay impacts (Hunger/Thirst exhaustion)
        if (entity instanceof PlayerEntity player && !player.getAbilities().invulnerable) {
            if (isCold()) {
                player.getHungerManager().addExhaustion(0.1f); // Cold makes you hungrier
            }
            if (isHot()) {
                ((IThirstData) player).survivalOverhaul$getThirstManager().addExhaustion(0.2f); // Heat makes you
                                                                                                // thirstier
            }
        }
    }

    private void updateStableState(float temp) {
        TemperatureState current = getState();
        TemperatureState next = current;

        boolean hasFrostbite = entity.hasStatusEffect(ModEffects.FROSTBITE);
        boolean hasHeatstroke = entity.hasStatusEffect(ModEffects.HEATSTROKE);

        // Priority states (Dying)
        if (temp <= 5.0f || hasFrostbite) {
            next = TemperatureState.FREEZING;
        } else if (temp >= 32.5f || hasHeatstroke) {
            next = TemperatureState.HOT;
        }
        // Hysteresis for COLD
        else if (temp < 14.0f) {
            next = TemperatureState.COLD;
        } else if (current == TemperatureState.COLD && temp < 15.5f) {
            next = TemperatureState.COLD;
        }
        // Hysteresis for WARM
        else if (temp > 26.0f) {
            next = TemperatureState.WARM;
        } else if (current == TemperatureState.WARM && temp > 24.5f) {
            next = TemperatureState.WARM;
        }
        // Otherwise Normal
        else {
            next = TemperatureState.NORMAL;
        }

        if (next != current) {
            setStableState(next);
        }
    }

    public TemperatureState getState() {
        return TemperatureState.fromId(entity.getDataTracker().get(TEMPERATURE_STATE));
    }

    public void setStableState(TemperatureState state) {
        entity.getDataTracker().set(TEMPERATURE_STATE, state.getId());
    }

    public boolean isCold() {
        TemperatureState state = getState();
        return state == TemperatureState.COLD || state == TemperatureState.FREEZING;
    }

    public boolean isHot() {
        TemperatureState state = getState();
        return state == TemperatureState.WARM || state == TemperatureState.HOT;
    }

    public float getBodyTemperature() {
        return entity.getDataTracker().get(BODY_TEMPERATURE);
    }

    public void setBodyTemperature(float temperature) {
        float newTemp = MathHelper.clamp(temperature, MIN_TEMP, MAX_TEMP);
        entity.getDataTracker().set(BODY_TEMPERATURE, newTemp);
    }

    public float getAmbientTemperature() {
        return entity.getDataTracker().get(AMBIENT_TEMPERATURE);
    }

    public void setAmbientTemperature(float target) {
        entity.getDataTracker().set(AMBIENT_TEMPERATURE, MathHelper.clamp(target, MIN_TEMP, MAX_TEMP));
    }

    public void readNbt(NbtCompound nbt) {
        if (nbt.contains("Temperature")) {
            setBodyTemperature(nbt.getFloat("Temperature"));
            setAmbientTemperature(nbt.getFloat("Temperature"));
            updateStableState(getBodyTemperature());
        } else {
            setBodyTemperature(NORMAL_TEMP);
            setAmbientTemperature(NORMAL_TEMP);
            setStableState(TemperatureState.NORMAL);
        }
    }

    public void writeNbt(NbtCompound nbt) {
        nbt.putFloat("Temperature", getBodyTemperature());
    }
}
