package io.github.chromonym.chronoception.blocks;

import io.github.chromonym.chronoception.Chronoception;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class TemporalTableBlock extends Block {
    public TemporalTableBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos,
            PlayerEntity player, Hand hand, BlockHitResult hit) {
        long local = Chronoception.getPercievedTime(world, player);
        int lunar = ((int)(Chronoception.getPercievedLunarTime(world, player) / 24000L % 8L + 8L) % 8);
        ItemStack result = ItemStack.EMPTY;
        // yes i know this is extremely bad practise both in terms of custom minecraft recipes and java coding but i really don't have much time to do this
        if (stack.getItem() == Chronoception.TEMPORAL_GEM.get()) {
            if (Chronoception.CREPUSCULAR.test(local, lunar)) {result = Chronoception.CREPUSCULAR_GEM.get().getDefaultStack();}
            else if (Chronoception.DIURNAL.test(local, lunar)) {result = Chronoception.DIURNAL_GEM.get().getDefaultStack();}
            else if (Chronoception.NOCTURNAL.test(local, lunar)) {result = Chronoception.NOCTURNAL_GEM.get().getDefaultStack();}
        } else if (stack.getItem() == Chronoception.TEMPORAL_DUST.get()) {
            if (Chronoception.FULL_MOON.test(local, lunar)) {result = Chronoception.FULL_MOON_DUST.get().getDefaultStack();}
            else if (Chronoception.GIBBOUS_MOON.test(local, lunar)) {result = Chronoception.GIBBOUS_DUST.get().getDefaultStack();}
            else if (Chronoception.QUARTER_MOON.test(local, lunar)) {result = Chronoception.QUARTER_DUST.get().getDefaultStack();}
            else if (Chronoception.CRESCENT_MOON.test(local, lunar)) {result = Chronoception.CRESCENT_DUST.get().getDefaultStack();}
            else if (Chronoception.NEW_MOON.test(local, lunar)) {result = Chronoception.NEW_MOON_DUST.get().getDefaultStack();}
        }
        if (!result.isEmpty()) {
            player.setStackInHand(hand, stack.copyComponentsToNewStack(result.getItem(), stack.getCount()));
            return ItemActionResult.SUCCESS;
        }
        return super.onUseWithItem(stack, state, world, pos, player, hand, hit);
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
