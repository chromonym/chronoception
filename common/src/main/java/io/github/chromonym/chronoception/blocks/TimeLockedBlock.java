package io.github.chromonym.chronoception.blocks;

import java.util.function.BiPredicate;

import io.github.chromonym.chronoception.Chronoception;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.EntityShapeContext;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public abstract class TimeLockedBlock extends Block {

    private final Block timelessDimensionReplace;
    public final BiPredicate<Long,Long> validTime;

    public TimeLockedBlock(Settings settings, Block timelessDimensionReplace, BiPredicate<Long, Long> validTime) {
        // validTime is a predicate that takes a long representing time of day (0-24000L) and one representing lunar time (0-192000L) and calculates whether the time is "valid"
        super(settings);
        this.timelessDimensionReplace = timelessDimensionReplace;
        this.validTime = validTime;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        if (!world.getDimension().natural()) {
            replace(state, timelessDimensionReplace.getDefaultState(), world, pos, NOTIFY_ALL_AND_REDRAW | SKIP_DROPS);
        } else {
            super.onPlaced(world, pos, state, placer, itemStack);
        }
    }
    
    public boolean isValidTime(WorldAccess world, ShapeContext context) {
        long localTime = world.getLevelProperties().getTimeOfDay() % 24000L;
        long localLunarTime = world.getLunarTime() % 192000L;
        if (context instanceof EntityShapeContext entityContext) {
            if (entityContext.getEntity() instanceof PlayerEntity player) {
                localTime = Chronoception.getPercievedTime(world, player);
                localLunarTime = Chronoception.getPercievedLunarTime(world, player);
            }
        }
        return validTime.test(localTime, localLunarTime);
    }

    public Block getTimelessDimensionReplace() {
        return this.timelessDimensionReplace;
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (world.isClient()) {
            if (world instanceof ClientWorld && !validTime.test(world.getTimeOfDay(), world.getLunarTime())) {
                world.addBlockBreakParticles(pos, state); // TODO use a better particle
            }
        }
        super.randomDisplayTick(state, world, pos, random);
    }
}
