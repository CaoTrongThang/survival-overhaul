package com.trongthang.survivaloverhaul.compat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.combatroll.CombatRoll;
import net.combatroll.api.event.ServerSideRollEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.LivingEntity;

public class CombatRollCompat {

    private static final Map<LivingEntity, Integer> livingEntityInvulnerableTicks = new ConcurrentHashMap<>();

    public static void register() {
        // 1. Register roll start event
        ServerSideRollEvents.PLAYER_START_ROLLING.register((player, direction) -> {
            int invulnerabilityTicks = CombatRoll.config.invulnerable_ticks_upon_roll;
            livingEntityInvulnerableTicks.put(player, invulnerabilityTicks);
        });

        // 2. Register server tick for countdown
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            List<LivingEntity> toRemove = new ArrayList<>();

            livingEntityInvulnerableTicks.forEach((entity, ticks) -> {
                if (ticks <= 0) {
                    toRemove.add(entity);
                } else {
                    livingEntityInvulnerableTicks.put(entity, ticks - 1);
                }
            });

            // Remove expired entries
            toRemove.forEach(livingEntityInvulnerableTicks::remove);
        });
    }

    public static boolean isRolling(LivingEntity entity) {
        return livingEntityInvulnerableTicks.containsKey(entity);
    }
}
