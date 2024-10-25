package io.github.chromonym.chronoception.neoforge;

import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.registry.item.ItemPropertiesRegistry;
import io.github.chromonym.chronoception.Chronoception;
import io.github.chromonym.chronoception.client.ChronoceptionClient;
import net.minecraft.util.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

@Mod(value = Chronoception.MOD_ID, dist = Dist.CLIENT)
public class ChronoceptionNeoForgeClient {
    public ChronoceptionNeoForgeClient() {
        // Run our common setup.
        ChronoceptionClient.init();
        ClientLifecycleEvent.CLIENT_SETUP.register((client) -> {
            ItemPropertiesRegistry.register(Chronoception.TRUE_CLOCK.get(), Identifier.ofVanilla("server_time"), ChronoceptionClient.trueClockProvider);
        });
    }
}
