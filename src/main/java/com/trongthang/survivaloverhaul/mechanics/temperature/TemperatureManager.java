package com.trongthang.survivaloverhaul.mechanics.temperature;

import com.trongthang.survivaloverhaul.SurvivalOverhaul;
import com.trongthang.survivaloverhaul.block.ModBlocks;
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
import net.minecraft.registry.tag.TagKey;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Identifier;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.BlockItem;
import net.minecraft.block.Block;

public class TemperatureManager {
    private final LivingEntity entity;

    private static final TagKey<Item> C_TORCHES = TagKey.of(RegistryKeys.ITEM, new Identifier("c", "torches"));
    private static final TagKey<Item> C_HEATING_ITEMS = TagKey.of(RegistryKeys.ITEM,
            new Identifier("c", "heating_items"));
    private static final TagKey<Item> C_COOLING_ITEMS = TagKey.of(RegistryKeys.ITEM,
            new Identifier("c", "cooling_items"));

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

        // 1. Base Biome Temperature (Refined scaling for frozen biomes)
        float biomeTemp = world.getBiome(pos).value().getTemperature();
        float ambient;
        if (biomeTemp < 0.15f) {
            // 0.0 biomeTemp -> 10.0 ambient
            // -1.0 biomeTemp -> -5.0 ambient (gentler than before)
            ambient = 10.0f + biomeTemp * 15.0f;
        } else {
            ambient = 20.0f + (biomeTemp - 0.8f) * 20.0f;
        }

        // 1a. Dimension Modifier
        ambient += getDimensionTemperatureModifier(world);

        // 1b. Season modifier (optional Fabric Seasons compat)
        if (FabricSeasonsCompat.isLoaded()) {
            ambient += FabricSeasonsCompat.getSeasonTempModifier(world);
        }

        // 2. Environmental Modifiers
        if (!world.isDay()) {
            if (biomeTemp > 0.9f) {
                ambient -= 12.0f; // Hot biomes get colder at night
            } else if (biomeTemp < 0.2f) {
                ambient -= 3.0f; // cold biomes night penalty reduced
            } else {
                ambient -= 5.0f;
            }
        }
        if (world.isRaining() && world.isSkyVisible(pos)) {
            ambient -= 5.0f; // Only colder if exposed to rain/snow
        }
        if (entity.isSubmergedInWater())
            ambient -= 10.0f;

        // 2.5 Altitude and Depth Modifiers
        int y = pos.getY();
        if (y > 80) {
            ambient -= (y - 80) * 0.02f; // Gets colder as you go higher, reduced harshness
        } else if (y < 40 && !world.isSkyVisible(pos)) {
            // Only apply underground "coolness" if the base temperature is warm.
            // If it's already freezing outside, caves should be neutral or slightly warmer
            // (shelter).
            if (ambient > 15.0f) {
                ambient -= 1.0f;
            }
            if (y < 0 && ambient > 10.0f) {
                ambient -= (0 - y) * 0.02f;
            }
        }

        // 3. Thermal Source Influence (Blocks)
        float thermalInfluence = calculateThermalSourceInfluence();
        ambient += thermalInfluence;

        // 3.5 Held Items Influence
        ambient += getHeldItemInfluence(entity.getMainHandStack());
        ambient += getHeldItemInfluence(entity.getOffHandStack());

        // 3.6 Armor Influence
        for (ItemStack armor : entity.getArmorItems()) {
            if (!armor.isEmpty() && armor.getItem() instanceof ArmorItem armorItem) {
                if (armorItem.getMaterial() == ArmorMaterials.LEATHER) {
                    ambient += 2.5f; // Leather gives some warmth
                }
            }
        }

