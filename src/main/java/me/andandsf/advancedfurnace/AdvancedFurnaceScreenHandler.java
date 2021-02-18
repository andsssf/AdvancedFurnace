package me.andandsf.advancedfurnace;

import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.FurnaceOutputSlot;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.Nullable;

public class AdvancedFurnaceScreenHandler extends ScreenHandler {
    private final Inventory inventory;
    PropertyDelegate propertyDelegate;

    public AdvancedFurnaceScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(9), new ArrayPropertyDelegate(10));
    }

    public AdvancedFurnaceScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, PropertyDelegate propertyDelegate) {
        super(AdvancedFurnace.ADVANCED_FURNACE_SCREEN_HANDLER, syncId);
        checkSize(inventory, 9);
        this.inventory = inventory;
        this.propertyDelegate = propertyDelegate;
        inventory.onOpen(playerInventory.player);
        this.addProperties(propertyDelegate);
        int m,l;

        this.addSlot(new Slot(inventory, 0, 12, 33){
            @Override
            public boolean canInsert(ItemStack stack) {
                return AbstractFurnaceBlockEntity.canUseAsFuel(stack) || stack.getItem() == Items.BUCKET;
            }
            @Override
            public int getMaxItemCount(ItemStack stack) {
                return stack.getItem() == Items.BUCKET ? 1 : super.getMaxItemCount(stack);
            }
        });

        for (m=0; m < 4; ++m) {
            this.addSlot(new Slot(inventory, 2*m+1, 46 + 27*m, 19));
            this.addSlot(new FurnaceOutputSlot(playerInventory.player, inventory, 2*m+2, 46 + 27*m, 63));
        }

        for (m = 0; m < 3; ++m) {
            for (l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + m * 9 + 9, 8 + l * 18, 95 + m * 18));
            }
        }
        //The player Hotbar
        for (m = 0; m < 9; ++m) {
            this.addSlot(new Slot(playerInventory, m, 8 + m * 18, 153));
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int invSlot) {
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

    public boolean isBurning() {
        return propertyDelegate.get(0) > 0;
    }

    public int getFuelProgress() {
        int i = getFuelTime();
        if (i == 0) {
            i = 200;
        }

        return getBurnTime() * 13 / i;
    }

    public int getCookProgress(int index) {
        int i = getCookTime(index);
        int j = getCookTimeTotal(index);
        return j != 0 && i != 0 ? i * 24 / j : 0;
    }

    private int getBurnTime() {
        return propertyDelegate.get(0);
    }

    private int getFuelTime() {
        return propertyDelegate.get(1);
    }

    private int getCookTime(int i) {
        return propertyDelegate.get(2 + i * 2);
    }

    private int getCookTimeTotal(int i) {
        return propertyDelegate.get(3 + i * 2);
    }
}
