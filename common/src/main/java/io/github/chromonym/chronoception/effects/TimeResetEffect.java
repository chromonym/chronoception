package io.github.chromonym.chronoception.effects;

import java.util.ArrayList;

import io.github.chromonym.chronoception.Chronoception;
import io.github.chromonym.chronoception.PlayerStateSaver;
import io.github.chromonym.chronoception.PlayerTimeData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.InstantStatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.server.network.ServerPlayerEntity;

public class TimeResetEffect extends InstantStatusEffect {

    public TimeResetEffect() {
        super(StatusEffectCategory.NEUTRAL, 0x888888);
    }
    
    @Override
    public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
        ArrayList<StatusEffectInstance> effects = new ArrayList<StatusEffectInstance>();
        effects.addAll(entity.getStatusEffects()); // copy over the effects so i'm not looping through the actual list *while modifying it*
        effects.forEach((effect) -> {
            if((effect.getEffectType().value() instanceof TimeMultiplyEffect || effect.getEffectType().value() instanceof TimeOverrideEffect) && amplifier > 0) {
                entity.removeStatusEffect(effect.getEffectType());
            }
        });
        if (entity instanceof ServerPlayerEntity player) {
            PlayerTimeData data = PlayerStateSaver.getPlayerState(player);
            data.offset = 0.0;
            if (amplifier > 0) {
                if (data.baseTickrate == data.tickrate) {
                    data.tickrate = 1.0;
                }
                data.baseTickrate = 1.0;
            }
            Chronoception.syncPlayerTimes(player);
        }
        return true;
    }
    
}
