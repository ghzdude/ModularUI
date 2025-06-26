package com.cleanroommc.modularui.widget.scroll;

import com.cleanroommc.modularui.animation.Animator;
import com.cleanroommc.modularui.api.GuiAxis;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.Interpolation;

import com.cleanroommc.modularui.utils.MathUtils;
import com.cleanroommc.modularui.widget.sizer.Area;

import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.Nullable;

public abstract class ScrollData implements IDrawable {

    /**
     * Creates scroll data which handles scrolling and scroll bar. Scrollbar is 4 pixel thick
     * and will be at the end of the cross axis (bottom/right).
     *
     * @param axis      axis on which to scroll
     * @return new scroll data
     */
    public static ScrollData of(GuiAxis axis) {
        return of(axis, false, DEFAULT_THICKNESS);
    }

    /**
     * Creates scroll data which handles scrolling and scroll bar. Scrollbar is 4 pixel thick.
     *
     * @param axis      axis on which to scroll
     * @param axisStart if the scroll bar should be at the start of the cross axis (left/top)
     * @return new scroll data
     */
    public static ScrollData of(GuiAxis axis, boolean axisStart) {
        return of(axis, axisStart, DEFAULT_THICKNESS);
    }

    /**
     * Creates scroll data which handles scrolling and scroll bar.
     *
     * @param axis      axis on which to scroll
     * @param axisStart if the scroll bar should be at the start of the cross axis (left/top)
     * @param thickness cross axis thickness of the scroll bar in pixel
     * @return new scroll data
     */
    public static ScrollData of(GuiAxis axis, boolean axisStart, int thickness) {
        if (axis.isHorizontal()) return new HorizontalScrollData(axisStart, thickness);
        return new VerticalScrollData(axisStart, thickness);
    }

    public static final int DEFAULT_THICKNESS = 4;

    private final GuiAxis axis;
    private final boolean axisStart;
    private final int thickness;
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

    protected ScrollData(GuiAxis axis, boolean axisStart, int thickness) {
        this.axis = axis;
        this.axisStart = axisStart;
        this.thickness = thickness <= 0 ? 4 : thickness;
    }

    public GuiAxis getAxis() {
        return this.axis;
    }

    public boolean isOnAxisStart() {
        return this.axisStart;
    }

    public int getThickness() {
        return this.thickness;
    }

    public int getScrollSpeed() {
        return this.scrollSpeed;
    }

    public void setScrollSpeed(int scrollSpeed) {
        this.scrollSpeed = scrollSpeed;
    }

    public int getScrollSize() {
        return this.scrollSize;
    }

    public void setScrollSize(int scrollSize) {
        this.scrollSize = scrollSize;
    }

    public int getScroll() {
        return this.scroll;
    }

