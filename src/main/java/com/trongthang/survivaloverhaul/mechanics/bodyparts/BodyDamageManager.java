package com.trongthang.survivaloverhaul.mechanics.bodyparts;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import com.trongthang.survivaloverhaul.effect.ModEffects;

public class BodyDamageManager {
    private final LivingEntity entity;

    // Tracked Data Keys for synchronization
    public static final TrackedData<Float> HEAD_HEALTH = DataTracker.registerData(PlayerEntity.class,
            TrackedDataHandlerRegistry.FLOAT);
    public static final TrackedData<Float> TORSO_HEALTH = DataTracker.registerData(PlayerEntity.class,
            TrackedDataHandlerRegistry.FLOAT);
    public static final TrackedData<Float> LEFT_ARM_HEALTH = DataTracker.registerData(PlayerEntity.class,
            TrackedDataHandlerRegistry.FLOAT);
    public static final TrackedData<Float> RIGHT_ARM_HEALTH = DataTracker.registerData(PlayerEntity.class,
            TrackedDataHandlerRegistry.FLOAT);
    public static final TrackedData<Float> LEFT_LEG_HEALTH = DataTracker.registerData(PlayerEntity.class,
            TrackedDataHandlerRegistry.FLOAT);
    public static final TrackedData<Float> RIGHT_LEG_HEALTH = DataTracker.registerData(PlayerEntity.class,
            TrackedDataHandlerRegistry.FLOAT);
    public static final TrackedData<Float> LEFT_FOOT_HEALTH = DataTracker.registerData(PlayerEntity.class,
            TrackedDataHandlerRegistry.FLOAT);
    public static final TrackedData<Float> RIGHT_FOOT_HEALTH = DataTracker.registerData(PlayerEntity.class,
            TrackedDataHandlerRegistry.FLOAT);

    public BodyDamageManager(LivingEntity entity) {
        this.entity = entity;
    }

    public float getHealth(BodyPart part) {
        return entity.getDataTracker().get(getTrackedDataForPart(part));
    }

    public void setHealth(BodyPart part, float health) {
        float newHealth = Math.max(0, Math.min(health, part.getMaxHealth()));
        entity.getDataTracker().set(getTrackedDataForPart(part), newHealth);
    }

    private TrackedData<Float> getTrackedDataForPart(BodyPart part) {
        return switch (part) {
            case HEAD -> HEAD_HEALTH;
            case TORSO -> TORSO_HEALTH;
            case LEFT_ARM -> LEFT_ARM_HEALTH;
            case RIGHT_ARM -> RIGHT_ARM_HEALTH;
            case LEFT_LEG -> LEFT_LEG_HEALTH;
            case RIGHT_LEG -> RIGHT_LEG_HEALTH;
            case LEFT_FOOT -> LEFT_FOOT_HEALTH;
            case RIGHT_FOOT -> RIGHT_FOOT_HEALTH;
        };
    }

    public void update() {
        if (!entity.getWorld().isClient) {
            applyLimbEffects();
        }
    }

