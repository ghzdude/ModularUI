package com.cleanroommc.modularui.widget;

import com.cleanroommc.modularui.api.GuiAxis;
import com.cleanroommc.modularui.api.layout.IViewport;
import com.cleanroommc.modularui.api.layout.IViewportStack;
import com.cleanroommc.modularui.api.widget.IGuiAction;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.Stencil;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.utils.HoveredWidgetList;
import com.cleanroommc.modularui.widget.scroll.HorizontalScrollData;
import com.cleanroommc.modularui.widget.scroll.Scrollbar;
import com.cleanroommc.modularui.widget.scroll.VerticalScrollData;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A scrollable parent widget. Children can be added
 *
 * @param <I> type of children (in most cases just {@link IWidget})
 * @param <W> type of this widget
 */
public abstract class AbstractScrollWidget<I extends IWidget, W extends AbstractScrollWidget<I, W>> extends AbstractParentWidget<I, W> implements IViewport, Interactable {

//    private final ScrollArea scroll = new ScrollArea();
    private final Scrollbar horizontalBar;
    private final Scrollbar verticalBar;
    private boolean keepScrollBarInArea = false;

    public AbstractScrollWidget(@Nullable HorizontalScrollData x, @Nullable VerticalScrollData y) {
        this.horizontalBar = Scrollbar.horizontal(this::getArea);
        this.verticalBar = Scrollbar.vertical(this::getArea);
        listenGuiAction((IGuiAction.MouseReleased) this::onMouseRelease);
    }

    @Override
    public void onInit() {
        this.horizontalBar.initialise(this, false);
        this.verticalBar.initialise(this, false);
    }

//    @Override
//    public Area getArea() {
//        return this.scroll;
//    }

//    public ScrollArea getScrollArea() {
//        return this.scroll;
//    }

    @Override
    public void transformChildren(IViewportStack stack) {
        stack.translate(-getScrollX(), -getScrollY());
    }

    @Override
    public void getSelfAt(IViewportStack stack, HoveredWidgetList widgets, int x, int y) {
        if (isInside(stack, x, y)) {
            widgets.add(this, stack.peek());
        }
    }

    @Override
    public void getWidgetsAt(IViewportStack stack, HoveredWidgetList widgets, int x, int y) {
        if (getArea().isInside(x, y)) {
            int tx = x - getArea().x();
            int ty = y - getArea().y();
            if (isInsideScrollbarArea(tx, ty)) {
                if (verticalBar.getArea().isInside(tx, ty))
                    widgets.add(this.verticalBar, stack.peek());
                if (horizontalBar.getArea().isInside(tx, ty))
                    widgets.add(this.horizontalBar, stack.peek());
            } else if (hasChildren()) {
                IViewport.getChildrenAt(this, stack, widgets, x, y);
            }
        }
    }

    public boolean isInsideScrollbarArea(int x, int y) {
        return this.horizontalBar.getArea().isInside(x, y) || this.verticalBar.getArea().isInside(x, y);
    }

    @Override
    public void onResized() {
        super.onResized();
        this.horizontalBar.onResized();
        this.verticalBar.onResized();
//        if (this.scroll.getScrollX() != null) {
//            this.scroll.getScrollX().clamp(this.scroll);
//            if (!this.keepScrollBarInArea) {
//                getArea().height += this.scroll.getScrollX().getThickness();
//            }
//        }
//        if (this.scroll.getScrollY() != null) {
//            this.scroll.getScrollY().clamp(this.scroll);
//            if (!this.keepScrollBarInArea) {
//                getArea().width += this.scroll.getScrollY().getThickness();
//            }
//        }
    }

    @Override
    public boolean canHover() {
        return super.canHover() || this.isInsideScrollbarArea(getContext().getMouseX(), getContext().getMouseY());
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        ModularGuiContext context = getContext();
//        if (this.scroll.mouseClicked(context)) {
//            return Result.STOP;
//        }
//        if (this.horizontalBar.mouseClicked(context) || this.verticalBar.mouseClicked(context)) {
//            return Result.STOP;
//        }
        return Result.IGNORE;
    }

    @Override
    public boolean onMouseScroll(ModularScreen.UpOrDown scrollDirection, int amount) {
//        return this.horizontalBar.onMouseScroll(scrollDirection, amount);
//        return this.verticalBar.onMouseScroll(scrollDirection, amount);
//        return this.scroll.mouseScroll(getContext());
        return false;
    }

    @Override
    public boolean onMouseRelease(int mouseButton) {
//        this.scroll.mouseReleased(getContext());
//        this.horizontalBar.onMouseRelease(mouseButton);
//        this.verticalBar.onMouseRelease(mouseButton);
        return false;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        this.horizontalBar.drag(getContext().getAbsMouseX(), getContext().getAbsMouseY());
        this.verticalBar.drag(getContext().getAbsMouseX(), getContext().getAbsMouseY());
//        this.scroll.drag(getContext().getAbsMouseX(), getContext().getAbsMouseY());
    }

    @Override
    public void preDraw(ModularGuiContext context, boolean transformed) {
        if (!transformed) {
            Stencil.applyAtZero(getArea(), context);
        }
    }

    @Override
    public void postDraw(ModularGuiContext context, boolean transformed) {
        if (!transformed) {
            Stencil.remove();
            this.horizontalBar.draw(context, getWidgetTheme(context.getTheme()));
            this.verticalBar.draw(context, getWidgetTheme(context.getTheme()));
//            this.scroll.drawScrollbar();
        }
    }

    public int getScrollX() {
        return this.horizontalBar.getScroll();
    }

    public int getScrollY() {
        return this.verticalBar.getScroll();
    }

    public W setScrollSize(int x, int y) {
        return setHorizontalScrollSize(x)
                .setVerticalScrollSize(y);
    }

    public W setHorizontalScrollSize(int x) {
        this.horizontalBar.setScrollSize(x);
        return getThis();
    }

    public W setVerticalScrollSize(int y) {
        this.verticalBar.setScrollSize(y);
        return getThis();
    }

    public boolean isDraggingScroll() {
        return horizontalBar.isDragging() || verticalBar.isDragging();
    }

    public boolean isScrollActive(GuiAxis axis) {
        return axis.isHorizontal() ? horizontalBar.isActive() : verticalBar.isActive();
    }

    /**
     * Sets whether the scroll bar should be kept inside the area of this widget, which might cause it to overlap with the content of this widget.
     * By setting the value to false, the size of this widget is expanded by the thickness of the scrollbars after the tree is resized.
     * Default: false
     *
     * @param value if the scroll bar should be kept inside the widgets area
     * @return this
     */
    public W keepScrollBarInArea(boolean value) {
        this.keepScrollBarInArea = value;
        return getThis();
    }

    public W keepScrollBarInArea() {
        return keepScrollBarInArea(true);
    }
}
