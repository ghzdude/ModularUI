package com.cleanroommc.modularui.utils;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import org.jetbrains.annotations.NotNull;

public class ItemStackItemHandler implements IItemHandlerModifiable {

    private static final String KEY_ITEMS = "Items";

    private final ItemStack container;
    private final int slots;

    public ItemStackItemHandler(ItemStack container, int slots) {
        this.container = container;
        this.slots = slots;
    }

    @Override
    public int getSlots() {
        return this.slots;
    }

    @NotNull
    @Override
    public ItemStack getStackInSlot(int slot) {
        validateSlotIndex(slot);
        NBTTagCompound item = getItemsNbt().getCompoundTagAt(slot);
        int count = item.getInteger("Count");
        var stack = new ItemStack(item);
        stack.setCount(count);
        return item.isEmpty() ? ItemStack.EMPTY : stack;
    }

    @Override
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        validateSlotIndex(slot);
        NBTTagList list = getItemsNbt();
        NBTTagCompound item = stack.serializeNBT();
        item.setInteger("Count", stack.getCount());
        list.set(slot, stack.isEmpty() ? new NBTTagCompound() : item);
    }

    @NotNull
    @Override
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) return ItemStack.EMPTY;
        ItemStack existing = getStackInSlot(slot);

        int limit = getStackLimit(slot, stack);

        if (!existing.isEmpty()) {
            if (!ItemHandlerHelper.canItemStacksStack(stack, existing))
                return stack;

            limit -= existing.getCount();
        }

        if (limit <= 0) return stack;

        boolean reachedLimit = stack.getCount() > limit;

        if (!simulate) {
            if (existing.isEmpty()) {
                setStackInSlot(slot, reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, limit) : stack);
            } else {
                existing.grow(reachedLimit ? limit : stack.getCount());
            }
            onContentsChanged(slot);
        }

        return reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - limit) : ItemStack.EMPTY;
    }

    @NotNull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount == 0) return ItemStack.EMPTY;

        ItemStack existing = getStackInSlot(slot);
        if (existing.isEmpty()) return ItemStack.EMPTY;

        int toExtract = Math.min(amount, existing.getMaxStackSize());

        if (existing.getCount() <= toExtract) {
            if (!simulate) {
                setStackInSlot(slot, ItemStack.EMPTY);
                onContentsChanged(slot);
            }
            return existing;
        } else {
            if (!simulate) {
                setStackInSlot(slot, ItemHandlerHelper.copyStackWithSize(existing, existing.getCount() - toExtract));
                onContentsChanged(slot);
            }

            return ItemHandlerHelper.copyStackWithSize(existing, toExtract);
        }
    }

    @Override
    public int getSlotLimit(int slot) {
        return Integer.MAX_VALUE;
    }

    protected int getStackLimit(int slot, @NotNull ItemStack stack) {
        return getSlotLimit(slot);
//        return Math.min(getSlotLimit(slot), stack.getMaxStackSize());
    }

    protected void onContentsChanged(int slot) {
    }

    public NBTTagList getItemsNbt() {
        NBTTagCompound nbt = this.container.getTagCompound();
        if (nbt == null) {
            nbt = new NBTTagCompound();
            this.container.setTagCompound(nbt);
        }
        if (!nbt.hasKey(KEY_ITEMS)) {
            NBTTagList list = new NBTTagList();
            for (int i = 0; i < getSlots(); i++) {
                list.appendTag(new NBTTagCompound());
            }
            nbt.setTag(KEY_ITEMS, list);
        }
        return nbt.getTagList(KEY_ITEMS, Constants.NBT.TAG_COMPOUND);
    }

    protected void validateSlotIndex(int slot) {
        if (slot < 0 || slot >= this.slots) {
            throw new RuntimeException("Slot " + slot + " not in valid range - [0," + this.slots + ")");
        }
    }
}
