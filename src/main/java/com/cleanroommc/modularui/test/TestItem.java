package com.cleanroommc.modularui.test;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.factory.HandGuiData;
import com.cleanroommc.modularui.factory.ItemGuiFactory;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.ItemCapabilityProvider;
import com.cleanroommc.modularui.utils.ItemStackItemHandler;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.layout.Column;

import com.cleanroommc.modularui.widgets.layout.Grid;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class TestItem extends Item implements IGuiHolder<HandGuiData> {

    public static final TestItem testItem = new TestItem();

    @Override
    public ModularPanel buildUI(HandGuiData guiData, PanelSyncManager guiSyncManager) {
        IItemHandlerModifiable itemHandler = (IItemHandlerModifiable) guiData.getUsedItemStack().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        guiSyncManager.registerSlotGroup("mixer_items", 2);

        ModularPanel panel = ModularPanel.defaultPanel("knapping_gui");
        List<IWidget> l = new ArrayList<>(3);
        l.add(new Rectangle()
                .setColor(Color.RED.main)
                .asWidget()
                .size(18)
                .marginRight(0));
        l.add(new Rectangle()
                .setColor(Color.BLUE.main)
                .asWidget()
                .size(18)
                .marginRight(2));
        l.add(new Rectangle()
                .setColor(Color.GREEN.main)
                .asWidget()
                .size(18));
        panel.width(200);
        panel.child(new Grid()
                .width(190)
                .margin(0)
                .coverChildrenHeight()
                .mapTo(3, l, (i, widget) -> widget));
//        panel.child(new Column().margin(7)
//                .child(new ParentWidget<>().widthRel(1f).expanded()
//                        .child(SlotGroupWidget.builder()
//                                .row("II")
//                                .row("II")
//                                .key('I', index -> new ItemSlot().slot(SyncHandlers.itemSlot(itemHandler, index)
//                                        .ignoreMaxStackSize(true)
//                                        .slotGroup("mixer_items")))
//                                .build()
//                                .align(Alignment.Center)))
//                .child(SlotGroupWidget.playerInventory(0)));

        return panel;
    }

    @Override
    @Nonnull
    public ActionResult<ItemStack> onItemRightClick(World world, @NotNull EntityPlayer player, @Nonnull EnumHand hand) {
        if (!world.isRemote) {
            ItemGuiFactory.open((EntityPlayerMP) player, hand);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(@NotNull ItemStack stack, @Nullable NBTTagCompound nbt) {
        return new ItemCapabilityProvider() {
            @Override
            public <T> @Nullable T getCapability(@NotNull Capability<T> capability) {
                if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
                    return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(new ItemStackItemHandler(stack, 4));
                }
                return null;
            }
        };
    }
}
