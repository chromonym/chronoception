package io.github.chromonym.chronoception.effects;

import io.github.chromonym.chronoception.Chronoception;
import io.github.chromonym.chronoception.PlayerStateSaver;
import io.github.chromonym.chronoception.PlayerTimeData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.InstantStatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.server.network.ServerPlayerEntity;

public class TimeSetEffect extends InstantStatusEffect {

    private final long time;

    public TimeSetEffect(long time, int color) {
        super(StatusEffectCategory.NEUTRAL, color);
        this.time = time;
    }
    
    @Override
    public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (entity instanceof ServerPlayerEntity player) {
            long playerTime = Chronoception.getPercievedTime(player.getServerWorld(), player);
            long nextDay = (Math.round((double)(playerTime - time) / 24000L))*24000L + time;
            PlayerTimeData data = PlayerStateSaver.getPlayerState(player);
            data.offset = data.offset - playerTime + nextDay;
            Chronoception.syncPlayerTimes(player, false);
        }
        return true;
    }
    
}
