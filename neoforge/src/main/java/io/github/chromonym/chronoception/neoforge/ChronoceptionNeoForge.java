package io.github.chromonym.chronoception.neoforge;

import io.github.chromonym.chronoception.Chronoception;
import net.neoforged.fml.common.Mod;

@Mod(Chronoception.MOD_ID)
public final class ChronoceptionNeoForge {
    public ChronoceptionNeoForge() {
        // Run our common setup.
        Chronoception.init();
    }
}
