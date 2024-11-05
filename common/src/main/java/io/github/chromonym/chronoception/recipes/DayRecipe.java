package io.github.chromonym.chronoception.recipes;

import io.github.chromonym.chronoception.Chronoception;
import io.github.chromonym.chronoception.blockentities.TemporalTableBlockEntity.TemporalTableRecipeInput;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.world.World;

public class DayRecipe implements Recipe<TemporalTableRecipeInput> {

    private final Ingredient input;
    private final ItemStack dayOutput;
    private final ItemStack duskOutput;
    private final ItemStack nightOutput;

    public DayRecipe(Ingredient input, ItemStack dayOutput, ItemStack duskOutput, ItemStack nightOutput) {
        this.input = input;
        this.dayOutput = dayOutput;
        this.duskOutput = duskOutput;
        this.nightOutput = nightOutput;
    }

    public Ingredient getInput() {
        return input;
    }

    public ItemStack getDayOutput() {
        return dayOutput;
    }

    public ItemStack getDuskOutput() {
        return duskOutput;
    }

    public ItemStack getNightOutput() {
        return nightOutput;
    }

    @Override
    public ItemStack craft(TemporalTableRecipeInput input, WrapperLookup lookup) {
        return this.getResult(lookup).copy();
    }

    @Override
    public boolean fits(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResult(WrapperLookup registriesLookup) {
        return dayOutput;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Chronoception.DAY_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return Chronoception.DAY_RECIPE.get();
    }

    @Override
    public boolean matches(TemporalTableRecipeInput input, World world) {
        return this.input.test(input.getStackInSlot(0));
    }

    public static class Type implements RecipeType<DayRecipe> {
        private Type() {}
        public static final Type INSTANCE = new Type();
    }
    
}
