package io.github.chromonym.chronoception.effects;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class TimeMultiplyEffect extends StatusEffect {

    private final double multiplier;

    public TimeMultiplyEffect(double multiplier, int color) {
        super(StatusEffectCategory.NEUTRAL, color);
        this.multiplier = multiplier;
    }

    public double getMultiplier(int amplifier) {
        return Math.pow(multiplier, (double)(amplifier+1));
    }
    
}
