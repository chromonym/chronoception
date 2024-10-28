package io.github.chromonym.chronoception.networking;

import io.github.chromonym.chronoception.Chronoception;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record PlayerTimePayload(double offset, double tickrate, double baseTickrate) implements CustomPayload {

    public static final CustomPayload.Id<PlayerTimePayload> ID = new CustomPayload.Id<>(Chronoception.PLAYER_TIME_MODIFIED);
    public static final PacketCodec<RegistryByteBuf, PlayerTimePayload> CODEC = PacketCodec.tuple(PacketCodecs.DOUBLE, PlayerTimePayload::offset, PacketCodecs.DOUBLE, PlayerTimePayload::tickrate, PacketCodecs.DOUBLE, PlayerTimePayload::baseTickrate, PlayerTimePayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
    
}
