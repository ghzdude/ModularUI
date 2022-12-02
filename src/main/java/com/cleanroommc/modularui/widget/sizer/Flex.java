package com.cleanroommc.modularui.widget.sizer;

import com.cleanroommc.modularui.GuiError;
import com.cleanroommc.modularui.api.widget.IGuiElement;
import com.cleanroommc.modularui.api.widget.IVanillaSlot;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.utils.Alignment;
import net.minecraft.inventory.Slot;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

public class Flex implements IResizeable {

    private final DimensionSizer x = new DimensionSizer(GuiAxis.X);
    private final DimensionSizer y = new DimensionSizer(GuiAxis.Y);
    private boolean expanded = false;
    private final IGuiElement parent;
    private Area relativeTo;
    private boolean relativeToParent = true;
    private boolean skip = false;

    public Flex(IGuiElement parent) {
        this.parent = parent;
    }

    public void reset() {
        x.reset();
        y.reset();
    }

    public Flex startDefaultMode() {
        this.x.setDefaultMode(true);
        this.y.setDefaultMode(true);
        return this;
    }

    public Flex endDefaultMode() {
        this.x.setDefaultMode(false);
        this.y.setDefaultMode(false);
        return this;
    }

    public Flex coverChildrenWidth() {
        this.x.setCoverChildren(true);
        return this;
    }

    public Flex coverChildrenHeight() {
        this.y.setCoverChildren(true);
        return this;
    }

    public Flex cancelMovementX() {
        this.x.setCancelAutoMovement(true);
        return this;
    }

    public Flex cancelMovementY() {
        this.y.setCancelAutoMovement(true);
        return this;
    }

    public Flex expanded() {
        this.expanded = true;
        return this;
    }

    public Flex coverChildren() {
        return coverChildrenWidth().coverChildrenHeight();
    }

    public Flex relative(IGuiElement guiElement) {
        return relative(guiElement.getArea());
    }

    public Flex relative(Area guiElement) {
        this.relativeTo = guiElement;
        this.relativeToParent = false;
        return this;
    }

    public Flex relativeToScreen() {
        this.relativeTo = null;
        this.relativeToParent = false;
        return this;
    }

    public Flex relativeToParent() {
        this.relativeToParent = true;
        return this;
    }

    public Flex left(int x) {
        return left(x, 0, 0, Unit.Measure.PIXEL, true);
    }

    public Flex left(float x) {
        return left(x, 0, 0, Unit.Measure.RELATIVE, true);
    }

    public Flex left(float x, int offset) {
        return left(x, offset, 0, Unit.Measure.RELATIVE, true);
    }

    public Flex left(float x, float anchor) {
        return left(x, 0, anchor, Unit.Measure.RELATIVE, false);
    }

    public Flex left(float x, int offset, float anchor, Unit.Measure measure) {
        return left(x, offset, anchor, measure, false);
    }

    @ApiStatus.Internal
    public Flex left(float x, int offset, float anchor, Unit.Measure measure, boolean autoAnchor) {
        return unit(getLeft(), x, offset, anchor, measure, autoAnchor);
    }

    public Flex right(int x) {
        return right(x, 0, 0, Unit.Measure.PIXEL, true);
    }

    public Flex right(float x) {
        return right(x, 0, 0, Unit.Measure.RELATIVE, true);
    }

    public Flex right(float x, int offset) {
        return right(x, offset, 0, Unit.Measure.RELATIVE, true);
    }

    public Flex right(float x, float anchor) {
        return right(x, 0, anchor, Unit.Measure.RELATIVE, false);
    }

    public Flex right(float x, int offset, float anchor, Unit.Measure measure) {
        return right(x, offset, anchor, measure, false);
    }

    @ApiStatus.Internal
    public Flex right(float x, int offset, float anchor, Unit.Measure measure, boolean autoAnchor) {
        return unit(getRight(), x, offset, anchor, measure, autoAnchor);
    }

    private Flex unit(Unit u, float val, int offset, float anchor, Unit.Measure measure, boolean autoAnchor) {
        u.setValue(val);
        u.setMeasure(measure);
        u.setOffset(offset);
        u.setAnchor(anchor);
        u.setAutoAnchor(autoAnchor);
        return this;
    }

    public Flex top(int y) {
        return top(y, 0, 0, Unit.Measure.PIXEL, true);
    }

    public Flex top(float y) {
        return top(y, 0, 0, Unit.Measure.RELATIVE, true);
    }

    public Flex top(float y, int offset) {
        return top(y, offset, 0, Unit.Measure.RELATIVE, true);
    }

