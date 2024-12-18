package io.github.chromonym.chronoception.fabric;

import io.github.chromonym.chronoception.Chronoception;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder;

public final class ChronoceptionFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        Chronoception.init();

        FabricBrewingRecipeRegistryBuilder.BUILD.register(builder -> {
            Chronoception.registerBrewingRecipes(builder);
        });
    }
}