    public boolean isDragging() {
        return this.dragging;
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

    protected final int getRawVisibleSize(ScrollArea area) {
        return Math.max(0, getRawFullVisibleSize(area) - area.getPadding().getTotal(this.axis));
    }

    protected final int getRawFullVisibleSize(ScrollArea area) {
        return area.getSize(this.axis);
    }

    public final int getFullVisibleSize(ScrollArea area) {
        return getFullVisibleSize(area, false);
    }

    public final int getFullVisibleSize(Area area) {
        return area.getSize(this.axis);
    }

    public final int getFullVisibleSize(ScrollArea area, boolean isOtherActive) {
        int s = getRawFullVisibleSize(area);
        ScrollData data = getOtherScrollData(area);
        if (data != null && (isOtherActive || data.isScrollBarActive(area, true))) {
            s -= data.getThickness();
        }
        return s;
    }

    public final int getVisibleSize(ScrollArea area) {
        return getVisibleSize(area, false);
    }

    public final int getVisibleSize(Area area) {
        return getVisibleSize(area, area.getSize(this.axis));
    }

    public final int getVisibleSize(ScrollArea area, int fullVisibleSize) {
        return Math.max(0, fullVisibleSize - area.getPadding().getTotal(this.axis));
    }

    public final int getVisibleSize(Area area, int fullVisibleSize) {
        return Math.max(0, fullVisibleSize - area.getPadding().getTotal(this.axis));
    }

    public final int getVisibleSize(ScrollArea area, boolean isOtherActive) {
        return getVisibleSize(area, getFullVisibleSize(area, isOtherActive));
    }

    public float getProgress(ScrollArea area, int mainAxisPos, int crossAxisPos) {
        float fullSize = (float) getFullVisibleSize(area);
        return (mainAxisPos - area.getPoint(this.axis) - clickOffset) / (fullSize - getScrollBarLength(area));
    }

    public float getProgress(Area area, int mx, int my) {
        int mainAxisPos = this.axis.isHorizontal() ? mx : my;
        float fullSize = (float) getFullVisibleSize(area);
        float progress = (mainAxisPos - area.getPoint(this.axis) - clickOffset) / (fullSize - getScrollBarLength(area));
        return MathUtils.clamp(progress, 0f, 1f);
    }

    @Nullable
    public abstract ScrollData getOtherScrollData(ScrollArea area);

    /**
     * Clamp scroll to the bounds of the scroll size;
     */
    public boolean clamp(ScrollArea area) {
        int size = getVisibleSize(area);

        int old = this.scroll;
        if (this.scrollSize <= size) {
            this.scroll = 0;
        } else {
            this.scroll = MathHelper.clamp(this.scroll, 0, this.scrollSize - size);
        }
        return old != this.scroll; // returns true if the area was clamped
    }

    /**
     * Clamp scroll to the bounds of the scroll size;
     */
    public boolean clamp(Area area) {
        int size = getVisibleSize(area);

        int old = this.scroll;
        if (this.scrollSize <= size) {
            this.scroll = 0;
        } else {
            this.scroll = MathHelper.clamp(this.scroll, 0, this.scrollSize - size);
        }
        return old != this.scroll; // returns true if the area was clamped
    }

    public boolean scrollBy(ScrollArea area, int x) {
        this.scroll += x;
        return clamp(area);
    }

    /**
     * Scroll to the position in the scroll area
     */
    public boolean scrollTo(ScrollArea area, int x) {
        this.scroll = x;
        return clamp(area);
    }

    /**
     * Scroll to the position in the scroll area
     */
    public boolean scrollTo(Area area, int scroll) {
        this.scroll = scroll;
        return clamp(area);
    }

    public void animateTo(ScrollArea area, int x) {
        this.scrollAnimator.bounds(this.scroll, x).onUpdate(value -> {
            if (scrollTo(area, (int) value)) {
                this.scrollAnimator.stop(false); // stop animation once an edge is hit
            }
        });
        this.scrollAnimator.reset();
        this.scrollAnimator.animate();
        this.animatingTo = x;
    }

    public final boolean isScrollBarActive(ScrollArea area) {
        return isScrollBarActive(area, false);
    }

    public final boolean isScrollBarActive(ScrollArea area, boolean isOtherActive) {
        int s = getRawVisibleSize(area);
        if (s < this.scrollSize) return true;
        ScrollData data = getOtherScrollData(area);
        if (data == null || s - data.getThickness() >= this.scrollSize) return false;
        if (isOtherActive || data.isScrollBarActive(area, true)) {
            s -= data.getThickness();
        }
        return s < this.scrollSize;
    }

    public final boolean isOtherScrollBarActive(ScrollArea area, boolean isSelfActive) {
        ScrollData data = getOtherScrollData(area);
        return data != null && data.isScrollBarActive(area, isSelfActive);
    }

    public int getScrollBarLength(ScrollArea area) {
        boolean isOtherActive = isOtherScrollBarActive(area, false);
        int length = (int) (getVisibleSize(area, isOtherActive) * getFullVisibleSize(area, isOtherActive) / (float) this.scrollSize);
        return Math.max(length, 4); // min length of 4
    }

    public int getScrollBarLength(Area area) {
        int length = (int) (getVisibleSize(area) / (float) this.scrollSize);
        return Math.max(length, 4); // min length of 4
    }

    public abstract boolean isInsideScrollbarArea(ScrollArea area, int x, int y);

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

    public int getScrollBarStart(ScrollArea area, int scrollBarLength, int fullVisibleSize) {
        return ((fullVisibleSize - scrollBarLength) * getScroll()) / (getScrollSize() - getVisibleSize(area, fullVisibleSize));
    }

    public int getScrollBarStart(ScrollArea area, int scrollBarLength, boolean isOtherActive) {
        return getScrollBarStart(area, scrollBarLength, getFullVisibleSize(area, isOtherActive));
    }

    public int getScrollBarStart(Area area, int scrollBarLength, int fullVisibleSize) {
        return ((fullVisibleSize - scrollBarLength) * getScroll()) / (getScrollSize() - getVisibleSize(area, fullVisibleSize));
    }

    public int getScrollBarStart(Area area, int scrollBarLength) {
        return getScrollBarStart(area, scrollBarLength, getFullVisibleSize(area));
    }

    @SideOnly(Side.CLIENT)
    public abstract void drawScrollbar(ScrollArea area);

    @SideOnly(Side.CLIENT)
    public void drawScrollbar(WidgetTheme theme, Area area) {
        draw(GuiContext.getDefault(), area, theme);
    }

    @Override
    public void draw(GuiContext context, Area area, WidgetTheme widgetTheme) {
        int l = getScrollBarLength(area);
        int p = getScrollBarStart(area, l, getFullVisibleSize(area));
        int x = 0;
        int y = 0;
        if (axis.isHorizontal()) {
            if (!isOnAxisStart()) {
                y = area.h() - getThickness();
            }
            GuiDraw.drawRect(x, y, area.w(), getThickness(), Color.withAlpha(Color.BLACK.main, 0.25f));
            drawScrollBar(p, y, l, getThickness());
        } else {
            if (!isOnAxisStart()) {
                x = area.w() - getThickness();
            }
            GuiDraw.drawRect(x, y, getThickness(), area.h(), Color.withAlpha(Color.BLACK.main, 0.25f));
            drawScrollBar(p, y, getThickness(), l);
        }
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {}

    protected boolean shouldDraw(int width, int height) {
        int size = axis.isHorizontal() ? width : height;
        return scrollSize > size;
    }

    @SideOnly(Side.CLIENT)
    protected void drawScrollBar(int x, int y, int w, int h) {
        GuiDraw.drawRect(x, y, w, h, 0xffeeeeee);
        GuiDraw.drawRect(x + 1, y + 1, w - 1, h - 1, 0xff666666);
        GuiDraw.drawRect(x + 1, y + 1, w - 2, h - 2, 0xffaaaaaa);
    }

    public boolean onMouseClicked(ScrollArea area, int mainAxisPos, int crossAxisPos, int button) {
        if (isOnAxisStart() ? crossAxisPos <= area.getPoint(this.axis.getOther()) + getThickness() : crossAxisPos >= area.getEndPoint(this.axis.getOther()) - getThickness()) {
            this.dragging = true;
            this.clickOffset = mainAxisPos;

            int scrollBarSize = getScrollBarLength(area);
            int start = getScrollBarStart(area, scrollBarSize, false);
            int areaStart = area.getPoint(this.axis);
            boolean clickInsideBar = mainAxisPos >= areaStart + start && mainAxisPos <= areaStart + start + scrollBarSize;

            if (clickInsideBar) {
                this.clickOffset = mainAxisPos - areaStart - start; // relative click position inside bar
            } else {
                this.clickOffset = scrollBarSize / 2; // assume click position in center of bar
            }

            return true;
        }
        return false;
    }

    public boolean onMouseClicked(Area area, int x, int y) {
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
}
