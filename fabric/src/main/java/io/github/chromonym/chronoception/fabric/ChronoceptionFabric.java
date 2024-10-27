package io.github.chromonym.chronoception.fabric;

import io.github.chromonym.chronoception.Chronoception;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder;
import net.minecraft.registry.Registries;

public final class ChronoceptionFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        Chronoception.init();

        FabricBrewingRecipeRegistryBuilder.BUILD.register(builder -> {
            builder.registerRecipes(Chronoception.DIURNAL_GEM.get(), Registries.POTION.getEntry(Chronoception.TIME_SET_DAY_POTION.get()));
            builder.registerRecipes(Chronoception.NOCTURNAL_GEM.get(), Registries.POTION.getEntry(Chronoception.TIME_SET_NIGHT_POTION.get()));
        });
    }
}
