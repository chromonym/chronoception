package io.github.chromonym.chronoception.fabric.client;

import io.github.chromonym.chronoception.client.ChronoceptionClient;
import net.fabricmc.api.ClientModInitializer;

public final class ChronoceptionFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.
        ChronoceptionClient.init();
    }
}
