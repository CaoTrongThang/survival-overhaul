package com.trongthang.survivaloverhaul.mechanics.bodyparts;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import java.util.HashMap;
import java.util.Map;

public class BodyDamageManager {
    private final LivingEntity entity;
    private final Map<BodyPart, Float> limbHealth = new HashMap<>();
    private boolean isDirty = false;

    public BodyDamageManager(LivingEntity entity) {
        this.entity = entity;
        // Initialize all limbs to max health
        for (BodyPart part : BodyPart.values()) {
            limbHealth.put(part, part.getMaxHealth());
        }
    }

    public float getHealth(BodyPart part) {
        return limbHealth.getOrDefault(part, part.getMaxHealth());
    }

    public void setHealth(BodyPart part, float health) {
        float old = getHealth(part);
        float newHealth = Math.max(0, Math.min(health, part.getMaxHealth()));
        limbHealth.put(part, newHealth);
        if (old != newHealth) {
            isDirty = true;
        }
    }

    public void update() {
        if (!entity.getWorld().isClient && isDirty
                && entity instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer) {
            com.trongthang.survivaloverhaul.networking.ModNetworking.sendBodyDamageSync(serverPlayer,
                    (com.trongthang.survivaloverhaul.mechanics.bodyparts.IBodyDamageData) entity);
            isDirty = false;
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
            // Damage legs
            float half = limbDamage / 2f;
            damage(BodyPart.LEFT_LEG, half);
            damage(BodyPart.RIGHT_LEG, half);
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

    public void writeNbt(NbtCompound nbt) {
        NbtCompound partsNbt = new NbtCompound();
        for (Map.Entry<BodyPart, Float> entry : limbHealth.entrySet()) {
            partsNbt.putFloat(entry.getKey().name(), entry.getValue());
        }
        nbt.put("BodyParts", partsNbt);
    }

    public void readNbt(NbtCompound nbt) {
        if (nbt.contains("BodyParts")) {
            NbtCompound partsNbt = nbt.getCompound("BodyParts");
            for (BodyPart part : BodyPart.values()) {
                if (partsNbt.contains(part.name())) {
                    setHealth(part, partsNbt.getFloat(part.name()));
                }
            }
        }
    }
}
