package com.cleanroommc.modularui.widget.scroll;

import com.cleanroommc.modularui.api.GuiAxis;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.widget.Widget;

import com.cleanroommc.modularui.widget.sizer.Area;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class Scrollbar extends Widget<Scrollbar> implements Interactable, IDrawable {

    @Nullable
    private final ScrollData data;
    private final Supplier<Area> parentArea;
    private boolean keepScrollBarInArea;

    public Scrollbar(@Nullable ScrollData data, Supplier<Area> parentArea) {
        this.data = data;
        this.parentArea = parentArea;
    }

    @Override
    public boolean onMouseRelease(int mouseButton) {
        if (data != null) {
            data.dragging = false;
            data.clickOffset = 0;
        }
        return false;
    }

    /* GUI code for easier manipulations */

    @SideOnly(Side.CLIENT)
    public boolean mouseClicked(GuiContext context) {
        return this.mouseClicked(context.getAbsMouseX(), context.getAbsMouseY());
    }

    /**
     * This method should be invoked to register dragging
     */
    public boolean mouseClicked(int x, int y) {
        if (data != null) {
            return this.data.onMouseClicked(getArea(), x, y);
        }
        return false;
    }

    @Override
    public void onResized() {
        super.onResized();
        if (this.data == null) return;
        Area area = getParentArea();
        boolean axisStart = data.isOnAxisStart();
        GuiAxis mainAxis = data.getAxis();
        GuiAxis crossAxis = mainAxis.getOther();

        getArea().setSize(mainAxis, area.getSize(mainAxis));
        getArea().setSize(crossAxis, data.getThickness());

        int start = axisStart ? 0 : area.getSize(crossAxis) - area.getPadding().getEnd(crossAxis);
        getArea().setPoint(crossAxis, start - getArea().getSize(crossAxis));
        getArea().setPoint(mainAxis, 0);

        this.data.clamp(area);
        if (this.keepScrollBarInArea) return;


        // todo this doesn't work
        if (this.data.getAxis().isHorizontal()) {
            getArea().y += this.data.getThickness();
        } else {
            getArea().x += this.data.getThickness();
        }
    }

    @SideOnly(Side.CLIENT)
    public void drag(GuiContext context) {
        if (canDrag())
            this.drag(context.getMouse(data.getAxis()));
    }

    /**
     * This should be invoked in a drawing or and update method. It's
     * responsible for scrolling through this view when dragging.
     */
    public void drag(int x, int y) {
        if (canDrag())
            drag(data.getAxis().isHorizontal() ? x : y);
    }

    public void drag(int m) {
        if (canDrag()) {
            float progress = data.getProgress(getParentArea(), m);
            this.data.scrollTo(getParentArea(), (int) (progress * (data.getScrollSize() - data.getVisibleSize(getParentArea()))));
        }
    }

    public final boolean canDrag() {
        return data != null && !data.dragging;
    }

    public int getScroll() {
        return this.data == null ? 0 : this.data.getScroll();
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        if (data != null && data.shouldDraw(width, height)) {
            this.data.drawScrollbar(widgetTheme, getParentArea());
        }
    }

    public void setScrollSize(int size) {
        if (this.data != null) {
            this.data.setScrollSize(size);
        }
    }

    public boolean isDragging() {
        return data != null && data.isDragging();
    }

    public boolean isActive() {
        if (data == null) return false;
        int l = getParentArea().getSize(data.getAxis());
        return l > data.getScrollSize();
    }

    @Override
    public Area getParentArea() {
        return this.parentArea.get();
    }
}
