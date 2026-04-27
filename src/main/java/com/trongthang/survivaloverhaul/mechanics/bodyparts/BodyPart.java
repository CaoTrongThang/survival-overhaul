package com.trongthang.survivaloverhaul.mechanics.bodyparts;

public enum BodyPart {
    HEAD("Head", 20f),
    TORSO("Torso", 20f),
    LEFT_ARM("Left Arm", 20f),
    RIGHT_ARM("Right Arm", 20f),
    LEFT_LEG("Left Leg", 20f),
    RIGHT_LEG("Right Leg", 20f);

    private final String displayName;
    private final float maxHealth;

    BodyPart(String displayName, float maxHealth) {
        this.displayName = displayName;
        this.maxHealth = maxHealth;
    }

    public String getDisplayName() {
        return displayName;
    }

    public float getMaxHealth() {
        return maxHealth;
    }
}
