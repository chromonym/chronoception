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

    public TimeCollisionBlock(Settings settings, Block timelessDimensionReplace, BiPredicate<Long, Integer> validTime) {
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

    /*@Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (world.isClient()) {
            if (world instanceof ClientWorld && !validTime.test(world.getTimeOfDay(), world.getLunarTime())) {
                / * for (int i = 0; i < 4; ++i) {
                //ParticleUtil.spawnParticlesAround(world, pos, 3, 0.6, 1.0, true, ParticleTypes.ELECTRIC_SPARK);
                    double d = random.nextGaussian() * 0.04;
                    double e = random.nextGaussian() * 0.04;
                    double f = random.nextGaussian() * 0.04;
                    double h = (double)pos.getX() - 0.2 + random.nextDouble() * 1.4;
                    double j = (double)pos.getY() - 0.2 + random.nextDouble() * 1.4;
                    double k = (double)pos.getZ() - 0.2 + random.nextDouble() * 1.4;
                    world.addParticle(ParticleTypes.FIREWORK, h, j, k, d, e, f);
                } * /
                world.addParticle(ParticleTypes.ELECTRIC_SPARK, (double)pos.getX(), (double)pos.getY()+1.0, (double)pos.getZ(), 0.0, 0.0, 0.0);
                world.addParticle(ParticleTypes.ELECTRIC_SPARK, (double)pos.getX()+1.0, (double)pos.getY()+1.0, (double)pos.getZ(), 0.0, 0.0, 0.0);
                world.addParticle(ParticleTypes.ELECTRIC_SPARK, (double)pos.getX()+1.0, (double)pos.getY()+1.0, (double)pos.getZ()+1.0, 0.0, 0.0, 0.0);
                world.addParticle(ParticleTypes.ELECTRIC_SPARK, (double)pos.getX(), (double)pos.getY()+1.0, (double)pos.getZ()+1.0, 0.0, 0.0, 0.0);
                world.addParticle(ParticleTypes.ELECTRIC_SPARK, (double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), 0.0, 0.0, 0.0);
                world.addParticle(ParticleTypes.ELECTRIC_SPARK, (double)pos.getX()+1.0, (double)pos.getY(), (double)pos.getZ(), 0.0, 0.0, 0.0);
                world.addParticle(ParticleTypes.ELECTRIC_SPARK, (double)pos.getX()+1.0, (double)pos.getY(), (double)pos.getZ()+1.0, 0.0, 0.0, 0.0);
                world.addParticle(ParticleTypes.ELECTRIC_SPARK, (double)pos.getX(), (double)pos.getY(), (double)pos.getZ()+1.0, 0.0, 0.0, 0.0);
            }
        }
        super.randomDisplayTick(state, world, pos, random);
    }*/

}
