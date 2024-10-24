package io.github.chromonym.chronoception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.UUID;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;

import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import static net.minecraft.server.command.CommandManager.*;

public final class Chronoception {
    public static final String MOD_ID = "chronoception";

    public static final Identifier INITIAL_SYNC = Identifier.of(MOD_ID, "initial_sync");
    public static final Identifier PLAYER_TIME_MODIFIED = Identifier.of(MOD_ID, "player_time_modified");

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static void init() {
        PlayerEvent.PLAYER_JOIN.register((player) -> {
            syncPlayerTimes(player, true);
        });
        CommandRegistrationEvent.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("chronoception")
                .then(argument("player", EntityArgumentType.player())
                    .then(argument("rate", BoolArgumentType.bool())
                        .executes(context -> {
                            PlayerTimeData playerState = PlayerStateSaver.getPlayerState(EntityArgumentType.getPlayer(context, "player"));
                            if (BoolArgumentType.getBool(context, "rate")) {
                                float playerTime = playerState.tickrate;
                                context.getSource().sendFeedback(() -> Text.literal("Tick rate: %s".formatted(playerTime)), false);
                            } else {
                                long playerTime = playerState.offset;
                                context.getSource().sendFeedback(() -> Text.literal("Offset Time: %s".formatted(playerTime)), false);
                            }
                            syncPlayerTimes(EntityArgumentType.getPlayer(context, "player"), false);
                            return 1;
                        })
                        .then(argument("new_value",FloatArgumentType.floatArg())
                            .requires(source -> source.hasPermissionLevel(2))
                            .executes(context -> {
                                ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                PlayerTimeData playerState = PlayerStateSaver.getPlayerState(player);
                                if (BoolArgumentType.getBool(context, "rate")) {
                                    playerState.tickrate = FloatArgumentType.getFloat(context, "new_value");
                                    context.getSource().sendFeedback(() -> Text.literal("Updated tick rate to %s".formatted(playerState.tickrate)), true);
                                } else {
                                    playerState.offset = (long)FloatArgumentType.getFloat(context, "new_value");
                                    context.getSource().sendFeedback(() -> Text.literal("Updated time offset to %s".formatted(playerState.offset)), true);
                                }
                                syncPlayerTimes(player, false);
                                return 1;
                            })
                        )
                    )
                )
            );
        });
        TickEvent.SERVER_PRE.register((server) -> {
            //server.getPlayerManager().getPlayers().forEach((player) -> {
            //    PlayerTimeData playerState = PlayerStateSaver.getPlayerState(player);
            //    playerState.counter += playerState.tickrate;
            //    playerState.offset += (int)playerState.counter - 1;
            //    playerState.counter -= (float)((int)playerState.counter);
            //});
            PlayerStateSaver playerStateSaver = PlayerStateSaver.getServerState(server);
            playerStateSaver.players.forEach((uuid, playerData) -> {
                playerData.counter += playerData.tickrate;
                playerData.offset += (int)playerData.counter - 1;
                playerData.counter -= (float)((int)playerData.counter);
            });
        });
    }
    public static void syncPlayerTimes(ServerPlayerEntity player, boolean instant) {
        PlayerTimeData playerState = PlayerStateSaver.getPlayerState(player);
        RegistryByteBuf data = new RegistryByteBuf(Unpooled.buffer(), player.getRegistryManager());
        data.writeLong(playerState.offset);
        data.writeFloat(playerState.tickrate);
        data.writeFloat(playerState.counter);
        if (instant) {
            NetworkManager.sendToPlayer(player, INITIAL_SYNC, data);
        } else {
            NetworkManager.sendToPlayer(player, PLAYER_TIME_MODIFIED, data);
        }
    }
}