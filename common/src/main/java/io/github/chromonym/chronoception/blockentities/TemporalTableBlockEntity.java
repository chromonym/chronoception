package io.github.chromonym.chronoception.blockentities;

import java.util.UUID;

import io.github.chromonym.chronoception.Chronoception;
import io.github.chromonym.chronoception.screenhandlers.TemporalTableScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TemporalTableBlockEntity extends BlockEntity implements NamedScreenHandlerFactory, ImplementedInventory {

    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(1, ItemStack.EMPTY);
    public UUID recentInteract = null;
    public int progress;
    private final PropertyDelegate propertyDelegate = new PropertyDelegate() {
        @Override
        public int get(int index) {
            if (index == 1) {
                return (int)(Chronoception.getPercievedTime(world, recentInteract) % 24000); // should already be %24000 but just in case
            }
            if (index == 2) {
                return ((int)(Chronoception.getPercievedLunarTime(world, recentInteract) / 24000L % 8L + 8L) % 8);
            }
            return progress;
        }

        @Override
        public void set(int index, int value) {
            progress = value;
        }

        @Override
        public int size() {
            return 3;
        }
    };

    public TemporalTableBlockEntity(BlockPos pos, BlockState state) {
        super(Chronoception.TEMPORAL_TABLE_ENTITY.get(), pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, TemporalTableBlockEntity blockEntity) {
        if (!world.isClient()) {
            if ((blockEntity.getStack(0).getItem() == Chronoception.TEMPORAL_GEM.get() ||
            blockEntity.getStack(0).getItem() == Chronoception.TEMPORAL_DUST.get()) && blockEntity.recentInteract != null) {
                blockEntity.progress++;
                if (blockEntity.progress >= blockEntity.getStack(0).getCount()*20) {
                    long local = Chronoception.getPercievedTime(world, blockEntity.recentInteract);
                    int lunar = ((int)(Chronoception.getPercievedLunarTime(world, blockEntity.recentInteract) / 24000L % 8L + 8L) % 8);
                    ItemStack result = ItemStack.EMPTY;
                    ItemStack stack = blockEntity.getStack(0);
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
                        blockEntity.setStack(0, stack.copyComponentsToNewStack(result.getItem(), stack.getCount()));
                        blockEntity.progress = 0;
                    }
                }
            } else {
                blockEntity.progress = 0;
            }
            blockEntity.markDirty();
        }
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return items;
    }

    public UUID getRecentInteract() {
        return recentInteract;
    }

    @Override
    protected void readNbt(NbtCompound nbt, WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        this.progress = nbt.getInt("craftProgress");
        this.recentInteract = nbt.getUuid("recentInteract");
        Inventories.readNbt(nbt, items, registryLookup);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, WrapperLookup registryLookup) {
        Inventories.writeNbt(nbt, items, registryLookup);
        if (recentInteract != null) {
            nbt.putUuid("recentInteract", recentInteract);
        }
        nbt.putInt("craftProgress", progress);
        super.writeNbt(nbt, registryLookup);
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new TemporalTableScreenHandler(syncId, playerInventory, this, this.propertyDelegate);
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable(getCachedState().getBlock().getTranslationKey());
    }
}
