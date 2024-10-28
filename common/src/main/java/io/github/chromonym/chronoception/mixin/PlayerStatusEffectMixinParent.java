package io.github.chromonym.chronoception.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;

@Mixin(LivingEntity.class)
public abstract class PlayerStatusEffectMixinParent {
    @Inject(method = "onStatusEffectApplied", at = @At("HEAD"))
    public void mixinOnStatusEffectApplied(StatusEffectInstance effect, @Nullable Entity source, CallbackInfo ci) {}

    @Inject(method = "onStatusEffectRemoved", at = @At("HEAD"))
    public void mixinOnStatusEffectRemoved(StatusEffectInstance effect, CallbackInfo ci) {}

    @Inject(method = "addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;Lnet/minecraft/entity/Entity;)Z", at = @At("HEAD"))
    public void addStatusEffect(StatusEffectInstance effect, @Nullable Entity source, CallbackInfoReturnable<Boolean> ci) {}
}
