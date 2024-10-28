package io.github.chromonym.chronoception.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.chromonym.chronoception.Chronoception;
import io.github.chromonym.chronoception.PlayerStateSaver;
import io.github.chromonym.chronoception.PlayerTimeData;
import io.github.chromonym.chronoception.effects.TimeMultiplyEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(StatusEffectInstance.class)
public abstract class StatusEffectInstanceMixin {

    @Shadow
    private StatusEffectInstance hiddenEffect;

    @Shadow
    public abstract RegistryEntry<StatusEffect> getEffectType();

    @Shadow
    public abstract int getAmplifier();

    @Inject(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/effect/StatusEffectInstance;copyFrom(Lnet/minecraft/entity/effect/StatusEffectInstance;)V"))
    public void accountForTheHiddenEffect(LivingEntity entity, Runnable overwriteCallback, CallbackInfoReturnable<Boolean> ci) {
        //StatusEffectInstance thisEffect = (StatusEffectInstance)(Object)this;
        if (entity instanceof ServerPlayerEntity player && this.getEffectType().value() instanceof TimeMultiplyEffect tmEffect) {
            PlayerTimeData data = PlayerStateSaver.getPlayerState(player);
            if (data.tickrate == data.baseTickrate) {
                data.tickrate /= tmEffect.getMultiplier(this.getAmplifier());
            }
            data.baseTickrate /= tmEffect.getMultiplier(this.getAmplifier());
            if (this.hiddenEffect != null) {
                if (this.hiddenEffect.getEffectType().value() instanceof TimeMultiplyEffect hiddenTmEffect) {
                    if (data.tickrate == data.baseTickrate) {
                        data.tickrate *= hiddenTmEffect.getMultiplier(this.hiddenEffect.getAmplifier());
                    }
                    data.baseTickrate *= hiddenTmEffect.getMultiplier(this.hiddenEffect.getAmplifier());
                }
            }
            Chronoception.syncPlayerTimes(player);
        }
    }
}
