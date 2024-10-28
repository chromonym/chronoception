package io.github.chromonym.chronoception.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.chromonym.chronoception.Chronoception;
import io.github.chromonym.chronoception.PlayerStateSaver;
import io.github.chromonym.chronoception.PlayerTimeData;
import io.github.chromonym.chronoception.effects.TimeMultiplyEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(PlayerEntity.class)
public abstract class PlayerStatusEffectMixin extends PlayerStatusEffectMixinParent {

    @Override
    public void mixinOnStatusEffectApplied(StatusEffectInstance effect, @Nullable Entity source, CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity)((Object)this);
        if (effect.getEffectType().value() instanceof TimeMultiplyEffect tmEffect && player instanceof ServerPlayerEntity serverPlayer) {
            PlayerTimeData data = PlayerStateSaver.getPlayerState(serverPlayer);
            if (data.tickrate == data.baseTickrate) {
                data.tickrate *= tmEffect.getMultiplier(effect.getAmplifier());
            }
            data.baseTickrate *= tmEffect.getMultiplier(effect.getAmplifier());
            Chronoception.syncPlayerTimes(serverPlayer);
        }
    }

    @Override
    public void mixinOnStatusEffectRemoved(StatusEffectInstance effect, CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity)((Object)this);
        if (effect.getEffectType().value() instanceof TimeMultiplyEffect tmEffect && player instanceof ServerPlayerEntity serverPlayer) {
            PlayerTimeData data = PlayerStateSaver.getPlayerState(serverPlayer);
            if (data.tickrate == data.baseTickrate) {
                data.tickrate /= tmEffect.getMultiplier(effect.getAmplifier());
            }
            data.baseTickrate /= tmEffect.getMultiplier(effect.getAmplifier());
            Chronoception.syncPlayerTimes(serverPlayer);
        }
    }

    @Override
    public void addStatusEffect(StatusEffectInstance effect, @Nullable Entity source, CallbackInfoReturnable<Boolean> ci) {
        PlayerEntity player = (PlayerEntity)((Object)this);
        if (effect.getEffectType().value() instanceof TimeMultiplyEffect tmEffect && player.hasStatusEffect(effect.getEffectType())) {
            StatusEffectInstance oldEffect = player.getStatusEffect(effect.getEffectType());
            //player.removeStatusEffect(effect.getEffectType()); // do the reverse calcs first
            if (oldEffect.getAmplifier() < effect.getAmplifier() && player instanceof ServerPlayerEntity serverPlayer) {
                PlayerTimeData data = PlayerStateSaver.getPlayerState(serverPlayer);
                if (data.tickrate == data.baseTickrate) {
                    data.tickrate /= tmEffect.getMultiplier(oldEffect.getAmplifier());
                    data.tickrate *= tmEffect.getMultiplier(effect.getAmplifier());
                }
                data.baseTickrate /= tmEffect.getMultiplier(oldEffect.getAmplifier());
                data.baseTickrate *= tmEffect.getMultiplier(effect.getAmplifier());
                Chronoception.syncPlayerTimes(serverPlayer);
            }
        }
    }
}
