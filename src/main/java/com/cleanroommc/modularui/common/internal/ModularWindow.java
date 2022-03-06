package com.cleanroommc.modularui.common.internal;

import com.cleanroommc.modularui.api.*;
import com.cleanroommc.modularui.api.animation.Eases;
import com.cleanroommc.modularui.api.animation.Interpolator;
import com.cleanroommc.modularui.api.math.Alignment;
import com.cleanroommc.modularui.api.math.Color;
import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.math.Size;
import com.cleanroommc.modularui.common.widget.Widget;
import com.google.common.collect.ImmutableBiMap;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A window in a modular gui. Only the "main" window can exist on both, server and client.
 * All other only exist and needs to be opened on client.
 */
public class ModularWindow implements IWidgetParent {

    public static Builder builder(Size size) {
        return new Builder(size);
    }

    private ModularUIContext context;
    private final List<Widget> children;
    public final ImmutableBiMap<Integer, ISyncedWidget> syncedWidgets;
    private final List<Interactable> interactionListeners = new ArrayList<>();

    private final Size size;
    private Pos2d pos = Pos2d.ZERO;
    private final Alignment alignment = Alignment.Center;
    private boolean draggable = false;
    private boolean active;
    private boolean needsRebuild = false;
    private int color = 0xFFFFFFFF;

    private Interpolator openAnimation, closeAnimation;

    public ModularWindow(Size size, List<Widget> children) {
        this.size = size;
        this.children = children;

        ImmutableBiMap.Builder<Integer, ISyncedWidget> syncedWidgetBuilder = ImmutableBiMap.builder();
        AtomicInteger i = new AtomicInteger();
        IWidgetParent.forEachByLayer(this, widget -> {
            if (widget instanceof ISyncedWidget) {
                syncedWidgetBuilder.put(i.getAndIncrement(), (ISyncedWidget) widget);
            }
            return false;
        });
        this.syncedWidgets = syncedWidgetBuilder.build();
    }

    protected void initialize(ModularUIContext context) {
        this.context = context;
        for (Widget widget : children) {
            widget.initialize(this, this, 0);
        }
    }

    public void onResize(Size screenSize) {
        this.pos = alignment.getAlignedPos(screenSize, size);
        markNeedsRebuild();
    }

    /**
     * The final call after the window is initialized & positioned
     */
    public void onOpen() {
        final int startY = context.getScaledScreenSize().height, endY = pos.y;
        openAnimation = new Interpolator(0, 1, 250, Eases.EaseQuadOut, value -> {
            float val = (float) value;
            color = Color.withAlpha(color, val);
            int y = (int) ((endY - startY) * val + startY);
            setPos(new Pos2d(pos.x, y));
            markNeedsRebuild();
        });
        closeAnimation = openAnimation.getReversed(250, Eases.EaseQuadIn);
        openAnimation.forward();
        closeAnimation.setCallback(val -> context.close());
        this.pos = new Pos2d(pos.x, getContext().getScaledScreenSize().height);
    }

    /**
     * Called when the player tries to close the ui
     *
     * @return if the ui should be closed
     */
    public boolean onTryClose() {
        if (closeAnimation == null) {
            return true;
        }
        closeAnimation.forward();
        return false;
    }

    protected void setActive(boolean active) {
        this.active = active;
    }

    public void update() {
        IWidgetParent.forEachByLayer(this, Widget::onScreenUpdate);
    }

    public void frameUpdate(float partialTicks) {
        if (openAnimation != null) {
            openAnimation.update(partialTicks);
        }
        if (closeAnimation != null) {
            closeAnimation.update(partialTicks);
        }
        if (needsRebuild) {
            rebuild();
            needsRebuild = false;
        }
    }

    public void serverUpdate() {
        for (ISyncedWidget syncedWidget : syncedWidgets.values()) {
            syncedWidget.onServerTick();
        }
    }

