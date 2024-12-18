package io.github.chromonym.chronoception.mixin;

import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;

import io.github.chromonym.chronoception.client.ChronoceptionClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin extends World {
    protected ClientWorldMixin(MutableWorldProperties properties, RegistryKey<World> registryRef,
            DynamicRegistryManager registryManager, RegistryEntry<DimensionType> dimensionEntry,
            Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long biomeAccess,
            int maxChainedNeighborUpdates) {
        super(properties, registryRef, registryManager, dimensionEntry, profiler, isClient, debugWorld, biomeAccess,
                maxChainedNeighborUpdates);
    }

    @Override
    public long getTimeOfDay() {
        return super.getTimeOfDay() + (long)ChronoceptionClient.playerData.offset;
    }

    @Override
    public long getLunarTime() {
        return super.getLunarTime() + (long)ChronoceptionClient.playerData.offset;
    }
}
