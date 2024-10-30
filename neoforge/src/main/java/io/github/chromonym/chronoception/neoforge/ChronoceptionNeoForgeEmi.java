package io.github.chromonym.chronoception.neoforge;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import io.github.chromonym.chronoception.client.emi.ChronoceptionEmi;

@EmiEntrypoint
public class ChronoceptionNeoForgeEmi implements EmiPlugin {

    @Override
    public void register(EmiRegistry registry) {
        ChronoceptionEmi.register(registry);
    }
    
}
