package io.github.chromonym.chronoception.screenhandlers;

import io.github.chromonym.chronoception.Chronoception;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class TemporalTableSlot extends Slot {

    public TemporalTableSlot(Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return stack.getItem() == Chronoception.TEMPORAL_GEM.get() || stack.getItem() == Chronoception.TEMPORAL_DUST.get();
    }
    
}
