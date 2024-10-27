package io.github.chromonym.chronoception.neoforge;

import io.github.chromonym.chronoception.Chronoception;
import io.github.chromonym.chronoception.ChronoceptionServer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

@Mod(value = Chronoception.MOD_ID, dist = Dist.DEDICATED_SERVER)
public class ChronoceptionNeoForgeServer {
    public ChronoceptionNeoForgeServer() {
        // Run our common setup.
        ChronoceptionServer.init();
    }
}
