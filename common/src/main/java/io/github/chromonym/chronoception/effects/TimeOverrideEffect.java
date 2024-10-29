package io.github.chromonym.chronoception.effects;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class TimeOverrideEffect extends StatusEffect {

    private final double override;

    public TimeOverrideEffect(double override, int color) {
        super(StatusEffectCategory.NEUTRAL, color);
        this.override = override;
    }

    public double getOverride() {
        return override;
    }
}
