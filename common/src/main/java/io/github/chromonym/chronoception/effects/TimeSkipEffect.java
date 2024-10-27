package io.github.chromonym.chronoception.effects;

import io.github.chromonym.chronoception.Chronoception;
import io.github.chromonym.chronoception.PlayerStateSaver;
import io.github.chromonym.chronoception.PlayerTimeData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.InstantStatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.server.network.ServerPlayerEntity;

public class TimeSkipEffect extends InstantStatusEffect {
    
    private final long time;

    public TimeSkipEffect(long time, int color) {
        super(StatusEffectCategory.NEUTRAL, color);
        this.time = time;
    }
    
    @Override
    public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (entity instanceof ServerPlayerEntity player) {
            PlayerTimeData data = PlayerStateSaver.getPlayerState(player);
            data.offset += time*(1+amplifier);
            Chronoception.syncPlayerTimes(player, false);
        }
        return true;
    }

}
