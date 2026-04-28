package com.trongthang.survivaloverhaul.item;

import com.trongthang.survivaloverhaul.SurvivalOverhaul;
import com.trongthang.survivaloverhaul.item.custom.BandageItem;
import com.trongthang.survivaloverhaul.item.custom.MedkitItem;
import com.trongthang.survivaloverhaul.item.custom.PurifiedWaterBucketItem;
import com.trongthang.survivaloverhaul.item.custom.PurifiedWaterItem;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {

        public static final Item PURIFIED_WATER = registerItem("purified_water",
                        new PurifiedWaterItem(new FabricItemSettings().maxCount(16)));
        public static final Item PURIFIED_WATER_BUCKET = registerItem("purified_water_bucket",
                        new PurifiedWaterBucketItem(
                                        new FabricItemSettings().maxCount(1)));
        public static final Item MEDKIT = registerItem("medkit",
                        new MedkitItem(new FabricItemSettings().maxCount(16)));
        public static final Item BANDAGE = registerItem("bandage",
                        new BandageItem(new FabricItemSettings().maxCount(16)));

        private static Item registerItem(String name, Item item) {
                return Registry.register(Registries.ITEM, new Identifier(SurvivalOverhaul.MOD_ID, name), item);
        }

        public static void registerModItems() {
                SurvivalOverhaul.LOGGER.info("Registering Mod Items for " + SurvivalOverhaul.MOD_ID);
        }
}
