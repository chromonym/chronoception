package io.github.chromonym.chronoception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

import com.mojang.brigadier.arguments.DoubleArgumentType;

import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import io.github.chromonym.chronoception.blocks.TemporalTableBlock;
import io.github.chromonym.chronoception.blocks.TimeCollisionBlock;
import io.github.chromonym.chronoception.blocks.TimeLockedBlock;
import io.github.chromonym.chronoception.blocks.TimeResetCollisionBlock;
import io.github.chromonym.chronoception.effects.TimeMultiplyEffect;
import io.github.chromonym.chronoception.effects.TimeOverrideEffect;
import io.github.chromonym.chronoception.effects.TimeResetEffect;
import io.github.chromonym.chronoception.effects.TimeSetEffect;
import io.github.chromonym.chronoception.effects.TimeSkipEffect;
import io.github.chromonym.chronoception.items.StopwatchItem;
import io.github.chromonym.chronoception.networking.PlayerTimePayload;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemGroup.Row;
import net.minecraft.potion.Potion;
import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;
import net.minecraft.world.LunarWorldView;
import net.minecraft.world.World;

import static net.minecraft.server.command.CommandManager.*;

public final class Chronoception {
    public static final String MOD_ID = "chronoception";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final BiPredicate<Long,Integer> CREPUSCULAR = (local, lunar) -> (local >= 12502L && local <= 13702L) || (local >= 22300L && local <= 23500L); // 1200-tick windows ending/starting when the sun disappears/appears on the horizon
    public static final BiPredicate<Long,Integer> DIURNAL = (local, lunar) -> (local > 23216L || local < 12786L); // solar zenith angle = 0
    public static final BiPredicate<Long,Integer> NOCTURNAL = (local, lunar) -> (local > 12786L && local < 23216L); // solar zenith angle = 0
    public static final BiPredicate<Long,Integer> FULL_MOON = (local, lunar) -> (lunar == 0);
    public static final BiPredicate<Long,Integer> GIBBOUS_MOON = (local, lunar) -> (lunar == 1 || lunar == 7);
    public static final BiPredicate<Long,Integer> QUARTER_MOON = (local, lunar) -> (lunar == 2 || lunar == 6);
    public static final BiPredicate<Long,Integer> CRESCENT_MOON = (local, lunar) -> (lunar == 3 || lunar == 5);
    public static final BiPredicate<Long,Integer> NEW_MOON = (local, lunar) -> (lunar == 4);

    //public static final Identifier INITIAL_SYNC = Identifier.of(MOD_ID, "initial_sync");
    public static final Identifier PLAYER_TIME_MODIFIED = Identifier.of(MOD_ID, "player_time_modified");
    
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(MOD_ID, RegistryKeys.ITEM);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(MOD_ID, RegistryKeys.BLOCK);
    public static final DeferredRegister<ItemGroup> ITEM_GROUPS = DeferredRegister.create(MOD_ID, RegistryKeys.ITEM_GROUP);
    public static final DeferredRegister<StatusEffect> STATUS_EFFECTS = DeferredRegister.create(MOD_ID, RegistryKeys.STATUS_EFFECT);
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(MOD_ID, RegistryKeys.POTION);

    public static final RegistrySupplier<Item> TEMPORAL_GEM = ITEMS.register("temporal_gem", () -> new Item(new Item.Settings()));
    public static final RegistrySupplier<Item> DIURNAL_GEM = ITEMS.register("diurnal_gem", () -> new Item(new Item.Settings()));
    public static final RegistrySupplier<Item> NOCTURNAL_GEM = ITEMS.register("nocturnal_gem", () -> new Item(new Item.Settings()));
    public static final RegistrySupplier<Item> CREPUSCULAR_GEM = ITEMS.register("crepuscular_gem", () -> new Item(new Item.Settings()));

    public static final RegistrySupplier<Item> TEMPORAL_DUST = ITEMS.register("temporal_dust", () -> new Item(new Item.Settings()));
    public static final RegistrySupplier<Item> NEW_MOON_DUST = ITEMS.register("new_moon_dust", () -> new Item(new Item.Settings()));
    public static final RegistrySupplier<Item> CRESCENT_DUST = ITEMS.register("crescent_moon_dust", () -> new Item(new Item.Settings()));
    public static final RegistrySupplier<Item> QUARTER_DUST = ITEMS.register("quarter_moon_dust", () -> new Item(new Item.Settings()));
    public static final RegistrySupplier<Item> GIBBOUS_DUST = ITEMS.register("gibbous_moon_dust", () -> new Item(new Item.Settings()));
    public static final RegistrySupplier<Item> FULL_MOON_DUST = ITEMS.register("full_moon_dust", () -> new Item(new Item.Settings()));

    public static final RegistrySupplier<Item> TRUE_CLOCK = ITEMS.register("true_clock", () -> new Item(new Item.Settings()));
    public static final RegistrySupplier<StopwatchItem> STOPWATCH = ITEMS.register("stopwatch", () -> new StopwatchItem(new Item.Settings(), 50.0));

