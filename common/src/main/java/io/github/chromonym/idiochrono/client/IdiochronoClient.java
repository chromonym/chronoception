package io.github.chromonym.idiochrono.client;

import dev.architectury.networking.NetworkManager;
import io.github.chromonym.idiochrono.Idiochrono;
import io.github.chromonym.idiochrono.PlayerData;
import net.minecraft.entity.player.PlayerEntity;

public class IdiochronoClient {
    public static PlayerData playerData = new PlayerData();

    public static void init() {
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, Idiochrono.INITIAL_SYNC, (buf, context) -> {
            PlayerEntity player = context.getPlayer();
            playerData.playerTimeOffset = buf.readLong();
            playerData.playerTimeStatic = buf.readLong();
            Idiochrono.LOGGER.info(Long.toString(playerData.playerTimeOffset));
            Idiochrono.LOGGER.info(Long.toString(playerData.playerTimeStatic));
        });
    }
}
