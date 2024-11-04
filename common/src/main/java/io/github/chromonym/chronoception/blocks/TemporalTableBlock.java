package io.github.chromonym.chronoception.blocks;

import com.mojang.serialization.MapCodec;

import io.github.chromonym.chronoception.Chronoception;
import io.github.chromonym.chronoception.blockentities.TemporalTableBlockEntity;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class TemporalTableBlock extends BlockWithEntity {

    public TemporalTableBlock(Settings settings) {
        super(settings);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new TemporalTableBlockEntity(pos, state);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return createCodec(TemporalTableBlock::new);
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state,
            BlockEntityType<T> type) {
        return validateTicker(type, Chronoception.TEMPORAL_TABLE_ENTITY.get(), TemporalTableBlockEntity::tick);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient()) {
            Chronoception.syncPlayerTimes((ServerPlayerEntity)player);
            NamedScreenHandlerFactory screenHandlerFactory = state.createScreenHandlerFactory(world, pos);
            if (world.getBlockEntity(pos) instanceof TemporalTableBlockEntity blockEntity) {
                blockEntity.recentInteract = player.getUuid();
            }
            if (screenHandlerFactory != null) {
                //MenuRegistry.openMenu((ServerPlayerEntity)player, screenHandlerFactory);
                player.openHandledScreen(screenHandlerFactory);
            }
        }
        return ActionResult.SUCCESS;
    }

    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof TemporalTableBlockEntity temporalTableBlockEntity) {
                ItemScatterer.spawn(world, pos, temporalTableBlockEntity);
                world.updateComparators(pos, this);
            }
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    @Override
    protected boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    protected int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return ScreenHandler.calculateComparatorOutput(world.getBlockEntity(pos));
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VoxelShapes.union(VoxelShapes.cuboid(0.0f, 0.875f, 0.0f, 1.0f, 1.0f, 1.0f), 
        VoxelShapes.cuboid(0.0f, 0.0f, 0.0f, 0.125f, 1.0f, 0.125f),
        VoxelShapes.cuboid(0.0f, 0.0f, 0.875f, 0.125f, 1.0f, 1.0f),
        VoxelShapes.cuboid(0.875f, 0.0f, 0.0f, 1.0f, 1.0f, 0.125f),
        VoxelShapes.cuboid(0.875f, 0.0f, 0.875f, 1.0f, 1.0f, 1.0f));
    }
    
}
