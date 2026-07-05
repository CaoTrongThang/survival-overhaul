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

    // Breakdown components synced to the client for the Thermometer HUD
    public static final TrackedData<Float> BIOME_CONTRIBUTION = DataTracker.registerData(PlayerEntity.class,
            TrackedDataHandlerRegistry.FLOAT);
    public static final TrackedData<Float> ENVIRONMENT_MODIFIER = DataTracker.registerData(PlayerEntity.class,
            TrackedDataHandlerRegistry.FLOAT);
    public static final TrackedData<Float> THERMAL_MODIFIER = DataTracker.registerData(PlayerEntity.class,
            TrackedDataHandlerRegistry.FLOAT);
    public static final TrackedData<Float> EQUIPMENT_MODIFIER = DataTracker.registerData(PlayerEntity.class,
            TrackedDataHandlerRegistry.FLOAT);

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

        // 1. Biome Base
        // biomeTemp 0.0 (frozen) → ~10°, 0.8 (normal) → 20°, 2.0 (desert/nether) → ~28°
        float biomeTemp = world.getBiome(pos).value().getTemperature();
        float biomeBase;
        if (biomeTemp < 0.15f) {
            biomeBase = 10.0f + biomeTemp * 15.0f;
        } else {
            biomeBase = 20.0f + (biomeTemp - 0.8f) * 20.0f; // ×20: hot biomes (desert=44°, clamped to 40°→HOT)
        }

        // 1a. Dimension Modifier
        biomeBase += getDimensionTemperatureModifier(world);

        // 1b. Season modifier
        float seasonMod = 0f;
        if (FabricSeasonsCompat.isLoaded() && world.getRegistryKey() == net.minecraft.world.World.OVERWORLD) {
            seasonMod = FabricSeasonsCompat.getSeasonTempModifier(world);
            biomeBase += seasonMod;
        }

        float ambient = biomeBase;

        // 2. Environmental Modifiers
        float envMod = 0f;
        if (!world.isDay()) {
            if (biomeTemp > 0.9f) {
                envMod -= 12.0f; // Hot biomes cool dramatically at night (desert should go from HOT to WARM)
            } else if (biomeTemp < 0.2f) {
                envMod -= 1.0f; // Frozen biomes are already cold, barely changes
            } else {
                envMod -= 2.0f; // Normal biomes slightly colder at night
            }
        }
        if (world.isRaining() && world.isSkyVisible(pos)) {
            envMod -= 2.0f;
        }
        if (entity.isSubmergedInWater()) {
            envMod -= 5.0f;
        }

        // Altitude: only above y=100, gentler rate
        int y = pos.getY();
        if (y > 100) {
            envMod -= (y - 100) * 0.015f;
        }

        // Caves: slight warmth bonus (shelter from the elements)
        if (y < 40 && !world.isSkyVisible(pos)) {
            envMod += 2.0f;
        }

        // Env cap only applies to temperate biomes — prevents normal biome stacking to
        // 0°.
        // Extreme biomes (very hot or very cold) are allowed to swing harder.
        if (biomeTemp >= 0.15f && biomeTemp <= 0.9f) {
            envMod = Math.max(envMod, -8.0f);
        }

        ambient += envMod;

        // 3. Thermal Source Influence (Blocks)
        float thermalInfluence = calculateThermalSourceInfluence();
        ambient += thermalInfluence;

        // 4. Equipment (held items + armor + effects)
        float equipMod = 0f;
        equipMod += getHeldItemInfluence(entity.getMainHandStack());
        equipMod += getHeldItemInfluence(entity.getOffHandStack());

        boolean hasArmor = false;
        for (ItemStack armor : entity.getArmorItems()) {
            if (!armor.isEmpty() && armor.getItem() instanceof ArmorItem armorItem) {
                if (armorItem.getMaterial() == ArmorMaterials.LEATHER) {
                    equipMod += 2.0f;
                }

                // Sewing Table Coats
                NbtCompound nbt = armor.getSubNbt("survivaloverhaul");
                if (nbt != null && nbt.contains("CoatType")) {
                    String coatType = nbt.getString("CoatType");
                    if (coatType.equals("warming")) {
                        equipMod += 3.0f;
                    } else if (coatType.equals("cooling")) {
                        equipMod -= 3.0f;
                    }
                }

                hasArmor = true;
            }
        }

        if (entity.hasStatusEffect(ModEffects.WARMING)) {
            equipMod += 35.0f;
        }
        if (entity.hasStatusEffect(ModEffects.COOLING)) {
            equipMod -= 45.0f;
        }
        if (entity.hasStatusEffect(ModEffects.MILD_WARMING)) {
            equipMod += 15.0f;
        }
        if (entity.hasStatusEffect(ModEffects.MILD_COOLING)) {
            equipMod -= 15.0f;
        }

        if (entity.isInLava()) {
            ambient = MAX_TEMP;
            equipMod = 0f;
        } else if (entity.isOnFire()) {
            ambient = Math.max(ambient, 35.0f);
        }

        if (entity.hasStatusEffect(net.minecraft.entity.effect.StatusEffects.FIRE_RESISTANCE)) {
            float tempBeforeFireRes = ambient + equipMod;
            if (tempBeforeFireRes > 20.0f) {
                float fireResReduction = Math.min(15.0f, tempBeforeFireRes - 20.0f);
                equipMod -= fireResReduction;
            }
        }

        ambient += equipMod;

        // Safety floor for armored players in temperate biomes
        if (hasArmor && biomeTemp >= 0.15f && !entity.isSubmergedInWater()
                && !entity.hasStatusEffect(ModEffects.COOLING)
                && ambient < 8.0f) {
            ambient = 8.0f;
        }

        // Sync breakdown to client
        entity.getDataTracker().set(BIOME_CONTRIBUTION, biomeBase);
        entity.getDataTracker().set(ENVIRONMENT_MODIFIER, envMod);
        entity.getDataTracker().set(THERMAL_MODIFIER, thermalInfluence);
        entity.getDataTracker().set(EQUIPMENT_MODIFIER, equipMod);

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
                float strength = state.isOf(Blocks.CAMPFIRE) ? 1.5f
                        : ((state.isOf(Blocks.FURNACE) || state.isOf(Blocks.BLAST_FURNACE) || state.isOf(Blocks.SMOKER))
                                ? 0.5f
                                : 1.0f);
                heat += strength / dist;
                if (state.isOf(ModBlocks.BOILER)) {
                    heat += 0.5f / dist; // Boilers are more effective
                }
            } else if (isColdSource(state)) {
                float dist = (float) Math.sqrt(distanceSq);
                if (state.isOf(Blocks.WATER)) {
                    cold += 0.2f / dist; // Water provides a very weak cooling effect over distance
                } else {
                    float coldStrength = (state.isOf(Blocks.ICE) || state.isOf(Blocks.PACKED_ICE)
                            || state.isOf(Blocks.BLUE_ICE)) ? 1.5f : 1.0f;
                    cold += coldStrength / dist;
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
            totalModifier -= Math.min(25.0f, cold * 15.0f); // Increased impact of cold blocks

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
                ((IThirstData) player).survivalOverhaul$getThirstManager().addExhaustion(0.15f); // Heat makes you
                                                                                                 // thirstier
            }
        }
    }

    private void updateStableState(float temp) {
        TemperatureState current = getState();
        TemperatureState next = current;

        // Priority states (Dying)
        if (temp <= 0.0f) {
            next = TemperatureState.FREEZING;
        } else if (temp <= 8.0f) {
            next = TemperatureState.COLD;
        } else if (temp >= 37.0f) {
            next = TemperatureState.HOT;
        } else if (temp >= 32.0f) {
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

    public float getBiomeContribution() {
        return entity.getDataTracker().get(BIOME_CONTRIBUTION);
    }

    public float getEnvironmentModifier() {
        return entity.getDataTracker().get(ENVIRONMENT_MODIFIER);
    }

    public float getThermalModifier() {
        return entity.getDataTracker().get(THERMAL_MODIFIER);
    }

    public float getEquipmentModifier() {
        return entity.getDataTracker().get(EQUIPMENT_MODIFIER);
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
