package io.github.chromonym.chronoception.client;

import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.networking.NetworkManager;
import io.github.chromonym.chronoception.Chronoception;
import io.github.chromonym.chronoception.PlayerTimeData;

public class ChronoceptionClient {
    public static PlayerTimeData playerData = new PlayerTimeData();

    public static void init() {
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, Chronoception.INITIAL_SYNC, (buf, context) -> {
            playerData.offset = buf.readLong();
            playerData.tickrate = buf.readFloat();
            playerData.counter = buf.readFloat();
            Chronoception.LOGGER.info("Initial player times - Offset: %s, Rate: %s, Counter: %s".formatted(playerData.offset, playerData.tickrate, playerData.counter));
        });
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, Chronoception.PLAYER_TIME_MODIFIED, (buf, context) -> {
            Chronoception.LOGGER.info("Client-stored times - Offset: %s, Rate: %s, Counter: %s".formatted(playerData.offset, playerData.tickrate, playerData.counter));
            playerData.offset = buf.readLong();
            playerData.tickrate = buf.readFloat();
            playerData.counter = buf.readFloat();
            Chronoception.LOGGER.info("Player times updated - Offset: %s, Rate: %s, Counter: %s".formatted(playerData.offset, playerData.tickrate, playerData.counter));
        });
        ClientTickEvent.CLIENT_PRE.register((client) -> {
            playerData.counter += playerData.tickrate;
            playerData.offset += (int)playerData.counter - 1;
            playerData.counter -= (float)((int)playerData.counter);
        });
    }
}
