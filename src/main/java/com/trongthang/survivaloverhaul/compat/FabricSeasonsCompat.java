package com.trongthang.survivaloverhaul.compat;

import io.github.lucaargolo.seasons.FabricSeasons;
import io.github.lucaargolo.seasons.utils.Season;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.World;

/**
 * Optional compatibility with Fabric Seasons.
 * Applies a seasonal temperature modifier to the ambient temperature
 * calculation.
 * Safe to call even when Fabric Seasons is not installed — isLoaded() guards
 * all usage.
 */
public class FabricSeasonsCompat {

    public static boolean isLoaded() {
        return FabricLoader.getInstance().isModLoaded("seasons");
    }

    /**
     * Returns a temperature offset (in °C on our 0–40 scale) based on the current
     * season.
     * Falls back to 0.0 if anything goes wrong.
     */
    public static float getSeasonTempModifier(World world) {
        try {
            Season season = FabricSeasons.getCurrentSeason(world);
            return switch (season) {
                case SPRING -> 0.0f; // Baseline
                case SUMMER -> 5.0f; // Warmer
                case FALL -> -3.0f; // Slightly cooler
                case WINTER -> -8.0f; // Cold
            };
        } catch (Exception e) {
            return 0.0f;
        }
    }
}
