package io.github.chromonym.chronoception.effects;

import io.github.chromonym.chronoception.Chronoception;
import io.github.chromonym.chronoception.PlayerStateSaver;
import io.github.chromonym.chronoception.PlayerTimeData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.InstantStatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.server.network.ServerPlayerEntity;

public class TimeResetEffect extends InstantStatusEffect {

    public TimeResetEffect() {
        super(StatusEffectCategory.NEUTRAL, 0x888888);
    }
    
    @Override
    public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (entity instanceof ServerPlayerEntity player) {
            PlayerTimeData data = PlayerStateSaver.getPlayerState(player);
            data.offset = 0;
            Chronoception.syncPlayerTimes(player);
        }
        return true;
    }
    
}
