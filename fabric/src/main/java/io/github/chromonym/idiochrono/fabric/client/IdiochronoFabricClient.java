package io.github.chromonym.idiochrono.fabric.client;

import io.github.chromonym.idiochrono.client.IdiochronoClient;
import net.fabricmc.api.ClientModInitializer;

public final class IdiochronoFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.
        IdiochronoClient.init();
    }
}
