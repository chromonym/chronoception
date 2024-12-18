package io.github.chromonym.chronoception.screenhandlers;

import io.github.chromonym.chronoception.Chronoception;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class TemporalTableScreenHandler extends ScreenHandler {
    private final Inventory inventory;
    PropertyDelegate propertyDelegate;

    public TemporalTableScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(1), new ArrayPropertyDelegate(3));
    }

    public TemporalTableScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, PropertyDelegate propertyDelegate) {
        super(Chronoception.TEMPORAL_TABLE_SCREEN_HANDLER.get(), syncId);
        checkSize(inventory, 1);
        this.inventory = inventory;
        this.propertyDelegate = propertyDelegate;
        inventory.onOpen(playerInventory.player);
        this.addProperties(propertyDelegate);
        int m;
        int l;
        // Our inventory
        this.addSlot(new Slot(inventory, 0, 80, 21));
        // The player inventory
        for (m = 0; m < 3; ++m) {
            for (l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + m * 9 + 9, 8 + l * 18, 57 + m * 18));
            }
        }
        // The player Hotbar
        for (m = 0; m < 9; ++m) {
            this.addSlot(new Slot(playerInventory, m, 8 + m * 18, 115));
        }
    }

    public int getProgress() {
        return propertyDelegate.get(0);
    }

    public int getLocalTime() {
        return propertyDelegate.get(1);
    }

    public int getLunarTime() {
        return propertyDelegate.get(2);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();
            if (invSlot < this.inventory.size()) {
                if (!this.insertItem(originalStack, this.inventory.size(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(originalStack, 0, this.inventory.size(), false)) {
                return ItemStack.EMPTY;
            }
 
            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }
 
        return newStack;
    }
    
}
