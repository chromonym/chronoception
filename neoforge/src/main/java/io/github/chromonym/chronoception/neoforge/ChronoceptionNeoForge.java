package io.github.chromonym.chronoception.neoforge;

import io.github.chromonym.chronoception.Chronoception;
import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.registry.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;

@Mod(Chronoception.MOD_ID)
public final class ChronoceptionNeoForge {
    public ChronoceptionNeoForge(IEventBus modBus) {
        // Run our common setup.
        NeoForge.EVENT_BUS.addListener(ChronoceptionNeoForge::registerBrewingRecipes);
        Chronoception.init();
    }

    public static void registerBrewingRecipes(RegisterBrewingRecipesEvent event) {
        BrewingRecipeRegistry.Builder builder = event.getBuilder();
        builder.registerRecipes(Chronoception.DIURNAL_GEM.get(), Registries.POTION.getEntry(Chronoception.TIME_SET_DAY_POTION.get()));
        builder.registerRecipes(Chronoception.NOCTURNAL_GEM.get(), Registries.POTION.getEntry(Chronoception.TIME_SET_NIGHT_POTION.get()));
    }
}
