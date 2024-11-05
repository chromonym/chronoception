package io.github.chromonym.chronoception.blockentities;

import java.util.Optional;
import java.util.UUID;

import io.github.chromonym.chronoception.Chronoception;
import io.github.chromonym.chronoception.recipes.DayRecipe;
import io.github.chromonym.chronoception.recipes.MoonRecipe;
import io.github.chromonym.chronoception.screenhandlers.TemporalTableScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TemporalTableBlockEntity extends BlockEntity implements NamedScreenHandlerFactory, Inventory {

    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(1, ItemStack.EMPTY);
    public TemporalTableRecipeInput recipeInput = new TemporalTableRecipeInput();
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
        if (!world.isClient() && blockEntity.recentInteract != null) {
            RecipeManager recipes = world.getRecipeManager();
            Optional<RecipeEntry<DayRecipe>> optional = recipes.getFirstMatch(Chronoception.DAY_RECIPE.get(), blockEntity.recipeInput, world);
            Optional<RecipeEntry<MoonRecipe>> moon = recipes.getFirstMatch(Chronoception.MOON_RECIPE.get(), blockEntity.recipeInput, world);
            long local = Chronoception.getPercievedTime(world, blockEntity.recentInteract);
            int lunar = ((int)(Chronoception.getPercievedLunarTime(world, blockEntity.recentInteract) / 24000L % 8L + 8L) % 8);
            ItemStack result = ItemStack.EMPTY;
            ItemStack stack = blockEntity.getStack(0);
            if (optional.isPresent()) {
                if (Chronoception.CREPUSCULAR.test(local, lunar)) {result = optional.get().value().getDuskOutput();}
                else if (Chronoception.DIURNAL.test(local, lunar)) {result = optional.get().value().getDayOutput();}
                else if (Chronoception.NOCTURNAL.test(local, lunar)) {result = optional.get().value().getNightOutput();}
                if (!result.isEmpty()) {
                    blockEntity.progress++;
                    if (blockEntity.progress >= blockEntity.getStack(0).getCount()*20) {
                        blockEntity.setStack(0, stack.copyComponentsToNewStack(result.getItem(), Math.min(stack.getCount() * result.getCount(), result.getMaxCount())));
                        blockEntity.progress = 0;
                    }
                } else {
                    blockEntity.progress = 0;
                }
            } else if (moon.isPresent()) {
                if (Chronoception.FULL_MOON.test(local, lunar)) {result = moon.get().value().getFullOutput();}
                else if (Chronoception.GIBBOUS_MOON.test(local, lunar)) {result = moon.get().value().getGibbousOutput();}
                else if (Chronoception.QUARTER_MOON.test(local, lunar)) {result = moon.get().value().getQuarterOutput();}
                else if (Chronoception.CRESCENT_MOON.test(local, lunar)) {result = moon.get().value().getCrescentOutput();}
                else if (Chronoception.NEW_MOON.test(local, lunar)) {result = moon.get().value().getNewOutput();}
                if (!result.isEmpty()) {
                    blockEntity.progress++;
                    if (blockEntity.progress >= blockEntity.getStack(0).getCount()*20) {
                        blockEntity.setStack(0, stack.copyComponentsToNewStack(result.getItem(), Math.min(stack.getCount() * result.getCount(), result.getMaxCount())));
                        blockEntity.progress = 0;
                    }
                } else {
                    blockEntity.progress = 0;
                }
            } else {
                blockEntity.progress = 0;
            }
            blockEntity.markDirty();
        }
    }

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

    public class TemporalTableRecipeInput implements RecipeInput {

        public TemporalTableRecipeInput() {}

        @Override
        public int getSize() {
            return 1;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return items.get(0);
        }
    }

    /*@Override
    public int getSize() {
        return 1;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return items.get(slot);
    }*/

    @Override
    public int size() {
        return getItems().size();
    }

    @Override
    public ItemStack getStack(int slot) {
        return getItems().get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int count) {
        ItemStack result = Inventories.splitStack(getItems(), slot, count);
        if (!result.isEmpty()) {
            markDirty();
        }
        return result;
    }

    @Override
    public ItemStack removeStack(int slot) {
        return Inventories.removeStack(getItems(), slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        getItems().set(slot, stack);
        if (stack.getCount() > stack.getMaxCount()) {
            stack.setCount(stack.getMaxCount());
        }
    }

    @Override
    public void clear() {
        getItems().clear();
    }

    @Override
    public void markDirty() {
        // Override if you want behavior.
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < size(); i++) {
            ItemStack stack = getStack(i);
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
