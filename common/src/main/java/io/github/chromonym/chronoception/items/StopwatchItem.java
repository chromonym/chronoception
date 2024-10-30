package io.github.chromonym.chronoception.items;

import java.util.List;

import io.github.chromonym.chronoception.Chronoception;
import io.github.chromonym.chronoception.PlayerStateSaver;
import io.github.chromonym.chronoception.PlayerTimeData;
import io.github.chromonym.chronoception.effects.TimeOverrideEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class StopwatchItem extends Item {

    private final double overrideTime;

    public StopwatchItem(Settings settings, double overrideTime) {
        super(settings);
        this.overrideTime = overrideTime;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable(this.getTranslationKey().concat(".tooltip")).formatted(Formatting.GRAY));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        user.setCurrentHand(hand);
        if (user instanceof ServerPlayerEntity player) {
            PlayerTimeData data = PlayerStateSaver.getPlayerState(player);
            if (player.isSneaking()) {
                data.tickrate = -overrideTime;
            } else {
                data.tickrate = overrideTime;
            }
            Chronoception.syncPlayerTimes(player);
        }
        return TypedActionResult.consume(itemStack);
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return Integer.MAX_VALUE;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (user instanceof ServerPlayerEntity player) {
            PlayerTimeData data = PlayerStateSaver.getPlayerState(player);
            boolean hasOverrideEffect = false;
            double overrideTime = 1.0;
            for (StatusEffectInstance effectInstance : player.getStatusEffects()) {
                if (effectInstance.getEffectType().value() instanceof TimeOverrideEffect toEffect) {
                    if (hasOverrideEffect) {
                        Chronoception.LOGGER.warn("Player has more than one tickrate override effect!");
                    }
                    hasOverrideEffect = true;
                    overrideTime = toEffect.getOverride();
                }
            }
            if (hasOverrideEffect) {
                data.tickrate = overrideTime;
            } else {
                data.tickrate = data.baseTickrate;
            }
            Chronoception.syncPlayerTimes(player);
        }
        return super.finishUsing(stack, world, user);
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        this.finishUsing(stack, world, user);
    }
    
}
