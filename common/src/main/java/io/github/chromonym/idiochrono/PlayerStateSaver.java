package io.github.chromonym.idiochrono;

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

    public HashMap<UUID, PlayerData> players = new HashMap<>();

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, WrapperLookup registryLookup) {
        NbtCompound playersNbt = new NbtCompound();
        players.forEach((uuid, playerData) -> {
            NbtCompound playerNbt = new NbtCompound();
            playerNbt.putLong("playerTimeOffset", playerData.playerTimeOffset);
            playerNbt.putLong("playerTimeStatic", playerData.playerTimeStatic);
            playersNbt.put(uuid.toString(), playerNbt);
        });
        nbt.put("players", playersNbt);
        return nbt;
    }
    
    public static PlayerStateSaver createFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        PlayerStateSaver state = new PlayerStateSaver();
        NbtCompound playersNbt = tag.getCompound("players");
        playersNbt.getKeys().forEach(key -> {
            PlayerData playerData = new PlayerData();
            playerData.playerTimeOffset = playersNbt.getCompound(key).getLong("playerTimeOffset");
            playerData.playerTimeStatic = playersNbt.getCompound(key).getLong("playerTimeStatic");
            UUID uuid = UUID.fromString(key);
            state.players.put(uuid, playerData);
        });
        return state;
    }

    private static Type<PlayerStateSaver> type = new Type<>(
        PlayerStateSaver::new, PlayerStateSaver::createFromNbt, null);
    
    public static PlayerStateSaver getServerState(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server.getWorld(World.OVERWORLD).getPersistentStateManager();
        PlayerStateSaver state = persistentStateManager.getOrCreate(type, Idiochrono.MOD_ID);
        state.markDirty();
        return state;
    }

    public static PlayerData getPlayerState(LivingEntity player) {
        PlayerStateSaver serverState = getServerState(player.getWorld().getServer());
        PlayerData playerState = serverState.players.computeIfAbsent(player.getUuid(), uuid -> new PlayerData());
        return playerState;
    }
}
