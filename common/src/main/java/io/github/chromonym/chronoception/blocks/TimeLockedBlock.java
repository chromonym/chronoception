package io.github.chromonym.chronoception.blocks;

import io.github.chromonym.chronoception.Chronoception;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.EntityShapeContext;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.TransparentBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class TimeLockedBlock extends TransparentBlock {

    public TimeLockedBlock(Settings settings) {
        super(settings);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        if (!world.getDimension().natural()) {
            replace(state, Blocks.ORANGE_STAINED_GLASS.getDefaultState(), world, pos, NOTIFY_ALL_AND_REDRAW | SKIP_DROPS);
        } else {
            super.onPlaced(world, pos, state, placer, itemStack);
        }
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView blockWorld, BlockPos pos, ShapeContext context) {
        if (context instanceof EntityShapeContext entityContext && blockWorld instanceof World world) {
            long localTime;
            if (entityContext.getEntity() instanceof PlayerEntity player) {
                localTime = Chronoception.getPercievedTime(world, player);
            } else {
                localTime = world.getLevelProperties().getTimeOfDay() % 24000L;
            }
            if ((localTime <= 13000L && localTime >= 12000L) || (localTime >= 0L && localTime <= 1000L)) { // currently testing sunset/sunrise
                return VoxelShapes.empty();
            } else {
                return state.getOutlineShape(blockWorld, pos);
            }
        }
        return super.getCollisionShape(state, blockWorld, pos, context);
    }
}