    private void applyLimbEffects() {
        // Run checks once per second to reduce overhead
        if (entity.age % 20 != 0)
            return;

        float headRatio = getHealth(BodyPart.HEAD) / BodyPart.HEAD.getMaxHealth();
        float leftLeg = getHealth(BodyPart.LEFT_LEG) / BodyPart.LEFT_LEG.getMaxHealth();
        float rightLeg = getHealth(BodyPart.RIGHT_LEG) / BodyPart.RIGHT_LEG.getMaxHealth();
        float leftFoot = getHealth(BodyPart.LEFT_FOOT) / BodyPart.LEFT_FOOT.getMaxHealth();
        float rightFoot = getHealth(BodyPart.RIGHT_FOOT) / BodyPart.RIGHT_FOOT.getMaxHealth();
        float leftArm = getHealth(BodyPart.LEFT_ARM) / BodyPart.LEFT_ARM.getMaxHealth();
        float rightArm = getHealth(BodyPart.RIGHT_ARM) / BodyPart.RIGHT_ARM.getMaxHealth();

        float minLegFoot = Math.min(Math.min(leftLeg, rightLeg), Math.min(leftFoot, rightFoot));

        // ----- BROKEN LEGS/FEET => Slowness -----
        if (minLegFoot <= 0f) {
            entity.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.SLOWNESS, 60, 1, false, false, true));
        }

        // ----- BROKEN ARMS => Weakness + Mining Fatigue -----
        if (leftArm <= 0f || rightArm <= 0f) {
            entity.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.WEAKNESS, 60, 0, false, false, true));
            entity.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.MINING_FATIGUE, 60, 1, false, false, true));
        }

        // ----- BROKEN HEAD => Nausea + Blindness (permanent) -----
        if (headRatio <= 0f) {
            entity.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.NAUSEA, 100, 0, false, false, true));
            entity.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.BLINDNESS, 60, 0, false, false, true));
        }

        // ----- HEADACHE: wounded head => shows icon + random Blindness flicker -----
        if (headRatio > 0f && headRatio < 0.66f) {
            int amplifier = headRatio < 0.33f ? 1 : 0;
            entity.addStatusEffect(new StatusEffectInstance(
                    ModEffects.HEADACHE, 40, amplifier, false, true, true));
            // Random chance of short Blindness flash
            float chance = headRatio < 0.33f ? 0.10f : 0.05f;
            if (entity.getRandom().nextFloat() < chance) {
                entity.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.BLINDNESS, 30, 0, false, false, true));
            }
        }

        // ----- HARD FALLING: wounded legs/feet => shows icon; damage amplification in
        // Mixin -----
        if (minLegFoot < 0.66f) {
            int amplifier = minLegFoot <= 0f ? 2 : (minLegFoot < 0.33f ? 1 : 0);
            entity.addStatusEffect(new StatusEffectInstance(
                    ModEffects.HARD_FALLING, 40, amplifier, false, true, true));
        }

        // ----- VULNERABILITY: wounded torso => shows icon; damage amplification in
        // Mixin -----
        float torsoRatio = getHealth(BodyPart.TORSO) / BodyPart.TORSO.getMaxHealth();
        if (torsoRatio < 0.66f) {
            int amplifier = torsoRatio <= 0f ? 2 : (torsoRatio < 0.33f ? 1 : 0);
            entity.addStatusEffect(new StatusEffectInstance(
                    ModEffects.VULNERABILITY, 40, amplifier, false, true, true));
        }
    }

    public void heal(BodyPart part, float amount) {
        setHealth(part, getHealth(part) + amount);
    }

    public void healAll() {
        for (BodyPart part : BodyPart.values()) {
            setHealth(part, part.getMaxHealth());
        }
    }

    public void applyDamage(DamageSource source, float amount) {
        if (amount <= 0)
            return;

        float limbDamage = amount * 0.5f; // Limb takes 50% of the actual damage to scale it

        if (source.isOf(DamageTypes.FALL) || source.isOf(DamageTypes.HOT_FLOOR)
                || source.isOf(DamageTypes.SWEET_BERRY_BUSH)) {
            // Damage feet and legs
            float footDmg = limbDamage * 0.6f;
            float legDmg = limbDamage * 0.4f;
            damage(BodyPart.LEFT_FOOT, footDmg / 2f);
            damage(BodyPart.RIGHT_FOOT, footDmg / 2f);
            damage(BodyPart.LEFT_LEG, legDmg / 2f);
            damage(BodyPart.RIGHT_LEG, legDmg / 2f);
        } else if (source.isOf(DamageTypes.FLY_INTO_WALL) || source.isOf(DamageTypes.FALLING_ANVIL)
                || source.isOf(DamageTypes.FALLING_BLOCK)) {
            // Damage head
            damage(BodyPart.HEAD, limbDamage);
        } else if (source.isOf(DamageTypes.STARVE) || source.isOf(DamageTypes.DROWN)) {
            // Damage torso
            damage(BodyPart.TORSO, limbDamage);
        } else {
            // Randomly distribute to other parts
            BodyPart[] parts = BodyPart.values();
            BodyPart hitPart = parts[entity.getRandom().nextInt(parts.length)];
            damage(hitPart, limbDamage);

            // If it's a big damage, splash some to torso
            if (limbDamage > 3.0f && hitPart != BodyPart.TORSO) {
                damage(BodyPart.TORSO, limbDamage * 0.3f);
            }
        }
    }

    public void damage(BodyPart part, float amount) {
        setHealth(part, getHealth(part) - amount);
    }

    public void writeNbt(net.minecraft.nbt.NbtCompound nbt) {
        net.minecraft.nbt.NbtCompound partsNbt = new net.minecraft.nbt.NbtCompound();
        for (BodyPart part : BodyPart.values()) {
            partsNbt.putFloat(part.name(), getHealth(part));
        }
        nbt.put("BodyParts", partsNbt);
    }

    public void readNbt(net.minecraft.nbt.NbtCompound nbt) {
        if (nbt.contains("BodyParts")) {
            net.minecraft.nbt.NbtCompound partsNbt = nbt.getCompound("BodyParts");
            for (BodyPart part : BodyPart.values()) {
                if (partsNbt.contains(part.name())) {
                    setHealth(part, partsNbt.getFloat(part.name()));
                }
            }
        }
    }
}
