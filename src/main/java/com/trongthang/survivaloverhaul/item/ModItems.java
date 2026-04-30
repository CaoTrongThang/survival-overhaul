package com.trongthang.survivaloverhaul.item;

import com.trongthang.survivaloverhaul.SurvivalOverhaul;
import com.trongthang.survivaloverhaul.effect.ModEffects;
import com.trongthang.survivaloverhaul.item.custom.BandageItem;
import com.trongthang.survivaloverhaul.item.custom.MedkitItem;
import com.trongthang.survivaloverhaul.item.custom.PurifiedWaterBucketItem;
import com.trongthang.survivaloverhaul.item.custom.PurifiedWaterItem;
import com.trongthang.survivaloverhaul.item.custom.CanteenItem;
import com.trongthang.survivaloverhaul.item.custom.EmptyCanteenItem;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.item.StewItem;
import net.minecraft.item.FoodComponent;
import net.minecraft.entity.effect.StatusEffectInstance;

public class ModItems {

        public static final Item PURIFIED_WATER = registerItem("purified_water",
                        new PurifiedWaterItem(new FabricItemSettings().maxCount(1)));
        public static final Item PURIFIED_WATER_BUCKET = registerItem("purified_water_bucket",
                        new PurifiedWaterBucketItem(
                                        new FabricItemSettings().maxCount(1)));

        public static final Item EMPTY_CANTEEN = registerItem("empty_canteen",
                        new EmptyCanteenItem(new FabricItemSettings().maxCount(1)));
        public static final Item CANTEEN = registerItem("canteen",
                        new CanteenItem(new FabricItemSettings().maxCount(1).maxDamage(3), false));
        public static final Item PURIFIED_CANTEEN = registerItem("purified_canteen",
                        new CanteenItem(new FabricItemSettings().maxCount(1).maxDamage(3), true));

        public static final Item EMPTY_LARGE_CANTEEN = registerItem("empty_large_canteen",
                        new EmptyCanteenItem(new FabricItemSettings().maxCount(1)));
        public static final Item LARGE_CANTEEN = registerItem("large_canteen",
                        new CanteenItem(new FabricItemSettings().maxCount(1).maxDamage(6), false));
        public static final Item PURIFIED_LARGE_CANTEEN = registerItem("purified_large_canteen",
                        new CanteenItem(new FabricItemSettings().maxCount(1).maxDamage(6), true));
        public static final Item MEDKIT = registerItem("medkit",
                        new MedkitItem(new FabricItemSettings().maxCount(16)));
        public static final Item BANDAGE = registerItem("bandage",
                        new BandageItem(new FabricItemSettings().maxCount(16)));

        public static final Item BOWL_OF_FIRE = registerItem("bowl_of_fire",
                        new StewItem(new FabricItemSettings().maxCount(1).food(
                                        new FoodComponent.Builder().hunger(6).saturationModifier(0.3f)
                                                        .statusEffect(new StatusEffectInstance(
                                                                        ModEffects.WARMING,
                                                                        3600, 0), 1.0f)
                                                        .alwaysEdible().build())));
        public static final Item BOWL_OF_ICE = registerItem("bowl_of_ice",
                        new StewItem(new FabricItemSettings().maxCount(1).food(
                                        new FoodComponent.Builder().hunger(6).saturationModifier(0.3f)
                                                        .statusEffect(new StatusEffectInstance(
                                                                        ModEffects.COOLING,
                                                                        3600, 0), 1.0f)
                                                        .alwaysEdible().build())));

        private static Item registerItem(String name, Item item) {
                return Registry.register(Registries.ITEM, new Identifier(SurvivalOverhaul.MOD_ID, name), item);
        }

        public static void registerModItems() {
                SurvivalOverhaul.LOGGER.info("Registering Mod Items for " + SurvivalOverhaul.MOD_ID);

                ((CanteenItem) CANTEEN).setEmptyVariant(EMPTY_CANTEEN);
                ((CanteenItem) PURIFIED_CANTEEN).setEmptyVariant(EMPTY_CANTEEN);
                ((CanteenItem) LARGE_CANTEEN).setEmptyVariant(EMPTY_LARGE_CANTEEN);
                ((CanteenItem) PURIFIED_LARGE_CANTEEN).setEmptyVariant(EMPTY_LARGE_CANTEEN);

                ((EmptyCanteenItem) EMPTY_CANTEEN).setFilledVariant(CANTEEN);
                ((EmptyCanteenItem) EMPTY_CANTEEN).setPurifiedFilledVariant(PURIFIED_CANTEEN);
                ((EmptyCanteenItem) EMPTY_LARGE_CANTEEN).setFilledVariant(LARGE_CANTEEN);
                ((EmptyCanteenItem) EMPTY_LARGE_CANTEEN).setPurifiedFilledVariant(PURIFIED_LARGE_CANTEEN);
        }
}
