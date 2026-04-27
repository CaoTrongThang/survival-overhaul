package com.trongthang.survivaloverhaul.client;

import com.trongthang.survivaloverhaul.mechanics.bodyparts.BodyPart;

import java.util.HashMap;
import java.util.Map;

public class ClientBodyDamageManager {
    private static final Map<BodyPart, Float> limbHealth = new HashMap<>();

    public static void setHealth(BodyPart part, float health) {
        limbHealth.put(part, health);
    }

    public static float getHealth(BodyPart part) {
        return limbHealth.getOrDefault(part, part.getMaxHealth());
    }
}
