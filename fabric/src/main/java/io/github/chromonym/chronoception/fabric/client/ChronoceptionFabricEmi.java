package io.github.chromonym.chronoception.fabric.client;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import io.github.chromonym.chronoception.client.emi.ChronoceptionEmi;

public class ChronoceptionFabricEmi implements EmiPlugin {

    @Override
    public void register(EmiRegistry registry) {
        ChronoceptionEmi.register(registry);
    }
}