    @SideOnly(Side.CLIENT)
    protected void rebuild() {
        for (Widget child : getChildren()) {
            child.rebuildInternal();
        }
    }

    public void pauseWindow() {
        if (isActive()) {
            setActive(false);
            IWidgetParent.forEachByLayer(this, Widget::onPause);
        }
    }

    public void resumeWindow() {
        if (!isActive()) {
            setActive(true);
            IWidgetParent.forEachByLayer(this, Widget::onResume);
        }
    }

    public void closeWindow() {
        IWidgetParent.forEachByLayer(this, widget -> {
            if (isActive()) {
                widget.onPause();
            }
            widget.onDestroy();
        });
    }

    public void drawWidgetsBackGround(float partialTicks) {
        GlStateManager.color(Color.getRedF(color), Color.getGreenF(color), Color.getBlueF(color), Color.getAlphaF(color));
        IWidgetParent.forEachByLayer(this, widget -> {
            widget.onFrameUpdate();
            if (widget.isEnabled()) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(widget.getAbsolutePos().x, widget.getAbsolutePos().y, 0);
                GlStateManager.color(Color.getRedF(color), Color.getGreenF(color), Color.getBlueF(color), Color.getAlphaF(color));
                GlStateManager.enableBlend();
                IWidgetDrawable background = widget.getDrawable();
                if (background != null) {
                    background.drawWidgetCustom(widget, partialTicks);
                }
                widget.drawInBackground(partialTicks);
                GlStateManager.popMatrix();
            }
            return false;
        });
        GlStateManager.color(1, 1, 1, 1);
    }

    public void drawWidgetsForeGround(float partialTicks) {
        IWidgetParent.forEachByLayer(this, widget -> {
            if (widget.isEnabled()) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(widget.getAbsolutePos().x, widget.getAbsolutePos().y, 0);
                GlStateManager.enableBlend();
                widget.drawInForeground(partialTicks);
                GlStateManager.popMatrix();
            }
            return false;
        });
    }

    @Override
    public Size getSize() {
        return size;
    }

    @Override
    public Pos2d getAbsolutePos() {
        return pos;
    }

    @Override
    public Pos2d getPos() {
        return pos;
    }

    @Override
    public List<Widget> getChildren() {
        return children;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isDraggable() {
        return draggable;
    }

    public ModularUIContext getContext() {
        return context;
    }

    public void markNeedsRebuild() {
        this.needsRebuild = true;
    }

    public void setPos(Pos2d pos) {
        this.pos = pos;
    }

    /**
     * The events of the added listeners are always called.
     */
    public void addInteractionListener(Interactable interactable) {
        interactionListeners.add(interactable);
    }

    public List<Interactable> getInteractionListeners() {
        return interactionListeners;
    }

    public int getSyncedWidgetId(ISyncedWidget syncedWidget) {
        Integer id = syncedWidgets.inverse().get(syncedWidget);
        if (id == null) {
            throw new NoSuchElementException("Can't find id for ISyncedWidget " + syncedWidget);
        }
        return id;
    }

    public ISyncedWidget getSyncedWidget(int id) {
        ISyncedWidget syncedWidget = syncedWidgets.get(id);
        if (syncedWidget == null) {
            throw new NoSuchElementException("Can't find ISyncedWidget for id " + id);
        }
        return syncedWidget;
    }

    public static class Builder implements IWidgetBuilder<Builder> {

        private final List<Widget> widgets = new ArrayList<>();
        private Size size;
        private boolean draggable = false;

        private Builder(Size size) {
            this.size = size;
        }

        public Builder setSize(Size size) {
            this.size = size;
            return this;
        }

        public Builder setDraggable(boolean draggable) {
            this.draggable = draggable;
            return this;
        }

        @Override
        public void addWidgetInternal(Widget widget) {
            widgets.add(widget);
        }

        public ModularWindow build() {
            ModularWindow window = new ModularWindow(size, widgets);
            window.draggable = draggable;
            return window;
        }
    }
}