    public static final RegistrySupplier<StatusEffect> TIME_SET_DAY = STATUS_EFFECTS.register("to_daytime", () -> new TimeSetEffect(1000L, 0x54BED8));
    public static final RegistrySupplier<StatusEffect> TIME_SET_NIGHT = STATUS_EFFECTS.register("to_nighttime", () -> new TimeSetEffect(13000L, 0x121851));
    public static final RegistrySupplier<StatusEffect> RESYNCHRONISATION = STATUS_EFFECTS.register("resynchronization", () -> new TimeResetEffect());
    public static final RegistrySupplier<StatusEffect> HOUR_SKIP = STATUS_EFFECTS.register("hour_skip", () -> new TimeSkipEffect(1000L, 0x681E7D));
    public static final RegistrySupplier<StatusEffect> HOUR_REVERSE = STATUS_EFFECTS.register("hour_reverse", () -> new TimeSkipEffect(-1000L, 0x8A0C6F));
    public static final RegistrySupplier<StatusEffect> DAY_SKIP = STATUS_EFFECTS.register("day_skip", () -> new TimeSkipEffect(24000L, 0xC14FEA));
    public static final RegistrySupplier<StatusEffect> DAY_REVERSE = STATUS_EFFECTS.register("day_reverse", () -> new TimeSkipEffect(-24000L, 0xD764AF));
    public static final RegistrySupplier<TimeMultiplyEffect> DOUBLE_TIME = STATUS_EFFECTS.register("double_time", () -> new TimeMultiplyEffect(2.0, 0xECC759));
    public static final RegistrySupplier<TimeMultiplyEffect> HALF_TIME = STATUS_EFFECTS.register("half_time", () -> new TimeMultiplyEffect(0.5, 0xBF922A));
    public static final RegistrySupplier<TimeMultiplyEffect> REVERSE_TIME = STATUS_EFFECTS.register("reverse_time", () -> new TimeMultiplyEffect(-1.0, 0xE0852A));
    public static final RegistrySupplier<TimeOverrideEffect> FREEZE_TIME = STATUS_EFFECTS.register("freeze_time", () -> new TimeOverrideEffect(0.0, 0x078C7D));

