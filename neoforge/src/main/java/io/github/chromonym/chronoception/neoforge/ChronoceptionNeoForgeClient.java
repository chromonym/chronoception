package io.github.chromonym.chronoception.neoforge;

import io.github.chromonym.chronoception.Chronoception;
import io.github.chromonym.chronoception.client.ChronoceptionClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

@Mod(value = Chronoception.MOD_ID, dist = Dist.CLIENT)
public class ChronoceptionNeoForgeClient {
    public ChronoceptionNeoForgeClient() {
        // Run our common setup.
        ChronoceptionClient.init();
    }
}
