package io.github.chromonym.chronoception;

import dev.architectury.networking.NetworkManager;

public class ChronoceptionServer {
    public static void init() {
        NetworkManager.registerS2CPayloadType(Chronoception.INITIAL_SYNC);
        NetworkManager.registerS2CPayloadType(Chronoception.PLAYER_TIME_MODIFIED); // hopefully this works???????
    }
}
