package io.github.chromonym.chronoception;

import java.util.HashMap;
import java.util.UUID;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

public class PlayerStateSaver extends PersistentState {

    public HashMap<UUID, PlayerTimeData> players = new HashMap<>();

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, WrapperLookup registryLookup) {
        NbtCompound playersNbt = new NbtCompound();
        players.forEach((uuid, playerData) -> {
            NbtCompound playerNbt = new NbtCompound();
            playerNbt.putDouble("playerTimeOffset", playerData.offset);
            playerNbt.putDouble("playerTickrate", playerData.tickrate);
            playerNbt.putDouble("playerBaseTickrate", playerData.baseTickrate);
            playersNbt.put(uuid.toString(), playerNbt);
        });
        nbt.put("players", playersNbt);
        return nbt;
    }
    
    public static PlayerStateSaver createFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        PlayerStateSaver state = new PlayerStateSaver();
        NbtCompound playersNbt = tag.getCompound("players");
        playersNbt.getKeys().forEach(key -> {
            PlayerTimeData playerData = new PlayerTimeData();
            playerData.offset = playersNbt.getCompound(key).getDouble("playerTimeOffset");
            playerData.tickrate = playersNbt.getCompound(key).getDouble("playerTickrate");
            playerData.baseTickrate = playersNbt.getCompound(key).getDouble("playerBaseTickrate");
            UUID uuid = UUID.fromString(key);
            state.players.put(uuid, playerData);
        });
        return state;
    }

    private static Type<PlayerStateSaver> type = new Type<>(
        PlayerStateSaver::new, PlayerStateSaver::createFromNbt, null);
    
    public static PlayerStateSaver getServerState(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server.getWorld(World.OVERWORLD).getPersistentStateManager();
        PlayerStateSaver state = persistentStateManager.getOrCreate(type, Chronoception.MOD_ID);
        state.markDirty();
        return state;
    }

    public static PlayerTimeData getPlayerState(LivingEntity player) {
        PlayerStateSaver serverState = getServerState(player.getWorld().getServer());
        PlayerTimeData playerState = serverState.players.computeIfAbsent(player.getUuid(), uuid -> new PlayerTimeData());
        return playerState;
    }
}
