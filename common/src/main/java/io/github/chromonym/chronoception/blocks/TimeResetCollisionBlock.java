package io.github.chromonym.chronoception.blocks;

import java.util.ArrayList;

import io.github.chromonym.chronoception.Chronoception;
import io.github.chromonym.chronoception.PlayerStateSaver;
import io.github.chromonym.chronoception.PlayerTimeData;
import io.github.chromonym.chronoception.effects.TimeMultiplyEffect;
import io.github.chromonym.chronoception.effects.TimeOverrideEffect;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class TimeResetCollisionBlock extends Block {

    public TimeResetCollisionBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VoxelShapes.empty();
    }

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (entity instanceof LivingEntity livingEntity) {
            ArrayList<StatusEffectInstance> effects = new ArrayList<StatusEffectInstance>();
                effects.addAll(livingEntity.getStatusEffects()); // copy over the effects so i'm not looping through the actual list *while modifying it*
                effects.forEach((effect) -> {
                    if(effect.getEffectType().value() instanceof TimeMultiplyEffect || effect.getEffectType().value() instanceof TimeOverrideEffect) {
                        livingEntity.removeStatusEffect(effect.getEffectType());
                    }
                });
            if (entity instanceof ServerPlayerEntity player) {
                PlayerTimeData data = PlayerStateSaver.getPlayerState(player);
                if (data.offset != 0.0 || data.tickrate != 1.0 || data.baseTickrate != 1.0) {
                    data.offset = 0.0;
                    data.tickrate = 1.0;
                    data.baseTickrate = 1.0;
                    Chronoception.syncPlayerTimes(player);
                }
            }
        }
    }

    @Override
    protected VoxelShape getCameraCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VoxelShapes.empty();
    }

    @Override
    protected float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos) {
        return 1.0f;
    }

    @Override
    protected boolean isTransparent(BlockState state, BlockView world, BlockPos pos) {
        return true;
    }

    @Override
    protected boolean isSideInvisible(BlockState state, BlockState stateFrom, Direction direction) {
        if (stateFrom.isOf(this)) {
            return true;
        }
        return super.isSideInvisible(state, stateFrom, direction);
    }
    
}
