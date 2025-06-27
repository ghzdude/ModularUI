package com.cleanroommc.modularui.widget.scroll;

import com.cleanroommc.modularui.animation.Animator;
import com.cleanroommc.modularui.api.GuiAxis;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.Interpolation;
import com.cleanroommc.modularui.utils.MathUtils;
import com.cleanroommc.modularui.widget.Widget;

import com.cleanroommc.modularui.widget.sizer.Area;

import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class Scrollbar extends Widget<Scrollbar> implements Interactable {

    public static final int DEFAULT_THICKNESS = 4;

    private final Supplier<Area> parentArea;
    @NotNull private GuiAxis axis;
    private boolean axisStart;
    private int thickness = DEFAULT_THICKNESS;
    private boolean active = false;
    private int scrollSpeed = 30;
    private boolean cancelScrollEdge = true;

    private int scrollSize;
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
        return Interactable.super.onMousePressed(mouseButton);
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
        if (isActive()) {
            Area area = getParentArea();
            int crossAxis = axis.isHorizontal() ? y : x;
            int mainAxis = axis.isHorizontal() ? x : y;

            if (isOnAxisStart() && crossAxis <= area.getPoint(this.axis.getOther()) + getThickness() ||
                    !isOnAxisStart() && crossAxis >= area.getEndPoint(this.axis.getOther()) - getThickness()) {
                this.dragging = true;
                this.clickOffset = mainAxis;

                int scrollBarSize = getScrollBarLength(area);
                int start = getScrollBarStart(area, scrollBarSize);
                int areaStart = area.getPoint(this.axis);
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
        area.setSize(crossAxis, getThickness());

        int start = isOnAxisStart() ? 0 : parent.getSize(crossAxis) - parent.getPadding().getEnd(crossAxis);
        area.setPoint(crossAxis, start - getThickness());
        area.setPoint(mainAxis, 0);

        this.clamp(parent);
        if (this.keepScrollBarInArea) return;


        // todo this doesn't work
//        if (this.getAxis().isHorizontal()) {
//            area.y += this.getThickness();
//        } else {
//            area.x += this.getThickness();
//        }
    }

    @SideOnly(Side.CLIENT)
    public void drag(GuiContext context) {
        if (canDrag())
            this.drag(context.getMouse(axis));
    }

    /**
     * This should be invoked in a drawing or and update method. It's
     * responsible for scrolling through this view when dragging.
     */
    public void drag(int x, int y) {
        if (canDrag())
            drag(axis.isHorizontal() ? x : y);
    }

    public void drag(int m) {
        if (canDrag()) {
            float progress = getProgress(getParentArea(), m);
            this.scrollTo(getParentArea(), (int) (progress * (getScrollSize() - getVisibleSize(getParentArea()))));
        }
    }

    public float getProgress(Area area, int mx, int my) {
        return getProgress(area, axis.isHorizontal() ? mx : my);
    }

    public float getProgress(Area area, int m) {
        float fullSize = (float) area.getSize(this.axis);
        float progress = (m - area.getPoint(this.axis) - clickOffset) / (fullSize - getScrollBarLength(area));
        return MathUtils.clamp(progress, 0f, 1f);
    }

    public int getScrollBarLength(Area area) {
        int length = (int) (getVisibleSize(area) / (float) this.scrollSize);
        return Math.max(length, 4); // min length of 4
    }

    public final int getVisibleSize(Area area) {
        return Math.max(0, area.getSize(this.axis) - area.getPadding().getTotal(this.axis));
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
        if (isActive())
            this.scroll = scroll;
        return clamp(area);
    }

    /**
     * Clamp scroll to the bounds of the scroll size;
     */
    public boolean clamp(Area area) {
        if (!isActive()) return false;
        int size = getVisibleSize(area);

        int old = this.scroll;
        if (this.scrollSize <= size) {
            this.scroll = 0;
        } else {
            this.scroll = MathHelper.clamp(this.scroll, 0, this.scrollSize - size);
        }
        return old != this.scroll; // returns true if the area was clamped
    }

    public int getScrollBarStart(Area area, int scrollBarLength, int fullVisibleSize) {
        return ((fullVisibleSize - scrollBarLength) * getScroll()) / (getScrollSize() - getVisibleSize(area));
    }

    public int getScrollBarStart(Area area, int scrollBarLength) {
        return getScrollBarStart(area, scrollBarLength, area.getSize(this.axis));
    }

    public boolean isVertical() {
        return this.axis.isVertical();
    }

    public boolean isHorizontal() {
        return this.axis.isHorizontal();
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

    public GuiAxis getAxis() {
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
        int l = getScrollBarLength(parent);
        int p = getScrollBarStart(parent, l, parent.getSize(this.axis));
        int x = area.x();
        int y = area.y();

        if (axis.isHorizontal()) {
            GuiDraw.drawRect(x, y, parent.w(), getThickness(), Color.withAlpha(Color.BLACK.main, 0.25f));
            drawScrollBar(p, y, l, getThickness());
        } else {
            GuiDraw.drawRect(x, y, getThickness(), parent.h(), Color.withAlpha(Color.BLACK.main, 0.25f));
            drawScrollBar(p, y, getThickness(), l);
        }
    }

    @SideOnly(Side.CLIENT)
    protected void drawScrollBar(int x, int y, int w, int h) {
        GuiDraw.drawRect(x, y, w, h, 0xffeeeeee);
        GuiDraw.drawRect(x + 1, y + 1, w - 1, h - 1, 0xff666666);
        GuiDraw.drawRect(x + 1, y + 1, w - 2, h - 2, 0xffaaaaaa);
    }

    public int getScrollSize() {
        return this.scrollSize;
    }

    public void setScrollSize(int size) {
        this.active = size > getVisibleSize(getParentArea());
        this.scrollSize = size;
    }

    public boolean isDragging() {
        return isActive() && dragging;
    }

    public boolean isActive() {
        return active && scrollSize > getParentArea().getSize(axis);
    }

    @Override
    public Area getParentArea() {
        return this.parentArea.get();
    }
}
