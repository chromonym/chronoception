package io.github.chromonym.idiochrono.client;

import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.networking.NetworkManager;
import io.github.chromonym.idiochrono.Idiochrono;
import io.github.chromonym.idiochrono.PlayerTimeData;

public class IdiochronoClient {
    public static PlayerTimeData playerData = new PlayerTimeData();

    public static void init() {
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, Idiochrono.INITIAL_SYNC, (buf, context) -> {
            playerData.offset = buf.readLong();
            playerData.tickrate = buf.readFloat();
            playerData.counter = buf.readFloat();
            Idiochrono.LOGGER.info("Initial player times - Offset: %s, Rate: %s, Counter: %s".formatted(playerData.offset, playerData.tickrate, playerData.counter));
        });
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, Idiochrono.PLAYER_TIME_MODIFIED, (buf, context) -> {
            Idiochrono.LOGGER.info("Client-stored times - Offset: %s, Rate: %s, Counter: %s".formatted(playerData.offset, playerData.tickrate, playerData.counter));
            playerData.offset = buf.readLong();
            playerData.tickrate = buf.readFloat();
            playerData.counter = buf.readFloat();
            Idiochrono.LOGGER.info("Player times updated - Offset: %s, Rate: %s, Counter: %s".formatted(playerData.offset, playerData.tickrate, playerData.counter));
        });
        ClientTickEvent.CLIENT_PRE.register((client) -> {
            playerData.counter += playerData.tickrate;
            playerData.offset += (int)playerData.counter - 1;
            playerData.counter -= (float)((int)playerData.counter);
        });
    }
}
