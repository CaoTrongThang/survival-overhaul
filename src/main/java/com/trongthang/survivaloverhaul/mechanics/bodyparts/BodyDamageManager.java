package com.trongthang.survivaloverhaul.mechanics.bodyparts;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;

import com.trongthang.survivaloverhaul.mechanics.temperature.ITemperatureData;
import com.trongthang.survivaloverhaul.mechanics.thirst.IThirstData;
import com.trongthang.survivaloverhaul.mixin.LivingEntityAccessor;
import com.trongthang.survivaloverhaul.config.ModConfig;
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

        // ----- HEALTHY BODY: +1 Strength if >= 90% total health -----
        if (ModConfig.enableHealthyBonus) {
            float totalHealth = 0;
            float maxTotalHealth = 0;
            for (BodyPart part : BodyPart.values()) {
                totalHealth += getHealth(part);
                maxTotalHealth += part.getMaxHealth();
            }

            boolean isHealthy = (totalHealth / maxTotalHealth >= ModConfig.healthyBonusThreshold);
            boolean canApplyFeelingGood = true;

            for (StatusEffectInstance instance : entity.getStatusEffects()) {
                if (instance.getEffectType()
                        .getCategory() == StatusEffectCategory.HARMFUL) {
                    canApplyFeelingGood = false;
                    break;
                }
            }

            if (entity instanceof PlayerEntity player) {
                float temp = ((ITemperatureData) player).survivalOverhaul$getTemperatureManager().getBodyTemperature();
                int thirst = ((IThirstData) player).survivalOverhaul$getThirstManager().getThirstLevel();

                // If player is too cold (< 15.0), too hot (> 25.0), or thirsty (< 10)
                if (temp < 15.0f || temp > 25.0f || thirst < 10) {
                    canApplyFeelingGood = false;
                }
            }

            if (isHealthy && canApplyFeelingGood) {
                entity.addStatusEffect(new StatusEffectInstance(
                        ModEffects.FEELING_GOOD, 200, 0, false, false, true));
            }
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

    public boolean isAllHealed() {
        for (BodyPart part : BodyPart.values()) {
            if (getHealth(part) < part.getMaxHealth()) {
                return false;
            }
        }
        return true;
    }

    public void applyDamage(DamageSource source, float amount) {
        if (!ModConfig.enableBodyDamage || amount <= 0)
            return;

        float limbDamage = amount * ModConfig.limbDamageMultiplier;

        if (source.isIn(DamageTypeTags.IS_FALL) || source.isOf(DamageTypes.HOT_FLOOR)
                || source.isOf(DamageTypes.SWEET_BERRY_BUSH)) {
            // Damage feet and legs
            float footDmg = limbDamage * 0.6f;
            float legDmg = limbDamage * 0.4f;
            damage(BodyPart.LEFT_FOOT, footDmg / 2f);
            damage(BodyPart.RIGHT_FOOT, footDmg / 2f);
            damage(BodyPart.LEFT_LEG, legDmg / 2f);
            damage(BodyPart.RIGHT_LEG, legDmg / 2f);
        } else if (source.isIn(DamageTypeTags.DAMAGES_HELMET) || source.isOf(DamageTypes.FLY_INTO_WALL)) {
            // Damage head
            damage(BodyPart.HEAD, limbDamage);
        } else if (source.isIn(DamageTypeTags.IS_DROWNING) || source.isOf(DamageTypes.STARVE)) {
            // Damage torso
            damage(BodyPart.TORSO, limbDamage);
        } else if (source.getName().equals("punch_block")) {
            // Damage the specific hand being used (Main hand or Off hand)
            // Respects left-handed/right-handed settings
            Hand swingingHand = ((LivingEntityAccessor) entity).getPreferredHand();
            if (swingingHand == null)
                swingingHand = Hand.MAIN_HAND;

            Arm arm = (swingingHand == Hand.MAIN_HAND) ? entity.getMainArm()
                    : (entity.getMainArm() == Arm.LEFT ? Arm.RIGHT : Arm.LEFT);

            damage(arm == Arm.LEFT ? BodyPart.LEFT_ARM : BodyPart.RIGHT_ARM, limbDamage);
        } else {
            // Detect which body part was actually hit using attacker/projectile position.
            // Falls back to random for AoE, fire, magic, etc. (no positional data).
            BodyPart hitPart = HitLocationDetector.detect(entity, source);
            damage(hitPart, limbDamage);

            // If it's a big damage, splash some to torso.
            // Splash damage indicates internal injury/blunt force, so it ignores 50% of
            // armor.
            if (limbDamage > 3.0f && hitPart != BodyPart.TORSO) {
                damage(BodyPart.TORSO, limbDamage * 0.3f, true);
            }
        }
    }

    public void damage(BodyPart part, float amount) {
        damage(part, amount, false);
    }

    public void damage(BodyPart part, float amount, boolean ignoreArmor) {
        if (amount <= 0)
            return;

        // Apply armor reduction for this specific limb unless it's partially ignoring
        // armor (splash/internal)
        if (!ignoreArmor) {
            amount *= (1.0f - getLimbArmorReduction(part));
        } else {
            // Internal splash damage ignores 50% of the armor's protection
            amount *= (1.0f - getLimbArmorReduction(part) * 0.5f);
        }

        setHealth(part, getHealth(part) - amount);
    }

    private float getLimbArmorReduction(BodyPart part) {
        EquipmentSlot slot = switch (part) {
            case HEAD -> EquipmentSlot.HEAD;
            case TORSO, LEFT_ARM, RIGHT_ARM -> EquipmentSlot.CHEST;
            case LEFT_LEG, RIGHT_LEG -> EquipmentSlot.LEGS;
            case LEFT_FOOT, RIGHT_FOOT -> EquipmentSlot.FEET;
        };

        ItemStack armorStack = entity.getEquippedStack(slot);
        if (armorStack.isEmpty() || !(armorStack.getItem() instanceof ArmorItem armorItem)) {
            return 0f;
        }

        // Linear reduction: each armor point reduces limb damage by 4% (max 80% per
        // piece)
        float protection = armorItem.getProtection();

        // Chestplate only offers partial protection to arms
        if (part == BodyPart.LEFT_ARM || part == BodyPart.RIGHT_ARM) {
            protection *= 0.5f;
        }

        return Math.min(0.8f, protection * 0.04f);
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
