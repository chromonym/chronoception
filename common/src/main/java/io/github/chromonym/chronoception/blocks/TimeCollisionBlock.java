package io.github.chromonym.chronoception.blocks;

import java.util.function.BiPredicate;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;

public class TimeCollisionBlock extends TimeLockedBlock {

    public TimeCollisionBlock(Settings settings, Block timelessDimensionReplace, BiPredicate<Long, Long> validTime) {
        super(settings, timelessDimensionReplace, validTime);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView blockWorld, BlockPos pos, ShapeContext context) {
        if (blockWorld instanceof WorldAccess world) {
            return isValidTime(world, context) ? VoxelShapes.empty() : state.getOutlineShape(blockWorld, pos);
        }
        return super.getCollisionShape(state, blockWorld, pos, context);
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