    public static final RegistrySupplier<Potion> TIME_SET_DAY_POTION = POTIONS.register("chronoception_to_daytime", () -> new Potion(new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry(TIME_SET_DAY.get()))));
    public static final RegistrySupplier<Potion> TIME_SET_NIGHT_POTION = POTIONS.register("chronoception_to_nighttime", () -> new Potion(new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry(TIME_SET_NIGHT.get()))));
    public static final RegistrySupplier<Potion> RESYNCHRONISATION_POTION = POTIONS.register("chronoception_resynchronization", () -> new Potion(new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry(RESYNCHRONISATION.get()))));
    public static final RegistrySupplier<Potion> RESYNCHRONISATION_II_POTION = POTIONS.register("chronoception_resynchronization_2", () -> new Potion("chronoception_resynchronization", new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry(RESYNCHRONISATION.get()), 0, 1)));
    public static final RegistrySupplier<Potion> HOUR_SKIP_POTION = POTIONS.register("chronoception_hour_skip", () -> new Potion(new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry(HOUR_SKIP.get()))));
    public static final RegistrySupplier<Potion> HOUR_SKIP_II_POTION = POTIONS.register("chronoception_hour_skip_2", () -> new Potion("chronoception_hour_skip", new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry(HOUR_SKIP.get()), 0, 1)));
    public static final RegistrySupplier<Potion> HOUR_SKIP_IV_POTION = POTIONS.register("chronoception_hour_skip_4", () -> new Potion("chronoception_hour_skip", new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry(HOUR_SKIP.get()), 0, 3)));
    public static final RegistrySupplier<Potion> HOUR_SKIP_VIII_POTION = POTIONS.register("chronoception_hour_skip_8", () -> new Potion("chronoception_hour_skip", new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry(HOUR_SKIP.get()), 0, 7)));
    public static final RegistrySupplier<Potion> HOUR_REVERSE_POTION = POTIONS.register("chronoception_hour_reverse", () -> new Potion(new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry(HOUR_REVERSE.get()))));
    public static final RegistrySupplier<Potion> HOUR_REVERSE_II_POTION = POTIONS.register("chronoception_hour_reverse_2", () -> new Potion("chronoception_hour_reverse", new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry(HOUR_REVERSE.get()), 0, 1)));
    public static final RegistrySupplier<Potion> HOUR_REVERSE_IV_POTION = POTIONS.register("chronoception_hour_reverse_4", () -> new Potion("chronoception_hour_reverse", new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry(HOUR_REVERSE.get()), 0, 3)));
    public static final RegistrySupplier<Potion> HOUR_REVERSE_VIII_POTION = POTIONS.register("chronoception_hour_reverse_8", () -> new Potion("chronoception_hour_reverse", new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry(HOUR_REVERSE.get()), 0, 7)));
    public static final RegistrySupplier<Potion> DAY_SKIP_POTION = POTIONS.register("chronoception_day_skip", () -> new Potion(new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry(DAY_SKIP.get()))));
    public static final RegistrySupplier<Potion> DAY_SKIP_II_POTION = POTIONS.register("chronoception_day_skip_2", () -> new Potion("chronoception_day_skip", new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry(DAY_SKIP.get()), 0, 1)));
    public static final RegistrySupplier<Potion> DAY_SKIP_III_POTION = POTIONS.register("chronoception_day_skip_3", () -> new Potion("chronoception_day_skip", new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry(DAY_SKIP.get()), 0, 2)));
    public static final RegistrySupplier<Potion> DAY_SKIP_IV_POTION = POTIONS.register("chronoception_day_skip_4", () -> new Potion("chronoception_day_skip", new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry(DAY_SKIP.get()), 0, 3)));
    public static final RegistrySupplier<Potion> DAY_REVERSE_POTION = POTIONS.register("chronoception_day_reverse", () -> new Potion(new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry(DAY_REVERSE.get()))));
    public static final RegistrySupplier<Potion> DAY_REVERSE_II_POTION = POTIONS.register("chronoception_day_reverse_2", () -> new Potion("chronoception_day_reverse", new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry(DAY_REVERSE.get()), 0, 1)));
    public static final RegistrySupplier<Potion> DAY_REVERSE_III_POTION = POTIONS.register("chronoception_day_reverse_3", () -> new Potion("chronoception_day_reverse", new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry(DAY_REVERSE.get()), 0, 2)));
    public static final RegistrySupplier<Potion> DAY_REVERSE_IV_POTION = POTIONS.register("chronoception_day_reverse_4", () -> new Potion("chronoception_day_reverse", new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry(DAY_REVERSE.get()), 0, 3)));

    public static final RegistrySupplier<Potion> DOUBLE_TIME_POTION = POTIONS.register("chronoception_double_time", () -> new Potion(new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry(DOUBLE_TIME.get()), 3600)));
    public static final RegistrySupplier<Potion> DOUBLE_TIME_II_POTION = POTIONS.register("chronoception_double_time_2", () -> new Potion("chronoception_double_time", new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry(DOUBLE_TIME.get()), 3600, 1)));
    public static final RegistrySupplier<Potion> DOUBLE_TIME_III_POTION = POTIONS.register("chronoception_double_time_3", () -> new Potion("chronoception_double_time", new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry(DOUBLE_TIME.get()), 3600, 2)));
    public static final RegistrySupplier<Potion> DOUBLE_TIME_IV_POTION = POTIONS.register("chronoception_double_time_4", () -> new Potion("chronoception_double_time", new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry(DOUBLE_TIME.get()), 3600, 3)));
    public static final RegistrySupplier<Potion> HALF_TIME_POTION = POTIONS.register("chronoception_half_time", () -> new Potion(new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry(HALF_TIME.get()), 3600)));
    public static final RegistrySupplier<Potion> HALF_TIME_II_POTION = POTIONS.register("chronoception_half_time_2", () -> new Potion("chronoception_half_time", new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry(HALF_TIME.get()), 3600, 1)));
    public static final RegistrySupplier<Potion> HALF_TIME_III_POTION = POTIONS.register("chronoception_half_time_3", () -> new Potion("chronoception_half_time", new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry(HALF_TIME.get()), 3600, 2)));
    public static final RegistrySupplier<Potion> HALF_TIME_IV_POTION = POTIONS.register("chronoception_half_time_4", () -> new Potion("chronoception_half_time", new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry(HALF_TIME.get()), 3600, 3)));
    public static final RegistrySupplier<Potion> REVERSE_TIME_POTION = POTIONS.register("chronoception_reverse_time", () -> new Potion(new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry(REVERSE_TIME.get()), 3600)));
    public static final RegistrySupplier<Potion> FREEZE_TIME_POTION = POTIONS.register("chronoception_freeze_time", () -> new Potion(new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry(FREEZE_TIME.get()), 3600)));

    public static final RegistrySupplier<TimeLockedBlock> CREPUSCULAR_GHOSTBLOCK = BLOCKS.register("crepuscular_ghostblock", () -> new TimeCollisionBlock(
        AbstractBlock.Settings.copy(Blocks.ORANGE_STAINED_GLASS).nonOpaque().solidBlock((var1, var2, var3) -> false).suffocates((var1, var2, var3) -> false).blockVision((var1, var2, var3) -> false),
        //.emissiveLighting((state, view, pos) -> view instanceof ClientWorld world ? CREPUSCULAR.test(world.getTimeOfDay(), world.getLunarTime()) : false),
        Blocks.ORANGE_STAINED_GLASS, CREPUSCULAR));
    public static final RegistrySupplier<BlockItem> CREPUSCULAR_GHOSTBLOCK_ITEM = ITEMS.register("crepuscular_ghostblock", () -> new BlockItem(CREPUSCULAR_GHOSTBLOCK.get(), new Item.Settings()));
    
    public static final RegistrySupplier<TimeLockedBlock> DIURNAL_GHOSTBLOCK = BLOCKS.register("diurnal_ghostblock", () -> new TimeCollisionBlock(
        AbstractBlock.Settings.copy(Blocks.LIGHT_BLUE_STAINED_GLASS).nonOpaque().solidBlock((var1, var2, var3) -> false).suffocates((var1, var2, var3) -> false).blockVision((var1, var2, var3) -> false),
        Blocks.LIGHT_BLUE_STAINED_GLASS, DIURNAL));
    public static final RegistrySupplier<BlockItem> DIURNAL_GHOSTBLOCK_ITEM = ITEMS.register("diurnal_ghostblock", () -> new BlockItem(DIURNAL_GHOSTBLOCK.get(), new Item.Settings()));

    public static final RegistrySupplier<TimeLockedBlock> NOCTURNAL_GHOSTBLOCK = BLOCKS.register("nocturnal_ghostblock", () -> new TimeCollisionBlock(
        AbstractBlock.Settings.copy(Blocks.BLUE_STAINED_GLASS).nonOpaque().solidBlock((var1, var2, var3) -> false).suffocates((var1, var2, var3) -> false).blockVision((var1, var2, var3) -> false),
        Blocks.BLUE_STAINED_GLASS, NOCTURNAL));
    public static final RegistrySupplier<BlockItem> NOCTURNAL_GHOSTBLOCK_ITEM = ITEMS.register("nocturnal_ghostblock", () -> new BlockItem(NOCTURNAL_GHOSTBLOCK.get(), new Item.Settings()));

    public static final RegistrySupplier<TimeLockedBlock> FULL_MOON_GHOSTBLOCK = BLOCKS.register("full_moon_ghostblock", () -> new TimeCollisionBlock(
        AbstractBlock.Settings.copy(Blocks.YELLOW_STAINED_GLASS).nonOpaque().solidBlock((var1, var2, var3) -> false).suffocates((var1, var2, var3) -> false).blockVision((var1, var2, var3) -> false),
        Blocks.YELLOW_STAINED_GLASS, FULL_MOON));
    public static final RegistrySupplier<BlockItem> FULL_MOON_GHOSTBLOCK_ITEM = ITEMS.register("full_moon_ghostblock", () -> new BlockItem(FULL_MOON_GHOSTBLOCK.get(), new Item.Settings()));

    public static final RegistrySupplier<TimeLockedBlock> GIBBOUS_MOON_GHOSTBLOCK = BLOCKS.register("gibbous_moon_ghostblock", () -> new TimeCollisionBlock(
        AbstractBlock.Settings.copy(Blocks.YELLOW_STAINED_GLASS).nonOpaque().solidBlock((var1, var2, var3) -> false).suffocates((var1, var2, var3) -> false).blockVision((var1, var2, var3) -> false),
        Blocks.YELLOW_STAINED_GLASS, GIBBOUS_MOON));
    public static final RegistrySupplier<BlockItem> GIBBOUS_MOON_GHOSTBLOCK_ITEM = ITEMS.register("gibbous_moon_ghostblock", () -> new BlockItem(GIBBOUS_MOON_GHOSTBLOCK.get(), new Item.Settings()));

    public static final RegistrySupplier<TimeLockedBlock> QUARTER_MOON_GHOSTBLOCK = BLOCKS.register("quarter_moon_ghostblock", () -> new TimeCollisionBlock(
        AbstractBlock.Settings.copy(Blocks.BROWN_STAINED_GLASS).nonOpaque().solidBlock((var1, var2, var3) -> false).suffocates((var1, var2, var3) -> false).blockVision((var1, var2, var3) -> false),
        Blocks.BROWN_STAINED_GLASS, QUARTER_MOON));
    public static final RegistrySupplier<BlockItem> QUARTER_MOON_GHOSTBLOCK_ITEM = ITEMS.register("quarter_moon_ghostblock", () -> new BlockItem(QUARTER_MOON_GHOSTBLOCK.get(), new Item.Settings()));

    public static final RegistrySupplier<TimeLockedBlock> CRESCENT_MOON_GHOSTBLOCK = BLOCKS.register("crescent_moon_ghostblock", () -> new TimeCollisionBlock(
        AbstractBlock.Settings.copy(Blocks.BROWN_STAINED_GLASS).nonOpaque().solidBlock((var1, var2, var3) -> false).suffocates((var1, var2, var3) -> false).blockVision((var1, var2, var3) -> false),
        Blocks.BROWN_STAINED_GLASS, CRESCENT_MOON));
    public static final RegistrySupplier<BlockItem> CRESCENT_MOON_GHOSTBLOCK_ITEM = ITEMS.register("crescent_moon_ghostblock", () -> new BlockItem(CRESCENT_MOON_GHOSTBLOCK.get(), new Item.Settings()));

    public static final RegistrySupplier<TimeLockedBlock> NEW_MOON_GHOSTBLOCK = BLOCKS.register("new_moon_ghostblock", () -> new TimeCollisionBlock(
        AbstractBlock.Settings.copy(Blocks.BROWN_STAINED_GLASS).nonOpaque().solidBlock((var1, var2, var3) -> false).suffocates((var1, var2, var3) -> false).blockVision((var1, var2, var3) -> false),
        Blocks.BROWN_STAINED_GLASS, NEW_MOON));
    public static final RegistrySupplier<BlockItem> NEW_MOON_GHOSTBLOCK_ITEM = ITEMS.register("new_moon_ghostblock", () -> new BlockItem(NEW_MOON_GHOSTBLOCK.get(), new Item.Settings()));

    public static final RegistrySupplier<Block> RESYNCHRONOUS_GHOSTBLOCK = BLOCKS.register("resynchronous_ghostblock", () -> new TimeResetCollisionBlock(
        AbstractBlock.Settings.copy(Blocks.GRAY_STAINED_GLASS).nonOpaque().solidBlock((var1, var2, var3) -> false).suffocates((var1, var2, var3) -> false).blockVision((var1, var2, var3) -> false)));
    public static final RegistrySupplier<BlockItem> RESYNCHRONOUS_GHOSTBLOCK_ITEM = ITEMS.register("resynchronous_ghostblock", () -> new BlockItem(RESYNCHRONOUS_GHOSTBLOCK.get(), new Item.Settings()));

    public static final RegistrySupplier<Block> TEMPORAL_TABLE = BLOCKS.register("temporal_table", () -> new TemporalTableBlock(AbstractBlock.Settings.copy(Blocks.QUARTZ_PILLAR)));
    public static final RegistrySupplier<BlockItem> TEMPORAL_TABLE_ITEM = ITEMS.register("temporal_table", () -> new BlockItem(TEMPORAL_TABLE.get(), new Item.Settings()));

    public static final Supplier<ItemGroup> CHRONOCEPTION_TAB = ITEM_GROUPS.register("tab", () -> ItemGroup.create(Row.TOP, 0)
        .displayName(Text.translatable("itemGroup." + MOD_ID + ".tab"))
        .icon(() -> new ItemStack(TRUE_CLOCK.get()))
        .entries((params, output) -> {
            output.add(TEMPORAL_GEM.get());
            output.add(TEMPORAL_DUST.get());
            output.add(TEMPORAL_TABLE_ITEM.get());
            output.add(DIURNAL_GEM.get());
            output.add(NOCTURNAL_GEM.get());
            output.add(CREPUSCULAR_GEM.get());
            output.add(FULL_MOON_DUST.get());
            output.add(GIBBOUS_DUST.get());
            output.add(QUARTER_DUST.get());
            output.add(CRESCENT_DUST.get());
            output.add(NEW_MOON_DUST.get());
            output.add(RESYNCHRONOUS_GHOSTBLOCK_ITEM.get());
            output.add(DIURNAL_GHOSTBLOCK_ITEM.get());
            output.add(NOCTURNAL_GHOSTBLOCK_ITEM.get());
            output.add(CREPUSCULAR_GHOSTBLOCK_ITEM.get());
            output.add(FULL_MOON_GHOSTBLOCK_ITEM.get());
            output.add(GIBBOUS_MOON_GHOSTBLOCK_ITEM.get());
            output.add(QUARTER_MOON_GHOSTBLOCK_ITEM.get());
            output.add(CRESCENT_MOON_GHOSTBLOCK_ITEM.get());
            output.add(NEW_MOON_GHOSTBLOCK_ITEM.get());
            output.add(TRUE_CLOCK.get());

            // Potions - might want to move this to its own tab?
            List<Item> potion_items = List.of(Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION, Items.TIPPED_ARROW);
            List<RegistrySupplier<Potion>> potion_effects = List.of(
                RESYNCHRONISATION_POTION, RESYNCHRONISATION_II_POTION, TIME_SET_DAY_POTION, TIME_SET_NIGHT_POTION,
                HOUR_SKIP_POTION, HOUR_SKIP_II_POTION, HOUR_SKIP_IV_POTION, HOUR_SKIP_VIII_POTION,
                HOUR_REVERSE_POTION, HOUR_REVERSE_II_POTION, HOUR_REVERSE_IV_POTION, HOUR_REVERSE_VIII_POTION,
                DAY_SKIP_POTION, DAY_SKIP_II_POTION, DAY_SKIP_III_POTION, DAY_SKIP_IV_POTION,
                DAY_REVERSE_POTION, DAY_REVERSE_II_POTION, DAY_REVERSE_III_POTION, DAY_REVERSE_IV_POTION,
                DOUBLE_TIME_POTION, DOUBLE_TIME_II_POTION, DOUBLE_TIME_III_POTION, DOUBLE_TIME_IV_POTION,
                HALF_TIME_POTION, HALF_TIME_II_POTION, HALF_TIME_III_POTION, HALF_TIME_IV_POTION,
                REVERSE_TIME_POTION, FREEZE_TIME_POTION
            );
            potion_items.forEach((item) -> {
                potion_effects.forEach((effect) -> {
                    output.add(PotionContentsComponent.createStack(item, Registries.POTION.getEntry(effect.get())));
                });
            });
        }).build());
    
    public static void init() {
        BLOCKS.register();
        ITEMS.register();
        ITEM_GROUPS.register();
        STATUS_EFFECTS.register();
        POTIONS.register();
        PlayerEvent.PLAYER_JOIN.register((player) -> {
            syncPlayerTimes(player);
        });
        CommandRegistrationEvent.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("chronoception")
            .then(literal("offset")
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player != null) {
                        PlayerTimeData playerState = PlayerStateSaver.getPlayerState(player);
                        context.getSource().sendFeedback(() -> Text.literal("Current offset: %s".formatted(playerState.offset)), false);
                        syncPlayerTimes(player);
                    } else {
                        context.getSource().sendError(Text.literal("Not a player!"));
                    }
                    return 1;
                }).then(argument("player",EntityArgumentType.player())
                    .requires(source -> source.hasPermissionLevel(2))
                    .executes(context -> {
                        ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                        if (player != null) {
                            PlayerTimeData playerState = PlayerStateSaver.getPlayerState(player);
                            context.getSource().sendFeedback(() -> Text.literal("Current offset: %s".formatted(playerState.offset)), false);
                            syncPlayerTimes(player);
                        } else {
                            context.getSource().sendError(Text.literal("Not a player!"));
                        }
                        return 1;
                    }).then(argument("offset", DoubleArgumentType.doubleArg(0.0, 192000.0))
                        .executes(context -> {
                            ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                            if (player != null) {
                                PlayerTimeData playerState = PlayerStateSaver.getPlayerState(player);
                                playerState.offset = DoubleArgumentType.getDouble(context, "offset");
                                context.getSource().sendFeedback(() -> Text.literal("Set %s's offset to %s".formatted(player.getGameProfile().getName(), playerState.offset)), true);
                                syncPlayerTimes(player);
                            } else {
                                context.getSource().sendError(Text.literal("Not a player!"));
                            }
                            return 1;
                        })
                    )
                )
            ).then(literal("tickrate")
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player != null) {
                        PlayerTimeData playerState = PlayerStateSaver.getPlayerState(player);
                        context.getSource().sendFeedback(() -> Text.literal("Current tickrate: %s".formatted(playerState.tickrate)), false);
                        syncPlayerTimes(player);
                    } else {
                        context.getSource().sendError(Text.literal("Not a player!"));
                    }
                    return 1;
                }).then(argument("player",EntityArgumentType.player())
                    .requires(source -> source.hasPermissionLevel(2))
                    .executes(context -> {
                        ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                        if (player != null) {
                            PlayerTimeData playerState = PlayerStateSaver.getPlayerState(player);
                            context.getSource().sendFeedback(() -> Text.literal("Current tickrate: %s".formatted(playerState.tickrate)), false);
                            syncPlayerTimes(player);
                        } else {
                            context.getSource().sendError(Text.literal("Not a player!"));
                        }
                        return 1;
                    }).then(argument("tickrate", DoubleArgumentType.doubleArg(-10000.0,10000.0))
                        .executes(context -> {
                            ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                            if (player != null) {
                                PlayerTimeData playerState = PlayerStateSaver.getPlayerState(player);
                                double newTR = DoubleArgumentType.getDouble(context, "tickrate");
                                playerState.tickrate = newTR;
                                playerState.baseTickrate = newTR;
                                context.getSource().sendFeedback(() -> Text.literal("Set %s's tickrate to %s".formatted(player.getGameProfile().getName(), playerState.tickrate)), true);
                                syncPlayerTimes(player);
                            } else {
                                context.getSource().sendError(Text.literal("Not a player!"));
                            }
                            return 1;
                        })
                    )
                )
            ).then(literal("baseTickrate")
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player != null) {
                        PlayerTimeData playerState = PlayerStateSaver.getPlayerState(player);
                        context.getSource().sendFeedback(() -> Text.literal("Current base tickrate: %s".formatted(playerState.baseTickrate)), false);
                        syncPlayerTimes(player);
                    } else {
                        context.getSource().sendError(Text.literal("Not a player!"));
                    }
                    return 1;
                }).then(argument("player",EntityArgumentType.player())
                    .requires(source -> source.hasPermissionLevel(2))
                    .executes(context -> {
                        ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                        if (player != null) {
                            PlayerTimeData playerState = PlayerStateSaver.getPlayerState(player);
                            context.getSource().sendFeedback(() -> Text.literal("Current base tickrate: %s".formatted(playerState.baseTickrate)), false);
                            syncPlayerTimes(player);
                        } else {
                            context.getSource().sendError(Text.literal("Not a player!"));
                        }
                        return 1;
                    })
                )
            ));
        });
        TickEvent.Server.SERVER_PRE.register((server) -> {
            if (server.getOverworld().getLevelProperties().getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE)) {
                PlayerStateSaver playerStateSaver = PlayerStateSaver.getServerState(server);
                playerStateSaver.players.forEach((uuid, playerData) -> {
                    playerData.offset += playerData.tickrate - 1.0;
                    playerData.offset %= 192000.0; // one lunar cycle
                    if (playerData.offset < 0.0) {
                        playerData.offset += 192000.0;
                    }
                    if (playerData.tickrate != 1.0 && server.getTicks() % 1200 == 0) {
                        syncPlayerTimes(server.getPlayerManager().getPlayer(uuid));
                    }
                });
            }
        });
        
    }
    public static void syncPlayerTimes(ServerPlayerEntity player) {
        if (!player.getWorld().isClient()) {
            PlayerTimeData playerState = PlayerStateSaver.getPlayerState(player);
            NetworkManager.sendToPlayer(player, new PlayerTimePayload(playerState.offset, playerState.tickrate, playerState.baseTickrate));
            /*RegistryByteBuf data = new RegistryByteBuf(Unpooled.buffer(), player.getRegistryManager());
            data.writeDouble(playerState.offset);
            data.writeDouble(playerState.tickrate);
            data.writeDouble(playerState.baseTickrate);
            NetworkManager.sendToPlayer(player, PLAYER_TIME_MODIFIED, data);*/
        }
    }
    public static long getPercievedTime(World world, PlayerEntity player) {
        if (world.isClient()) {
            return world.getTimeOfDay() % 24000L; // should be modified already
        } else {
            PlayerTimeData playerData = PlayerStateSaver.getPlayerState(player);
            return (world.getLevelProperties().getTimeOfDay() + (long)playerData.offset) % 24000L; // otherwise calc it here
        }
    }

    public static long getPercievedLunarTime(LunarWorldView world, PlayerEntity player) {
        if (world.isClient()) {
            return world.getLunarTime() % 192000L; // should be modified already
        } else {
            PlayerTimeData playerData = PlayerStateSaver.getPlayerState(player);
            return (world.getLunarTime() + (long)playerData.offset) % 192000L; // otherwise calc it here
        }
    }

    public static void registerBrewingRecipes(BrewingRecipeRegistry.Builder builder) {
        builder.registerRecipes(Chronoception.TEMPORAL_GEM.get(), Registries.POTION.getEntry(Chronoception.RESYNCHRONISATION_POTION.get()));
        builder.registerRecipes(Chronoception.TEMPORAL_DUST.get(), Registries.POTION.getEntry(Chronoception.RESYNCHRONISATION_II_POTION.get()));
        builder.registerRecipes(Chronoception.DIURNAL_GEM.get(), Registries.POTION.getEntry(Chronoception.TIME_SET_DAY_POTION.get()));
        builder.registerRecipes(Chronoception.NOCTURNAL_GEM.get(), Registries.POTION.getEntry(Chronoception.TIME_SET_NIGHT_POTION.get()));

        builder.registerRecipes(Chronoception.CRESCENT_DUST.get(), Registries.POTION.getEntry(Chronoception.HOUR_SKIP_POTION.get()));
        builder.registerPotionRecipe(Registries.POTION.getEntry(Chronoception.HOUR_SKIP_POTION.get()), Items.GLOWSTONE_DUST, Registries.POTION.getEntry(Chronoception.HOUR_SKIP_II_POTION.get()));
        builder.registerPotionRecipe(Registries.POTION.getEntry(Chronoception.HOUR_SKIP_II_POTION.get()), Items.GLOWSTONE_DUST, Registries.POTION.getEntry(Chronoception.HOUR_SKIP_IV_POTION.get()));
        builder.registerPotionRecipe(Registries.POTION.getEntry(Chronoception.HOUR_SKIP_IV_POTION.get()), Items.GLOWSTONE_DUST, Registries.POTION.getEntry(Chronoception.HOUR_SKIP_VIII_POTION.get()));
        builder.registerPotionRecipe(Registries.POTION.getEntry(Chronoception.HOUR_SKIP_POTION.get()), Items.FERMENTED_SPIDER_EYE, Registries.POTION.getEntry(Chronoception.HOUR_REVERSE_POTION.get()));
        builder.registerPotionRecipe(Registries.POTION.getEntry(Chronoception.HOUR_SKIP_II_POTION.get()), Items.FERMENTED_SPIDER_EYE, Registries.POTION.getEntry(Chronoception.HOUR_REVERSE_II_POTION.get()));
        builder.registerPotionRecipe(Registries.POTION.getEntry(Chronoception.HOUR_SKIP_IV_POTION.get()), Items.FERMENTED_SPIDER_EYE, Registries.POTION.getEntry(Chronoception.HOUR_REVERSE_IV_POTION.get()));
        builder.registerPotionRecipe(Registries.POTION.getEntry(Chronoception.HOUR_SKIP_VIII_POTION.get()), Items.FERMENTED_SPIDER_EYE, Registries.POTION.getEntry(Chronoception.HOUR_REVERSE_VIII_POTION.get()));
        builder.registerPotionRecipe(Registries.POTION.getEntry(Chronoception.HOUR_REVERSE_POTION.get()), Items.GLOWSTONE_DUST, Registries.POTION.getEntry(Chronoception.HOUR_REVERSE_II_POTION.get()));
        builder.registerPotionRecipe(Registries.POTION.getEntry(Chronoception.HOUR_REVERSE_II_POTION.get()), Items.GLOWSTONE_DUST, Registries.POTION.getEntry(Chronoception.HOUR_REVERSE_IV_POTION.get()));
        builder.registerPotionRecipe(Registries.POTION.getEntry(Chronoception.HOUR_REVERSE_IV_POTION.get()), Items.GLOWSTONE_DUST, Registries.POTION.getEntry(Chronoception.HOUR_REVERSE_VIII_POTION.get()));

        builder.registerRecipes(Chronoception.GIBBOUS_DUST.get(), Registries.POTION.getEntry(Chronoception.DAY_SKIP_POTION.get()));
        builder.registerPotionRecipe(Registries.POTION.getEntry(Chronoception.DAY_SKIP_POTION.get()), Items.GLOWSTONE_DUST, Registries.POTION.getEntry(Chronoception.DAY_SKIP_II_POTION.get()));
        builder.registerPotionRecipe(Registries.POTION.getEntry(Chronoception.DAY_SKIP_II_POTION.get()), Items.GLOWSTONE_DUST, Registries.POTION.getEntry(Chronoception.DAY_SKIP_III_POTION.get()));
        builder.registerPotionRecipe(Registries.POTION.getEntry(Chronoception.DAY_SKIP_III_POTION.get()), Items.GLOWSTONE_DUST, Registries.POTION.getEntry(Chronoception.DAY_SKIP_IV_POTION.get()));
        builder.registerPotionRecipe(Registries.POTION.getEntry(Chronoception.DAY_SKIP_POTION.get()), Items.FERMENTED_SPIDER_EYE, Registries.POTION.getEntry(Chronoception.DAY_REVERSE_POTION.get()));
        builder.registerPotionRecipe(Registries.POTION.getEntry(Chronoception.DAY_SKIP_II_POTION.get()), Items.FERMENTED_SPIDER_EYE, Registries.POTION.getEntry(Chronoception.DAY_REVERSE_II_POTION.get()));
        builder.registerPotionRecipe(Registries.POTION.getEntry(Chronoception.DAY_SKIP_III_POTION.get()), Items.FERMENTED_SPIDER_EYE, Registries.POTION.getEntry(Chronoception.DAY_REVERSE_III_POTION.get()));
        builder.registerPotionRecipe(Registries.POTION.getEntry(Chronoception.DAY_SKIP_IV_POTION.get()), Items.FERMENTED_SPIDER_EYE, Registries.POTION.getEntry(Chronoception.DAY_REVERSE_IV_POTION.get()));
        builder.registerPotionRecipe(Registries.POTION.getEntry(Chronoception.DAY_REVERSE_POTION.get()), Items.GLOWSTONE_DUST, Registries.POTION.getEntry(Chronoception.DAY_REVERSE_II_POTION.get()));
        builder.registerPotionRecipe(Registries.POTION.getEntry(Chronoception.DAY_REVERSE_II_POTION.get()), Items.GLOWSTONE_DUST, Registries.POTION.getEntry(Chronoception.DAY_REVERSE_III_POTION.get()));
        builder.registerPotionRecipe(Registries.POTION.getEntry(Chronoception.DAY_REVERSE_III_POTION.get()), Items.GLOWSTONE_DUST, Registries.POTION.getEntry(Chronoception.DAY_REVERSE_IV_POTION.get()));

        builder.registerRecipes(Chronoception.FULL_MOON_DUST.get(), Registries.POTION.getEntry(Chronoception.DOUBLE_TIME_POTION.get()));
        builder.registerPotionRecipe(Registries.POTION.getEntry(Chronoception.DOUBLE_TIME_POTION.get()), Items.GLOWSTONE_DUST, Registries.POTION.getEntry(Chronoception.DOUBLE_TIME_II_POTION.get()));
        builder.registerPotionRecipe(Registries.POTION.getEntry(Chronoception.DOUBLE_TIME_II_POTION.get()), Items.GLOWSTONE_DUST, Registries.POTION.getEntry(Chronoception.DOUBLE_TIME_III_POTION.get()));
        builder.registerPotionRecipe(Registries.POTION.getEntry(Chronoception.DOUBLE_TIME_III_POTION.get()), Items.GLOWSTONE_DUST, Registries.POTION.getEntry(Chronoception.DOUBLE_TIME_IV_POTION.get()));

        builder.registerRecipes(Chronoception.QUARTER_DUST.get(), Registries.POTION.getEntry(Chronoception.HALF_TIME_POTION.get()));
        builder.registerPotionRecipe(Registries.POTION.getEntry(Chronoception.HALF_TIME_POTION.get()), Items.GLOWSTONE_DUST, Registries.POTION.getEntry(Chronoception.HALF_TIME_II_POTION.get()));
        builder.registerPotionRecipe(Registries.POTION.getEntry(Chronoception.HALF_TIME_II_POTION.get()), Items.GLOWSTONE_DUST, Registries.POTION.getEntry(Chronoception.HALF_TIME_III_POTION.get()));
        builder.registerPotionRecipe(Registries.POTION.getEntry(Chronoception.HALF_TIME_III_POTION.get()), Items.GLOWSTONE_DUST, Registries.POTION.getEntry(Chronoception.HALF_TIME_IV_POTION.get()));

        builder.registerRecipes(Chronoception.CREPUSCULAR_GEM.get(), Registries.POTION.getEntry(Chronoception.REVERSE_TIME_POTION.get()));
        builder.registerRecipes(Chronoception.NEW_MOON_DUST.get(), Registries.POTION.getEntry(Chronoception.FREEZE_TIME_POTION.get()));
    }
}