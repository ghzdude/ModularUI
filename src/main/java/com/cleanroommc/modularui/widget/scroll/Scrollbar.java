package com.cleanroommc.modularui.widget.scroll;

import com.cleanroommc.modularui.animation.Animator;
import com.cleanroommc.modularui.api.GuiAxis;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.layout.IViewport;
import com.cleanroommc.modularui.api.layout.IViewportStack;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.HoveredWidgetList;
import com.cleanroommc.modularui.utils.Interpolation;
import com.cleanroommc.modularui.utils.MathUtils;
import com.cleanroommc.modularui.widget.Widget;

import com.cleanroommc.modularui.widget.WidgetTree;
import com.cleanroommc.modularui.widget.sizer.Area;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class Scrollbar extends Widget<Scrollbar> implements Interactable, IViewport {

    public static final int DEFAULT_THICKNESS = 4;

    private final Supplier<Area> parentArea;
    @NotNull private GuiAxis axis;
    private boolean axisStart;
    private int thickness = DEFAULT_THICKNESS;
    private int scrollSpeed = 30;
    private boolean cancelScrollEdge = true;

//    private int scrollSize;
    private int scroll;
    protected boolean dragging;
    protected int clickOffset;

    private int animatingTo = 0;
    private final Animator scrollAnimator = new Animator()
            .duration(500)
            .curve(Interpolation.QUAD_OUT);

    private boolean keepScrollBarInArea;

    public Scrollbar(Supplier<Area> parentArea, GuiAxis axis) {
        this.parentArea = parentArea;
        this.axis = axis;
    }

    public static Scrollbar horizontal(Supplier<Area> parentArea) {
        return new Scrollbar(parentArea, GuiAxis.X);
    }

    public static Scrollbar vertical(Supplier<Area> parentArea) {
        return new Scrollbar(parentArea, GuiAxis.Y);
    }

    public static int getScrollbarStart(Scrollbar scrollbar) {
        int fullSize = scrollbar.getScrollSize();
        int visible = getVisibleSize(scrollbar);
        int l = getScrollbarLength(scrollbar);
        return ((fullSize - l) * scrollbar.getScroll()) / (fullSize - visible);
    }

    public static int getScrollbarLength(Scrollbar scrollbar) {
        int length = (int) (getVisibleSize(scrollbar) / (float) scrollbar.getScrollSize());
        return Math.max(length, 4); // min length of 4
    }

    public static int getVisibleSize(Scrollbar scrollbar) {
        return scrollbar.getParentArea().getSize(scrollbar.getAxis());
    }

    @Override
    public boolean onMouseRelease(int mouseButton) {
        if (isActive()) {
            dragging = false;
            clickOffset = 0;
        }
        return false;
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        if (mouseClicked(getContext())) {
            return Result.ACCEPT;
        }
        return Result.IGNORE;
    }

    @Override
    public boolean onMouseScroll(ModularScreen.UpOrDown scrollDirection, int amount) {
        ModularGuiContext context = getContext();
        return mouseScroll(context.getAbsMouseX(), context.getAbsMouseY(), amount * scrollDirection.modifier);
    }

    /**
     * This method should be invoked when mouse wheel is scrolling
     */
    public boolean mouseScroll(int x, int y, int scroll) {
        int scrollAmount = (int) Math.copySign(getScrollSpeed(), scroll);
        int scrollTo;
        if (isAnimating()) {
            scrollTo = getAnimatingTo() - scrollAmount;
        } else {
            scrollTo = getScroll() - scrollAmount;
        }

        // simulate scroll to determine whether event should be canceled
        int oldScroll = getScroll();

        boolean changed = scrollTo(getArea(), scrollTo);;
        scrollTo(getArea(), oldScroll);
        if (changed) {
            animateTo(getArea(), scrollTo);
            return true;
        }
        return isCancelScrollEdge();
    }

    /* GUI code for easier manipulations */

    public boolean mouseClicked(GuiContext context) {
        return this.mouseClicked(context.getAbsMouseX(), context.getAbsMouseY());
    }

    /**
     * This method should be invoked to register dragging
     */
    public boolean mouseClicked(int x, int y) {
        if (isActive() && getArea().isInside(x, y)) {
//            int crossAxis = isHorizontal() ? y : x;
            int mainAxis = isHorizontal() ? x : y;
            Area parent = getParentArea();
            this.dragging = true;
            this.clickOffset = mainAxis;

            int scrollBarSize = getScrollbarStart(this);
            int start = getScrollbarStart(this);
            int areaStart = parent.getPoint(getAxis());
            boolean clickInsideBar = mainAxis >= areaStart + start && mainAxis <= areaStart + start + scrollBarSize;

            if (clickInsideBar) {
                this.clickOffset = x - areaStart - start; // relative click position inside bar
            } else {
                this.clickOffset = scrollBarSize / 2; // assume click position in center of bar
            }

            return true;
        }
        return false;
    }

    @Override
    public void onResized() {
        super.onResized();
        Area parent = getParentArea();
        Area area = getArea();

        GuiAxis mainAxis = getAxis();
        GuiAxis crossAxis = mainAxis.getOther();

        area.setSize(mainAxis, parent.getSize(mainAxis));
        area.setSize(crossAxis, this.thickness);

        int start = isOnAxisStart() ? 0 : parent.getSize(crossAxis) - parent.getPadding().getEnd(crossAxis);
        int cross = keepScrollBarInArea ? start - getThickness() : start;
        area.setPoint(crossAxis, cross);
        area.setPoint(mainAxis, 0);

        this.clamp(parent);
//        if (this.keepScrollBarInArea) return;
//
//
//        // todo this doesn't work
//        if (isHorizontal()) {
//            area.y += this.getThickness();
//        } else {
//            area.x += this.getThickness();
//        }
    }

    @SideOnly(Side.CLIENT)
    public void drag(GuiContext context) {
        if (canDrag()) drag(context.getMouse(getAxis()));
    }

    /**
     * This should be invoked in a drawing or and update method. It's
     * responsible for scrolling through this view when dragging.
     */
    public void drag(int x, int y) {
        if (canDrag()) drag(isHorizontal() ? x : y);
    }

    public void drag(int m) {
        if (canDrag()) {
            float progress = getProgress(getParentArea(), m);
            this.scrollTo(getParentArea(), (int) (progress * (getScrollSize() - getVisibleSize(getParentArea()))));
        }
    }

    public float getProgress(Area area, int m) {
        float fullSize = (float) area.getSize(getAxis());
        float progress = (m - area.getPoint(getAxis()) - clickOffset) / (fullSize - getScrollBarLength(area));
        return MathUtils.clamp(progress, 0f, 1f);
    }

    public int getScrollBarLength(Area area) {
        int length = (int) (getVisibleSize(area) / (float) getScrollSize());
        return Math.max(length, 4); // min length of 4
    }

    public final int getVisibleSize(Area area) {
        return Math.max(0, area.getSize(getAxis()) - area.getPadding().getTotal(getAxis()));
    }

    public final boolean canDrag() {
        return isActive() && !dragging;
    }

    public int getScroll() {
        return isActive() ? 0 : this.scroll;
    }

    /**
     * Scroll to the position in the scroll area
     */
    public boolean scrollTo(Area area, int scroll) {
        if (!isActive()) return false;
        int old = this.scroll;
        this.scroll = scroll;
        clamp(area);
        return this.scroll != old;
    }

    /**
     * Clamp scroll to the bounds of the scroll size;
     */
    public boolean clamp(Area area) {
        if (!isActive()) return false;
        int size = getVisibleSize(area);

        int old = this.scroll;
        if (this.getScrollSize() <= size) {
            this.scroll = 0;
        } else {
            this.scroll = MathHelper.clamp(this.scroll, 0, getScrollSize() - size);
        }
        return old != this.scroll; // returns true if the area was clamped
    }

    public boolean isVertical() {
        return this.getAxis().isVertical();
    }

    public boolean isHorizontal() {
        return this.getAxis().isHorizontal();
    }

    /**
     * Determines if scrolling of widgets below should still be canceled if this scroll view
     * has hit the end and is currently not scrolling.
     * Most of the time this should be true
     *
     * @return true if scrolling should be canceled even when this view hit an edge
     */
    public boolean isCancelScrollEdge() {
        return cancelScrollEdge;
    }

    public void setCancelScrollEdge(boolean cancelScrollEdge) {
        this.cancelScrollEdge = cancelScrollEdge;
    }

    public boolean isAnimating() {
        return this.scrollAnimator.isAnimating();
    }

    public int getAnimationDirection() {
        if (!isAnimating()) return 0;
        return this.scrollAnimator.getMax() >= this.scrollAnimator.getMin() ? 1 : -1;
    }

    public int getAnimatingTo() {
        return this.animatingTo;
    }

    public void animateTo(Area area, int x) {
        this.scrollAnimator.bounds(this.scroll, x).onUpdate(value -> {
            if (scrollTo(area, (int) value)) {
                this.scrollAnimator.stop(false); // stop animation once an edge is hit
            }
        });
        this.scrollAnimator.reset();
        this.scrollAnimator.animate();
        this.animatingTo = x;
    }

    public @NotNull GuiAxis getAxis() {
        return this.axis;
    }

    public boolean isOnAxisStart() {
        return this.axisStart;
    }

    public int getThickness() {
        return getArea().getSize(getAxis().getOther());
    }

    public int getScrollSpeed() {
        return this.scrollSpeed;
    }

    public void setScrollSpeed(int scrollSpeed) {
        this.scrollSpeed = scrollSpeed;
    }

    @Override
    public void draw(ModularGuiContext context, WidgetTheme widgetTheme) {
        if (!isActive()) return;
        Area parent = getParentArea();
        Area area = getArea();
        int l = getScrollbarLength(this);
        int p = getScrollbarStart(this);
        int x = area.x();
        int y = area.y();

        if (isHorizontal()) {
            GuiDraw.drawRect(x, y, parent.w(), getThickness(), Color.withAlpha(Color.BLACK.main, 0.25f));
            drawScrollBar(p, y, l, getThickness());
        } else {
            GuiDraw.drawRect(x, y, getThickness(), parent.h(), Color.withAlpha(Color.BLACK.main, 0.25f));
            drawScrollBar(p, y, getThickness(), l);
        }
    }

    @SideOnly(Side.CLIENT)
    protected static void drawScrollBar(int x, int y, int w, int h) {
        GuiDraw.drawRect(x, y, w, h, 0xffeeeeee);
        GuiDraw.drawRect(x + 1, y + 1, w - 1, h - 1, 0xff666666);
        GuiDraw.drawRect(x + 1, y + 1, w - 2, h - 2, 0xffaaaaaa);
    }

    public int getScrollSize() {
        return getArea().getSize(getAxis());
    }

    public void setScrollSize(int size) {
        this.getArea().setSize(getAxis(), size);
    }

    public boolean isDragging() {
        return isActive() && dragging;
    }

    public boolean isActive() {
        return getScrollSize() > getParentArea().getSize(axis);
    }

    @Override
    public Area getParentArea() {
        return this.parentArea.get();
    }

    @Override
    public void getSelfAt(IViewportStack stack, HoveredWidgetList widgets, int x, int y) {
        if (isInside(stack, x, y)) {
            widgets.add(this, stack.peek());
        }
    }

    @Override
    public void getWidgetsAt(IViewportStack stack, HoveredWidgetList widgets, int x, int y) {

    }
}
