package io.github.chromonym.idiochrono;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;

import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import static net.minecraft.server.command.CommandManager.*;

public final class Idiochrono {
    public static final String MOD_ID = "idiochrono";

    public static final Identifier INITIAL_SYNC = Identifier.of(MOD_ID, "initial_sync");

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static void init() {
        /*
         * NetworkManager.registerReceiver(NetworkManager.Side.S2C, INITIAL_SYNC, (buf, context) -> {
            PlayerEntity player = context.getPlayer();
            long playerTimeOffset = buf.readLong();
            long playerTimeStatic = buf.readLong();
        });
         */
        PlayerEvent.PLAYER_JOIN.register((player) -> {
            PlayerData playerState = PlayerStateSaver.getPlayerState(player);
            RegistryByteBuf data = new RegistryByteBuf(Unpooled.buffer(), player.getRegistryManager());
            data.writeLong(playerState.playerTimeOffset);
            data.writeLong(playerState.playerTimeStatic);
            NetworkManager.sendToPlayer(player, INITIAL_SYNC, data);
            // TODO set up client side logic for this
        });
        CommandRegistrationEvent.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("idiochrono")
                .then(argument("player", EntityArgumentType.player())
                    .then(argument("static", BoolArgumentType.bool())
                        .executes(context -> {
                            long playerTime;
                            PlayerData playerState = PlayerStateSaver.getPlayerState(EntityArgumentType.getPlayer(context, "player"));
                            if (BoolArgumentType.getBool(context, "static")) {
                                playerTime = playerState.playerTimeStatic;
                                context.getSource().sendFeedback(() -> Text.literal("Static Time: %s".formatted(playerTime)), false);
                            } else {
                                playerTime = playerState.playerTimeOffset;
                                context.getSource().sendFeedback(() -> Text.literal("Offset Time: %s".formatted(playerTime)), false);
                            }
                            return (int)playerTime;
                        })
                        .then(argument("new_value",LongArgumentType.longArg())
                            .requires(source -> source.hasPermissionLevel(2))
                            .executes(context -> {
                                ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                PlayerData playerState = PlayerStateSaver.getPlayerState(player);
                                if (BoolArgumentType.getBool(context, "static")) {
                                    playerState.playerTimeStatic = LongArgumentType.getLong(context, "new_value");
                                    context.getSource().sendFeedback(() -> Text.literal("Updated static time to %s".formatted(playerState.playerTimeStatic)), true);
                                } else {
                                    playerState.playerTimeOffset = LongArgumentType.getLong(context, "new_value");
                                    context.getSource().sendFeedback(() -> Text.literal("Updated time offset to %s".formatted(playerState.playerTimeOffset)), true);
                                }
                                RegistryByteBuf data = new RegistryByteBuf(Unpooled.buffer(), player.getRegistryManager());
                                data.writeLong(playerState.playerTimeOffset);
                                data.writeLong(playerState.playerTimeStatic);
                                NetworkManager.sendToPlayer(player, INITIAL_SYNC, data);
                                return 1;
                            })
                        )
                    )
                )
            );
        });
    }
}