        // 3.8 Status Effect Influence
        if (entity.hasStatusEffect(ModEffects.WARMING)) {
            ambient += 35.0f;
        }
        if (entity.hasStatusEffect(ModEffects.COOLING)) {
            ambient -= 35.0f;
        }

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
                float dist = (float) Math.sqrt(distanceSq);
                heat += 1.0f / dist;
                if (state.isOf(ModBlocks.BOILER)) {
                    heat += 0.5f / dist; // Boilers are more effective
                }
            } else if (isColdSource(state)) {
                float dist = (float) Math.sqrt(distanceSq);
                if (state.isOf(Blocks.WATER)) {
                    cold += 0.2f / dist; // Water provides a very weak cooling effect over distance
                } else {
                    cold += 1.0f / dist;
                    if (state.isOf(ModBlocks.ICE_BOX)) {
                        cold += 0.5f / dist; // Ice Boxes are more effective
                    }
                }
            }
        }

        float totalModifier = 0;
        if (heat > 0)
            totalModifier += Math.min(40.0f, heat * 18.0f); // Higher cap and even stronger multiplier
        if (cold > 0)
            totalModifier -= Math.min(15.0f, cold * 10.0f); // Lowered impact of cold blocks to prevent them from
                                                            // drowning out fires

        return totalModifier;
    }

    private boolean isHeatSource(BlockState state) {
        return (state.isOf(Blocks.CAMPFIRE) && state.contains(net.minecraft.state.property.Properties.LIT)
                && state.get(net.minecraft.state.property.Properties.LIT))
                || state.isOf(Blocks.LAVA) || state.isOf(Blocks.FIRE)
                || (state.isOf(ModBlocks.BOILER) && state.get(BoilerBlock.LIT))
                || ((state.isOf(Blocks.FURNACE) || state.isOf(Blocks.BLAST_FURNACE) || state.isOf(Blocks.SMOKER))
                        && state.contains(net.minecraft.state.property.Properties.LIT)
                        && state.get(net.minecraft.state.property.Properties.LIT));
    }

    private boolean isColdSource(BlockState state) {
        return state.isOf(Blocks.ICE) || state.isOf(Blocks.PACKED_ICE) || state.isOf(Blocks.BLUE_ICE)
                || (state.isOf(ModBlocks.ICE_BOX) && state.get(IceBoxBlock.LIT))
                || state.isOf(Blocks.WATER);
    }

    private float getHeldItemInfluence(ItemStack stack) {
        if (stack.isEmpty())
            return 0f;

        Item item = stack.getItem();
        if (stack.isIn(C_HEATING_ITEMS) || stack.isIn(C_TORCHES) || stack.isIn(ItemTags.COALS)
                || item == Items.LAVA_BUCKET) {
            return 2.5f;
        }
        if (stack.isIn(C_COOLING_ITEMS) || item == Items.WATER_BUCKET
                || item == com.trongthang.survivaloverhaul.item.ModItems.PURIFIED_WATER_BUCKET
                || item == Items.SNOWBALL) {
            return -2.5f;
        }
        if (item instanceof BlockItem blockItem) {
            Block block = blockItem.getBlock();
            if (block == Blocks.CAMPFIRE || block == Blocks.FIRE || block == ModBlocks.BOILER
                    || block == Blocks.MAGMA_BLOCK)
                return 2.5f;
            if (block == Blocks.ICE || block == Blocks.PACKED_ICE || block == Blocks.BLUE_ICE
                    || block == ModBlocks.ICE_BOX)
                return -2.5f;
        }
        return 0f;
    }

    private float getDimensionTemperatureModifier(World world) {
        String dimensionId = world.getRegistryKey().getValue().toString();
        for (String entry : ModConfig.dimensionTemperatureModifiers) {
            String[] parts = entry.split("=");
            if (parts.length == 2 && parts[0].trim().equals(dimensionId)) {
                try {
                    return Float.parseFloat(parts[1].trim());
                } catch (NumberFormatException e) {
                    SurvivalOverhaul.LOGGER.error("Invalid dimension temperature modifier in config: " + entry);
                }
            }
        }
        return 0f;
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
            StatusEffectInstance currentEffect = entity.getStatusEffect(ModEffects.FROSTBITE);
            if (currentEffect == null || currentEffect.getDuration() <= 40) {
                entity.addStatusEffect(new StatusEffectInstance(ModEffects.FROSTBITE, 100, 0, false, false, true));
            }
        } else if (state == TemperatureState.HOT) {
            StatusEffectInstance currentEffect = entity.getStatusEffect(ModEffects.HEATSTROKE);
            if (currentEffect == null || currentEffect.getDuration() <= 40) {
                entity.addStatusEffect(new StatusEffectInstance(ModEffects.HEATSTROKE, 100, 0, false, false, true));
            }
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

        // Priority states (Dying)
        if (temp <= 5.0f) {
            next = TemperatureState.FREEZING;
        } else if (temp >= 32.5f) {
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
