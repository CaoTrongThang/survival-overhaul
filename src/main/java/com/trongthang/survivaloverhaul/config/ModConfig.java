package com.trongthang.survivaloverhaul.config;

import eu.midnightdust.lib.config.MidnightConfig;

public class ModConfig extends MidnightConfig {

    // --- General ---
    @Entry(category = "thirst", name = "Enable Thirst")
    public static boolean enableThirst = true;

    // --- Temperature ---
    @Entry(category = "temperature", name = "Enable Temperature")
    public static boolean enableTemperature = true;

    @Entry(category = "temperature", name = "Temperature Change Speed", isSlider = true, min = 0.01f, max = 1.0f)
    public static float temperatureChangeSpeed = 0.1f;

    @Entry(category = "temperature", name = "Temperature Detection Range", min = 1, max = 8)
    public static int temperatureDetectionRange = 3;

    // --- Thirst Depletion ---
    @Entry(category = "thirst", name = "Thirst Depletion Multiplier", isSlider = true, min = 0.0f, max = 10.0f)
    public static float thirstDepletionMultiplier = 2.0f;

    @Entry(category = "thirst", name = "Thirst Exhaustion Threshold", isSlider = true, min = 0.1f, max = 10.0f)
    public static float thirstExhaustionThreshold = 4.0f;

    @Entry(category = "thirst", name = "Thirst Sprint Threshold", min = 0, max = 20)
    public static int thirstSprintThreshold = 6;

    @Entry(category = "thirst", name = "Max Thirst Level", min = 6, max = 40)
    public static int maxThirstLevel = 20;

    // --- Drinking ---
    @Entry(category = "thirst", name = "Enable Rain Drinking")
    public static boolean enableRainDrinking = true;

    @Entry(category = "thirst", name = "Enable Hand Drinking")
    public static boolean enableHandDrinking = true;

    @Entry(category = "thirst", name = "Thirst from Water Source")
    public static int thirstFromWater = 1;

    @Entry(category = "thirst", name = "Thirst from Rain")
    public static int thirstFromRain = 1;

    @Entry(category = "thirst", name = "Dehydration Chance from Sources", isSlider = true, min = 0.0f, max = 1.0f)
    public static float dehydrationChanceFromSources = 0.2f;

    // --- Items ---
    @Entry(category = "thirst", name = "Dehydration Chance from Items", isSlider = true, min = 0.0f, max = 1.0f)
    public static float dehydrationChanceFromItems = 0.1f;

    @Entry(category = "thirst", name = "Item Thirst Values")
    public static java.util.List<String> itemThirstValues = java.util.Arrays.asList(
            "minecraft:melon_slice=1", "minecraft:apple=1", "croptopia:coconut=1",
            "minecraft:potion=2", "wilderwild:split_coconut=2",
            "minecraft:milk_bucket=3", "minecraft:suspicious_stew=3", "minecraft:mushroom_stew=3",
            "minecraft:beetroot_soup=3", "croptopia:blackberry_jam=3", "croptopia:blueberry_jam=3",
            "croptopia:cherry_jam=3", "croptopia:elderberry_jam=3", "croptopia:peach_jam=3",
            "croptopia:raspberry_jam=3", "croptopia:strawberry_jam=3", "expandeddelight:apple_juice=3",
            "expandeddelight:sweet_berry_juice=3", "expandeddelight:glow_berry_juice=3",
            "expandeddelight:sweet_berry_jelly=3", "expandeddelight:glow_berry_jelly=3",
            "croptopia:grape_jam=3", "dangerousplants:boom_berries=3",
            "minecraft:rabbit_stew=6", "croptopia:apple_juice=6", "croptopia:cranberry_juice=6",
            "croptopia:grape_juice=6", "croptopia:melon_juice=6", "croptopia:orange_juice=6",
            "croptopia:pineapple_juice=6", "croptopia:saguaro_juice=6", "croptopia:tomato_juice=6",
            "croptopia:apricot_juice=6", "betternether:stalagnate_bowl_wart=6", "croptopia:leek_soup=6",
            "croptopia:potato_soup=6", "croptopia:pumpkin_soup=6", "expandeddelight:asparagus_soup=6",
            "expandeddelight:asparagus_soup_creamy=6", "expandeddelight:gpeanut_honey_soup=6",
            "farmersdelight:chicken_soup=6", "farmersdelight:vegetable_soup=6", "farmersdelight:pumpkin_soup=6",
            "farmersdelight:noodle_soup=6", "farmersdelight:bowl_of_guardian_soup=6",
            "oceansdelight:guardian_soup=8");

    // --- Damage ---
    @Entry(category = "thirst", name = "Enable Thirst Damage")
    public static boolean enableThirstDamage = true;

    @Entry(category = "thirst", name = "Thirst Damage", min = 1, max = 20)
    public static int thirstDamage = 8;

    @Entry(category = "thirst", name = "Thirst Interval (Thirst check interval)", min = 20, max = 200)
    public static int thirstInterval = 80;

    // --- Body Parts ---
    @Entry(category = "bodyparts", name = "Enable Body Damage")
    public static boolean enableBodyDamage = true;

    @Entry(category = "bodyparts", name = "Limb Damage Multiplier", isSlider = true, min = 0.0f, max = 2.0f)
    public static float limbDamageMultiplier = 0.5f;

    @Entry(category = "bodyparts", name = "Enable Healthy Bonus")
    public static boolean enableHealthyBonus = true;

    @Entry(category = "bodyparts", name = "Healthy Bonus Threshold (0.95 = 95% health then give the buff)", isSlider = true, min = 0.5f, max = 1.0f)
    public static float healthyBonusThreshold = 0.95f;

    public enum HudPosition {
        BOTTOM_RIGHT, BOTTOM_LEFT, TOP_RIGHT, TOP_LEFT, MIDDLE_RIGHT, MIDDLE_LEFT, TOP_MIDDLE
    }

    @Entry(category = "bodyparts", name = "HUD Position")
    public static HudPosition hudPosition = HudPosition.MIDDLE_LEFT;

    @Entry(category = "bodyparts", name = "HUD Scale", isSlider = true, min = 0.5f, max = 3.0f)
    public static float hudScale = 2.0f;

    @Entry(category = "bodyparts", name = "HUD X Offset", isSlider = true, min = -500, max = 500)
    public static int hudXOffset = 0;

    @Entry(category = "bodyparts", name = "HUD Y Offset", isSlider = true, min = -500, max = 500)
    public static int hudYOffset = 0;
}
