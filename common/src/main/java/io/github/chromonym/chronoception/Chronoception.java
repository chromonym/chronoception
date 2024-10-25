package io.github.chromonym.chronoception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;

import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import io.github.chromonym.chronoception.blocks.TimeLockedBlock;
import io.netty.buffer.Unpooled;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemGroup.Row;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import static net.minecraft.server.command.CommandManager.*;

public final class Chronoception {
    public static final String MOD_ID = "chronoception";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Identifier INITIAL_SYNC = Identifier.of(MOD_ID, "initial_sync");
    public static final Identifier PLAYER_TIME_MODIFIED = Identifier.of(MOD_ID, "player_time_modified");
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(MOD_ID, RegistryKeys.ITEM);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(MOD_ID, RegistryKeys.BLOCK);
    public static final DeferredRegister<ItemGroup> ITEM_GROUPS = DeferredRegister.create(MOD_ID, RegistryKeys.ITEM_GROUP);

    public static final RegistrySupplier<Item> DIURNAL_GEM = ITEMS.register("diurnal_gem", () -> new Item(new Item.Settings()));
    public static final RegistrySupplier<Item> NOCTURNAL_GEM = ITEMS.register("nocturnal_gem", () -> new Item(new Item.Settings()));
    public static final RegistrySupplier<Item> CREPUSCULAR_GEM = ITEMS.register("crepuscular_gem", () -> new Item(new Item.Settings()));
    public static final RegistrySupplier<Item> TRUE_CLOCK = ITEMS.register("true_clock", () -> new Item(new Item.Settings()));
    public static final RegistrySupplier<TimeLockedBlock> CREPUSCULAR_GHOSTBLOCK = BLOCKS.register("crepuscular_ghostblock", () -> new TimeLockedBlock(AbstractBlock.Settings.copy(Blocks.RED_WOOL).nonOpaque().solidBlock((var1, var2, var3) -> false).suffocates((var1, var2, var3) -> false).blockVision((var1, var2, var3) -> false)));
    public static final RegistrySupplier<BlockItem> CREPUSCULAR_GHOSTBLOCK_ITEM = ITEMS.register("crepuscular_ghostblock", () -> new BlockItem(CREPUSCULAR_GHOSTBLOCK.get(), new Item.Settings()));

    public static final Supplier<ItemGroup> CHRONOCEPTION_TAB = ITEM_GROUPS.register("tab", () -> ItemGroup.create(Row.TOP, 0)
        .displayName(Text.translatable("itemGroup." + MOD_ID + ".tab"))
        .icon(() -> new ItemStack(TRUE_CLOCK.get()))
        .entries((params, output) -> {
            output.add(DIURNAL_GEM.get());
            output.add(NOCTURNAL_GEM.get());
            output.add(CREPUSCULAR_GEM.get());
            output.add(TRUE_CLOCK.get());
            output.add(CREPUSCULAR_GHOSTBLOCK_ITEM.get());
        }).build());
    
    public static void init() {
        BLOCKS.register();
        ITEMS.register();
        ITEM_GROUPS.register();
        PlayerEvent.PLAYER_JOIN.register((player) -> {
            syncPlayerTimes(player, true);
        });
        CommandRegistrationEvent.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("chronoception")
                .then(argument("player", EntityArgumentType.player())
                    .then(argument("rate", BoolArgumentType.bool())
                        .executes(context -> {
                            PlayerTimeData playerState = PlayerStateSaver.getPlayerState(EntityArgumentType.getPlayer(context, "player"));
                            double playerTime;
                            if (BoolArgumentType.getBool(context, "rate")) {
                                playerTime = playerState.tickrate;
                                context.getSource().sendFeedback(() -> Text.literal("Tick rate: %s".formatted(playerTime)), false);
                            } else {
                                playerTime = playerState.offset;
                                context.getSource().sendFeedback(() -> Text.literal("Offset Time: %s".formatted(playerTime)), false);
                            }
                            syncPlayerTimes(EntityArgumentType.getPlayer(context, "player"), false);
                            return 1;
                        })
                        .then(argument("new_value",DoubleArgumentType.doubleArg())
                            .requires(source -> source.hasPermissionLevel(2))
                            .executes(context -> {
                                ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                PlayerTimeData playerState = PlayerStateSaver.getPlayerState(player);
                                if (BoolArgumentType.getBool(context, "rate")) {
                                    playerState.tickrate = DoubleArgumentType.getDouble(context, "new_value");
                                    context.getSource().sendFeedback(() -> Text.literal("Updated tick rate to %s".formatted(playerState.tickrate)), true);
                                } else {
                                    playerState.offset = DoubleArgumentType.getDouble(context, "new_value");
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
        TickEvent.Server.SERVER_PRE.register((server) -> {
            PlayerStateSaver playerStateSaver = PlayerStateSaver.getServerState(server);
            playerStateSaver.players.forEach((uuid, playerData) -> {
                playerData.offset += playerData.tickrate - 1.0;
                playerData.offset %= 192000.0; // one lunar cycle
            });
        });
    }
    public static void syncPlayerTimes(ServerPlayerEntity player, boolean instant) {
        PlayerTimeData playerState = PlayerStateSaver.getPlayerState(player);
        RegistryByteBuf data = new RegistryByteBuf(Unpooled.buffer(), player.getRegistryManager());
        data.writeDouble(playerState.offset);
        data.writeDouble(playerState.tickrate);
        data.writeDouble(playerState.baseTickrate);
        if (instant) {
            NetworkManager.sendToPlayer(player, INITIAL_SYNC, data);
        } else {
            NetworkManager.sendToPlayer(player, PLAYER_TIME_MODIFIED, data);
        }
    }
    public static long getPercievedTime(World world, PlayerEntity player) {
        if (world instanceof ClientWorld clientWorld) {
            return clientWorld.getTimeOfDay() % 24000L; // should be modified already
        } else {
            PlayerTimeData playerData = PlayerStateSaver.getPlayerState(player);
            return (world.getTimeOfDay() + (long)playerData.offset) % 24000L; // otherwise calc it here
        }
    }
}