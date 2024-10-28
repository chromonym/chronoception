package io.github.chromonym.chronoception;

import dev.architectury.networking.NetworkManager;
import io.github.chromonym.chronoception.networking.PlayerTimePayload;

public class ChronoceptionServer {
    public static void init() {
        NetworkManager.registerS2CPayloadType(PlayerTimePayload.ID, PlayerTimePayload.CODEC);
    }
}