    public Flex top(float y, float anchor) {
        return top(y, 0, anchor, Unit.Measure.RELATIVE, false);
    }

    public Flex top(float y, int offset, float anchor, Unit.Measure measure) {
        return top(y, offset, anchor, measure, false);
    }

    @ApiStatus.Internal
    public Flex top(float y, int offset, float anchor, Unit.Measure measure, boolean autoAnchor) {
        return unit(getTop(), y, offset, anchor, measure, autoAnchor);
    }

    public Flex bottom(int y) {
        return bottom(y, 0, 0, Unit.Measure.PIXEL, true);
    }

    public Flex bottom(float y) {
        return bottom(y, 0, 0, Unit.Measure.RELATIVE, true);
    }

    public Flex bottom(float y, int offset) {
        return bottom(y, offset, 0, Unit.Measure.RELATIVE, true);
    }

    public Flex bottom(float y, float anchor) {
        return bottom(y, 0, anchor, Unit.Measure.RELATIVE, false);
    }

    public Flex bottom(float y, int offset, float anchor, Unit.Measure measure) {
        return bottom(y, offset, anchor, measure, false);
    }

    @ApiStatus.Internal
    public Flex bottom(float y, int offset, float anchor, Unit.Measure measure, boolean autoAnchor) {
        return unit(getBottom(), y, offset, anchor, measure, autoAnchor);
    }

    public Flex pos(int x, int y) {
        return left(x).top(y);
    }

    public Flex pos(float x, float y) {
        return left(x).top(y);
    }

    public Flex width(int w) {
        return width(w, Unit.Measure.PIXEL);
    }

    public Flex width(float w) {
        return width(w, Unit.Measure.RELATIVE);
    }

    public Flex width(float w, Unit.Measure measure) {
        return unitSize(getWidth(), w, measure);
    }

    public Flex height(int h) {
        return height(h, Unit.Measure.PIXEL);
    }

    public Flex height(float h) {
        return height(h, Unit.Measure.RELATIVE);
    }

    public Flex height(float h, Unit.Measure measure) {
        return unitSize(getHeight(), h, measure);
    }

    private Flex unitSize(Unit u, float val, Unit.Measure measure) {
        u.setValue(val);
        u.setMeasure(measure);
        return this;
    }

    public Flex size(int w, int h) {
        return width(w).height(h);
    }

    public Flex size(float w, float h) {
        return width(w).height(h);
    }

    public Flex anchorLeft(float val) {
        getLeft().setAnchor(val);
        getLeft().setAutoAnchor(false);
        return this;
    }

    public Flex anchorRight(float val) {
        getRight().setAnchor(1 - val);
        getRight().setAutoAnchor(false);
        return this;
    }

    public Flex anchorTop(float val) {
        getTop().setAnchor(val);
        getTop().setAutoAnchor(false);
        return this;
    }

    public Flex anchorBottom(float val) {
        getBottom().setAnchor(1 - val);
        getBottom().setAutoAnchor(false);
        return this;
    }

    public Flex anchor(Alignment alignment) {
        if (this.x.hasStart()) {
            anchorLeft(alignment.x);
        }
        if (this.x.hasEnd()) {
            anchorRight(alignment.x);
        }
        if (this.y.hasStart()) {
            anchorTop(alignment.y);
        }
        if (this.y.hasEnd()) {
            anchorBottom(alignment.y);
        }
        return this;
    }

    public Flex alignX(float val) {
        return left(val).anchorLeft(val);
    }

    public Flex alignY(float val) {
        return top(val).anchorTop(val);
    }

    public Flex align(Alignment alignment) {
        alignX(alignment.x);
        alignY(alignment.y);
        return this;
    }

    private Area getRelativeTo() {
        Area relativeTo = relativeToParent ? parent.getParentArea() : this.relativeTo;
        return relativeTo != null ? relativeTo : this.parent.getScreen().getViewport();
    }

    public boolean isExpanded() {
        return expanded;
    }

    public boolean hasYPos() {
        return this.y.hasPos();
    }

    public boolean hasXPos() {
        return this.x.hasPos();
    }

    public boolean hasHeight() {
        return this.y.hasSize();
    }

    public boolean hasWidth() {
        return this.x.hasSize();
    }

    @ApiStatus.Internal
    public void skip() {
        this.skip = true;
    }

    @Override
    public boolean isSkip() {
        return skip;
    }

