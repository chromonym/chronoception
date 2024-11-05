package io.github.chromonym.chronoception.client.emi;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiStack;
import io.github.chromonym.chronoception.Chronoception;
import io.github.chromonym.chronoception.recipes.DayRecipe;
import io.github.chromonym.chronoception.recipes.MoonRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.util.Identifier;

public class ChronoceptionEmi {
    public static final Identifier SPRITESHEET = Identifier.of(Chronoception.MOD_ID, "textures/gui/emi_simplified.png");
    public static final EmiStack TEMPORAL_TABLE_STACK = EmiStack.of(Chronoception.TEMPORAL_TABLE_ITEM.get());
    public static final EmiRecipeCategory TEMPORAL_INFUSING
        = new EmiRecipeCategory(Identifier.of(Chronoception.MOD_ID, "temporal_infusing"), TEMPORAL_TABLE_STACK, new EmiTexture(SPRITESHEET, 0, 0, 16, 16, 16, 16, 16, 16));

    public static void register(EmiRegistry registry) {
        registry.addCategory(TEMPORAL_INFUSING);
        registry.addWorkstation(TEMPORAL_INFUSING, TEMPORAL_TABLE_STACK);
        for (RecipeEntry<DayRecipe> recipe : registry.getRecipeManager().listAllOfType(Chronoception.DAY_RECIPE.get())) {
            if (!recipe.value().getDayOutput().isEmpty()) {registry.addRecipe(new TemporalInfusingRecipe(recipe.id().withSuffixedPath("/day"), recipe.value().getInput(), recipe.value().getDayOutput(), "day"));}
            if (!recipe.value().getDuskOutput().isEmpty()) {registry.addRecipe(new TemporalInfusingRecipe(recipe.id().withSuffixedPath("/dusk"), recipe.value().getInput(), recipe.value().getDuskOutput(), "dusk"));}
            if (!recipe.value().getNightOutput().isEmpty()) {registry.addRecipe(new TemporalInfusingRecipe(recipe.id().withSuffixedPath("/night"), recipe.value().getInput(), recipe.value().getNightOutput(), "night"));}
        }
        for (RecipeEntry<MoonRecipe> recipe : registry.getRecipeManager().listAllOfType(Chronoception.MOON_RECIPE.get())) {
            if (!recipe.value().getFullOutput().isEmpty()) {registry.addRecipe(new TemporalInfusingRecipe(recipe.id().withSuffixedPath("/full"), recipe.value().getInput(), recipe.value().getFullOutput(), "full"));}
            if (!recipe.value().getGibbousOutput().isEmpty()) {registry.addRecipe(new TemporalInfusingRecipe(recipe.id().withSuffixedPath("/gibbous"), recipe.value().getInput(), recipe.value().getGibbousOutput(), "gibbous"));}
            if (!recipe.value().getQuarterOutput().isEmpty()) {registry.addRecipe(new TemporalInfusingRecipe(recipe.id().withSuffixedPath("/quarter"), recipe.value().getInput(), recipe.value().getQuarterOutput(), "quarter"));}
            if (!recipe.value().getCrescentOutput().isEmpty()) {registry.addRecipe(new TemporalInfusingRecipe(recipe.id().withSuffixedPath("/crescent"), recipe.value().getInput(), recipe.value().getCrescentOutput(), "crescent"));}
            if (!recipe.value().getNewOutput().isEmpty()) {registry.addRecipe(new TemporalInfusingRecipe(recipe.id().withSuffixedPath("/new"), recipe.value().getInput(), recipe.value().getNewOutput(), "new"));}
        }
    }
}
