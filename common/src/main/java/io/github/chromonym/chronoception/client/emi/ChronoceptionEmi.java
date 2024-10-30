package io.github.chromonym.chronoception.client.emi;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiStack;
import io.github.chromonym.chronoception.Chronoception;
import net.minecraft.util.Identifier;

public class ChronoceptionEmi {
    public static final Identifier SPRITESHEET = Identifier.of(Chronoception.MOD_ID, "textures/gui/emi_simplified.png");
    public static final EmiStack TEMPORAL_TABLE_STACK = EmiStack.of(Chronoception.TEMPORAL_TABLE_ITEM.get());
    public static final EmiRecipeCategory TEMPORAL_INFUSING
        = new EmiRecipeCategory(Identifier.of(Chronoception.MOD_ID, "temporal_infusing"), TEMPORAL_TABLE_STACK, new EmiTexture(SPRITESHEET, 0, 0, 16, 16, 16, 16, 16, 16));

    public static void register(EmiRegistry registry) {
        registry.addCategory(TEMPORAL_INFUSING);
        registry.addWorkstation(TEMPORAL_INFUSING, TEMPORAL_TABLE_STACK);
        registry.addRecipe(new TemporalInfusingRecipe(Identifier.of(Chronoception.MOD_ID, "/temporal_infusing/diurnal"), Chronoception.TEMPORAL_GEM.get(), Chronoception.DIURNAL_GEM.get()));
        registry.addRecipe(new TemporalInfusingRecipe(Identifier.of(Chronoception.MOD_ID, "/temporal_infusing/nocturnal"), Chronoception.TEMPORAL_GEM.get(), Chronoception.NOCTURNAL_GEM.get()));
        registry.addRecipe(new TemporalInfusingRecipe(Identifier.of(Chronoception.MOD_ID, "/temporal_infusing/crepuscular"), Chronoception.TEMPORAL_GEM.get(), Chronoception.CREPUSCULAR_GEM.get()));
        registry.addRecipe(new TemporalInfusingRecipe(Identifier.of(Chronoception.MOD_ID, "/temporal_infusing/full_moon"), Chronoception.TEMPORAL_DUST.get(), Chronoception.FULL_MOON_DUST.get()));
        registry.addRecipe(new TemporalInfusingRecipe(Identifier.of(Chronoception.MOD_ID, "/temporal_infusing/gibbous_moon"), Chronoception.TEMPORAL_DUST.get(), Chronoception.GIBBOUS_DUST.get()));
        registry.addRecipe(new TemporalInfusingRecipe(Identifier.of(Chronoception.MOD_ID, "/temporal_infusing/quarter_moon"), Chronoception.TEMPORAL_DUST.get(), Chronoception.QUARTER_DUST.get()));
        registry.addRecipe(new TemporalInfusingRecipe(Identifier.of(Chronoception.MOD_ID, "/temporal_infusing/crescent_moon"), Chronoception.TEMPORAL_DUST.get(), Chronoception.CRESCENT_DUST.get()));
        registry.addRecipe(new TemporalInfusingRecipe(Identifier.of(Chronoception.MOD_ID, "/temporal_infusing/new_moon"), Chronoception.TEMPORAL_DUST.get(), Chronoception.NEW_MOON_DUST.get()));
    }
}
