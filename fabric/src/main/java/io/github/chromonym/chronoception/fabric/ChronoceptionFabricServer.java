package io.github.chromonym.chronoception.fabric;

import io.github.chromonym.chronoception.ChronoceptionServer;
import net.fabricmc.api.DedicatedServerModInitializer;

public class ChronoceptionFabricServer implements DedicatedServerModInitializer {

    @Override
    public void onInitializeServer() {
        ChronoceptionServer.init();
    }
    
}
