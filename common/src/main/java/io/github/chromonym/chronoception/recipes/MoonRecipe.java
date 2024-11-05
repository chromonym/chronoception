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

public class MoonRecipe implements Recipe<TemporalTableRecipeInput> {

    private final Ingredient input;
    private final ItemStack fullOutput;
    private final ItemStack gibbousOutput;
    private final ItemStack quarterOutput;
    private final ItemStack crescentOutput;
    private final ItemStack newOutput;

    public MoonRecipe(Ingredient input, ItemStack fullOutput, ItemStack gibbousOutput, ItemStack quarterOutput, ItemStack crescentOutput, ItemStack newOutput) {
        this.input = input;
        this.fullOutput = fullOutput;
        this.gibbousOutput = gibbousOutput;
        this.quarterOutput = quarterOutput;
        this.crescentOutput = crescentOutput;
        this.newOutput = newOutput;
    }

    public Ingredient getInput() {
        return input;
    }

    public ItemStack getFullOutput() {
        return fullOutput;
    }

    public ItemStack getGibbousOutput() {
        return gibbousOutput;
    }

    public ItemStack getQuarterOutput() {
        return quarterOutput;
    }

    public ItemStack getCrescentOutput() {
        return crescentOutput;
    }

    public ItemStack getNewOutput() {
        return newOutput;
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
        return fullOutput;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Chronoception.MOON_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return Chronoception.MOON_RECIPE.get();
    }

    @Override
    public boolean matches(TemporalTableRecipeInput input, World world) {
        return this.input.test(input.getStackInSlot(0));
    }

    public static class Type implements RecipeType<MoonRecipe> {
        private Type() {}
        public static final Type INSTANCE = new Type();
    }
    
}
