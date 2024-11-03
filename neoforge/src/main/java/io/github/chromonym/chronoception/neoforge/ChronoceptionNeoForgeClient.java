package io.github.chromonym.chronoception.neoforge;

import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.registry.item.ItemPropertiesRegistry;
import io.github.chromonym.chronoception.Chronoception;
import io.github.chromonym.chronoception.client.ChronoceptionClient;
import io.github.chromonym.chronoception.client.screens.TemporalTableScreen;
import net.minecraft.util.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@Mod(value = Chronoception.MOD_ID, dist = Dist.CLIENT)
public class ChronoceptionNeoForgeClient {
    public ChronoceptionNeoForgeClient(IEventBus modBus) {
        // Run our common setup.
        modBus.addListener(this::registerScreens);
        ChronoceptionClient.init();
        ClientLifecycleEvent.CLIENT_SETUP.register((client) -> {
            ItemPropertiesRegistry.register(Chronoception.TRUE_CLOCK.get(), Identifier.ofVanilla("server_time"), ChronoceptionClient.trueClockProvider);
            ItemPropertiesRegistry.register(Chronoception.STOPWATCH.get(), Identifier.ofVanilla("time"), ChronoceptionClient.stopwatchProvider);
            //MenuRegistry.registerScreenFactory(Chronoception.TEMPORAL_TABLE_SCREEN_HANDLER.get(), TemporalTableScreen::new);
        });
    }

    private void registerScreens(RegisterMenuScreensEvent event) {
        event.register(Chronoception.TEMPORAL_TABLE_SCREEN_HANDLER.get(), TemporalTableScreen::new);
    }
}
