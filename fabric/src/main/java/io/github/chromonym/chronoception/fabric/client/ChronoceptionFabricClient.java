package io.github.chromonym.chronoception.fabric.client;

import io.github.chromonym.chronoception.Chronoception;
import io.github.chromonym.chronoception.client.ChronoceptionClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;

public final class ChronoceptionFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.
        ChronoceptionClient.init();
        BlockRenderLayerMap.INSTANCE.putBlock(Chronoception.CREPUSCULAR_GHOSTBLOCK.get(), RenderLayer.getTranslucent());
        ModelPredicateProviderRegistry.register(Chronoception.TRUE_CLOCK.get(), Identifier.ofVanilla("server_time"), ChronoceptionClient.trueClockProvider);
    }
}