    @Override
    public void apply(IGuiElement guiElement) {
        if (isSkip()) return;
        Area relativeTo = getRelativeTo();

        if (relativeTo.z() >= parent.getArea().z()) {
            GuiError.throwNew(this.parent, GuiError.Type.SIZING, "Widget can't be relative to a widget at the same level or above");
            return;
        }

        // check if children area is required for this widget
        if (this.x.dependsOnChildren() || this.y.dependsOnChildren()) {
            if (!(this.parent instanceof IWidget)) {
                throw new IllegalStateException("Can only cover children if instance of IWidget");
            }

            IWidget widget = (IWidget) this.parent;

            List<IWidget> children = widget.getChildren();
            if (!children.isEmpty()) {
                for (IWidget child : children) {
                    if (dependsOnThis(child, null)) {
                        // children area requires this area to be calculated and will be skipped
                        child.flex().skip();
                    }
                }
            }
        }

        // calculate x, y, width and height if possible
        this.x.apply(guiElement.getArea(), relativeTo, guiElement::getDefaultWidth);
        this.y.apply(guiElement.getArea(), relativeTo, guiElement::getDefaultHeight);
    }

    @Override
    public void postApply(IGuiElement guiElement) {
        // not skipped children are now calculated and now this area can be calculated if it requires childrens area
        if (this.x.dependsOnChildren() || this.y.dependsOnChildren()) {
            List<IWidget> children = ((IWidget) parent).getChildren();
            if (!children.isEmpty()) {
                int moveChildrenX = 0, moveChildrenY = 0;

                Box padding = this.parent.getArea().getPadding();
                // first calculate the area the children span
                int x0 = Integer.MAX_VALUE, x1 = Integer.MIN_VALUE, y0 = Integer.MAX_VALUE, y1 = Integer.MIN_VALUE;
                for (IWidget child : children) {
                    Box margin = child.getArea().getMargin();
                    Flex flex = child.flex();
                    Area area = child.getArea();
                    if (this.x.dependsOnChildren() && !dependsOnThis(child, GuiAxis.X)) {
                        x0 = Math.min(x0, area.rx - padding.left - margin.left);
                        x1 = Math.max(x1, area.rx + area.width + padding.right + margin.right);
                    }
                    if (this.y.dependsOnChildren() && !dependsOnThis(child, GuiAxis.Y)) {
                        y0 = Math.min(y0, area.ry - padding.top - margin.top);
                        y1 = Math.max(y1, area.ry + area.height + padding.bottom + margin.bottom);
                    }
                }

                // now calculate new x, y, width and height based on the childrens area
                Area relativeTo = getRelativeTo();
                if (this.x.dependsOnChildren()) {
                    moveChildrenX = this.x.postApply(this.parent.getArea(), relativeTo, x0, x1);
                }
                if (this.y.dependsOnChildren()) {
                    moveChildrenY = this.y.postApply(this.parent.getArea(), relativeTo, y0, y1);
                }
                for (IWidget widget : children) {
                    if (widget.flex().isSkip()) {
                        // child was skipped before
                        // this area is now calculated and child can now be calculated
                        widget.flex().skip = false;
                        widget.resize();
                    } else {
                        // child was already calculated and now needs to be moved, so that child stays in the same spot and doesn't get moved with the whole parent
                        Area area = widget.getArea();
                        area.rx += moveChildrenX;
                        area.ry += moveChildrenY;
                    }
                }
            }
        }
    }

    @Override
    public void applyPos(IGuiElement parent) {
        // after all widgets x, y, width and height have been calculated when can now calculate the absolute position
        Area relativeTo = getRelativeTo();
        parent.getArea().applyPos(relativeTo.x, relativeTo.y);
        if (parent instanceof IVanillaSlot) {
            Slot slot = ((IVanillaSlot) parent).getVanillaSlot();
            slot.xPos = parent.getArea().x;
            slot.yPos = parent.getArea().y;
        }
    }

    private boolean dependsOnThis(IWidget child, GuiAxis axis) {
        Flex flex = child.getFlex();
        if (flex == null || flex.getRelativeTo() != this.parent.getArea()) return false;
        return axis == null ? flex.x.dependsOnParent() || flex.y.dependsOnParent() : axis == GuiAxis.X ? flex.x.dependsOnParent() : flex.y.dependsOnParent();
    }

    private Unit getLeft() {
        return this.x.getStart();
    }

    private Unit getRight() {
        return this.x.getEnd();
    }

    private Unit getTop() {
        return this.y.getStart();
    }

    private Unit getBottom() {
        return this.y.getEnd();
    }

    private Unit getWidth() {
        return this.x.getSize();
    }

    private Unit getHeight() {
        return this.y.getSize();
    }
}