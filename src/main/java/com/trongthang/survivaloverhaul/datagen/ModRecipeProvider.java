package com.trongthang.survivaloverhaul.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import java.util.function.Consumer;
import net.minecraft.data.server.recipe.ShapelessRecipeJsonBuilder;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.item.Items;
import com.trongthang.survivaloverhaul.item.ModItems;

public class ModRecipeProvider extends FabricRecipeProvider {
    public ModRecipeProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generate(Consumer<RecipeJsonProvider> exporter) {
        ShapelessRecipeJsonBuilder.create(RecipeCategory.FOOD, ModItems.BOWL_OF_FIRE, 1)
                .input(Items.BOWL)
                .input(Items.BLAZE_ROD)
                .input(Items.MAGMA_CREAM)
                .input(Items.MAGMA_BLOCK)
                .criterion(hasItem(Items.BLAZE_ROD), conditionsFromItem(Items.BLAZE_ROD))
                .offerTo(exporter);

        ShapelessRecipeJsonBuilder.create(RecipeCategory.FOOD, ModItems.BOWL_OF_ICE, 1)
                .input(Items.BOWL)
                .input(Items.BLUE_ICE)
                .input(Items.PACKED_ICE)
                .input(Items.SNOWBALL)
                .criterion(hasItem(Items.BLUE_ICE), conditionsFromItem(Items.BLUE_ICE))
                .offerTo(exporter);
    }
}
