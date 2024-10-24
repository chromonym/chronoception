package io.github.chromonym.chronoception.blocks;

import io.github.chromonym.chronoception.PlayerStateSaver;
import io.github.chromonym.chronoception.PlayerTimeData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.EntityShapeContext;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class TimeLockedBlock extends Block {

    public TimeLockedBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView blockWorld, BlockPos pos, ShapeContext context) {
        if (context instanceof EntityShapeContext entityContext) {
            long localTime;
            if (blockWorld instanceof World world && entityContext.getEntity() instanceof PlayerEntity player) {
                if (world instanceof ClientWorld clientWorld) {
                    localTime = clientWorld.getTimeOfDay(); // should be modified already
                } else {
                    PlayerTimeData playerData = PlayerStateSaver.getPlayerState(player);
                    localTime = (world.getTimeOfDay() + (long)playerData.offset) % 24000L; // otherwise calc it here
                }
                if ((localTime <= 13000L && localTime >= 12000L) || (localTime >= 0L && localTime <= 1000L)) { // currently testing sunset/sunrise
                    return VoxelShapes.empty();
                } else {
                    return state.getOutlineShape(blockWorld, pos);
                }
            } // TODO logic here to make non-player entities behave properly based on the server time
        }
        return super.getCollisionShape(state, blockWorld, pos, context);
    }
    
}
