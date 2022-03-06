package com.cleanroommc.modularui.common.widget;

import com.cleanroommc.modularui.api.ISyncedWidget;
import com.cleanroommc.modularui.api.IWidgetDrawable;
import com.cleanroommc.modularui.api.Interactable;
import com.cleanroommc.modularui.api.drawable.TextFieldRenderer;
import com.cleanroommc.modularui.api.drawable.TextRenderer;
import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.math.Size;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.network.PacketBuffer;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class TextFieldWidget extends Widget implements Interactable, ISyncedWidget {

    // all positive whole numbers
    public static final Pattern NATURAL_NUMS = Pattern.compile("[0-9]*");
    // all positive and negative numbers
    public static final Pattern WHOLE_NUMS = Pattern.compile("-?[0-9]*");
    public static final Pattern DECIMALS = Pattern.compile("[0-9]*(\\.[0-9]*)?");
    public static final Pattern LETTERS = Pattern.compile("[a-zA-Z]*");
    public static final Pattern ANY = Pattern.compile(".*");
    private static final Pattern BASE_PATTERN = Pattern.compile("[A-Za-z0-9\\s_+\\-.,!@#$%^&*();\\\\/|<>\"'\\[\\]?=]");

    private String text = "";
    private int cursor = 0, cursorEnd = 0;
    private Pattern pattern = ANY;
    private final TextFieldRenderer renderer = new TextFieldRenderer(Pos2d.ZERO, 0, 0);
    private final TextRenderer helper = new TextRenderer(Pos2d.ZERO, 0, 0);
    private int cursorTimer = 0;
    private int textColor = TextFieldRenderer.DEFAULT_COLOR;
    private Function<String, String> validator = val -> val;
    private int maxWidth = 80, maxLines = 1;

    public void setCursor(int pos) {
        setCursor(pos, true);
    }

    public void setCursor(int pos, boolean setEnd) {
        this.cursor = Math.max(0, Math.min(text.length(), pos));
        if (setEnd) {
            this.cursorEnd = cursor;
        }
        this.renderer.setCursor(cursor, cursorEnd);
    }

    public void setCursorEnd(int pos) {
        this.cursorEnd = Math.max(0, Math.min(text.length(), pos));
        this.renderer.setCursor(cursor, cursorEnd);
    }

    public void incrementCursor(int amount) {
        setCursor(cursor + amount);
    }

    public boolean hasTextSelected() {
        return cursor != cursorEnd;
    }

    public String getSelectedText() {
        return text.substring(Math.min(cursor, cursorEnd), Math.max(cursor, cursorEnd));
    }

    public void insert(String string) {
        String part1 = text.substring(0, Math.min(cursor, cursorEnd));
        String part2 = text.substring(Math.max(cursor, cursorEnd));
        String newText = part1 + string + part2;
        if (!pattern.matcher(newText).matches() || !canFit(newText)) {
            return;
        }
        text = newText;
        setCursor(part1.length() + string.length());
    }

    public void delete(boolean positive) {
        String part1;
        String part2;
        if (hasTextSelected()) {
            part1 = text.substring(0, Math.min(cursor, cursorEnd));
            part2 = text.substring(Math.max(cursor, cursorEnd));
            text = part1 + part2;
            setCursor(part1.length());
        } else {
            if ((positive && cursor == text.length()) || (!positive && cursor == 0)) {
                return;
            }
            part1 = text.substring(0, cursor - (positive ? 0 : 1));
            part2 = text.substring(cursor + (positive ? 1 : 0));
        }
        text = part1 + part2;
        setCursor(part1.length());
    }

    @Override
    public void onScreenUpdate() {
        if (isFocused() && ++cursorTimer == 10) {
            renderer.toggleRenderCursor();
            cursorTimer = 0;
        }
    }

    @Override
    public void drawInBackground(float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.5f, 0.5f, 0);
        renderer.setUp(Pos2d.ZERO, textColor, getSize().width);
        renderer.draw(text);
        GlStateManager.popMatrix();
    }

    @Override
    public boolean onKeyPressed(char character, int keyCode) {
        if (character == Character.MIN_VALUE) {
            switch (keyCode) {
                case Keyboard.KEY_ESCAPE:
                case Keyboard.KEY_INSERT:
                    removeFocus();
                    break;
                case Keyboard.KEY_LEFT: {
                    int newCursor = cursor - 1;
                    if (Interactable.hasControlDown()) {
                        newCursor = text.lastIndexOf(" ", newCursor);
                        if (newCursor < 0) {
                            newCursor = 0;
                        }
                    }
                    setCursor(newCursor, !Interactable.hasShiftDown());
                    break;
                }
                case Keyboard.KEY_RIGHT: {
                    int newCursor = cursor + 1;
                    if (Interactable.hasControlDown()) {
                        newCursor = text.indexOf(" ", newCursor);
                        if (newCursor < 0) {
                            newCursor = text.length();
                        }
                    }
                    setCursor(newCursor, !Interactable.hasShiftDown());
                    break;
                }

                case Keyboard.KEY_DELETE:
                    delete(true);
                    break;
                case Keyboard.KEY_BACK:
                    delete(false);
                    break;
                default:
                    return false;
            }
            return true;
        }
        if (keyCode == Keyboard.KEY_BACK) {
            // backspace char is not equal to Character.MIN_VALUE
            int oldLength = text.length();
            delete(false);
            return oldLength != text.length();
        }

        if (GuiScreen.isKeyComboCtrlC(keyCode)) {
            // copy marked text
            GuiScreen.setClipboardString(getSelectedText());
            return true;
        } else if (GuiScreen.isKeyComboCtrlV(keyCode)) {
            // paste copied text in marked text
            insert(GuiScreen.getClipboardString());
            return true;
        } else if (GuiScreen.isKeyComboCtrlX(keyCode) && hasTextSelected()) {
            // copy and delete copied text
            GuiScreen.setClipboardString(getSelectedText());
            delete(false);
            return true;
        } else if (GuiScreen.isKeyComboCtrlA(keyCode)) {
            // mark whole text
            setCursor(0);
            setCursor(text.length(), false);
            return true;
        } else if (BASE_PATTERN.matcher(String.valueOf(character)).matches()) {
            // insert typed char
            insert(String.valueOf(character));
            return true;
        }
        return false;
    }

    @Override
    public void onClick(int buttonId, boolean doubleClick) {
        setCursor(getTextIndexUnderMouse());
    }

    @Override
    public void onMouseDragged(int buttonId, long deltaTime) {
        setCursor(getTextIndexUnderMouse(), false);
    }

    private boolean canFit(String string) {
        helper.setUp(Pos2d.ZERO, 0, size.width);
        helper.setDoDraw(false);
        helper.draw(string);
        return helper.getHeight() <= size.height;
    }

    private int getTextIndexUnderMouse() {
        return getTextIndex(getContext().getMousePos().subtract(getAbsolutePos()));
    }

    private int getTextIndex(Pos2d pos2d) {
        if (text.isEmpty()) {
            return 0;
        }
        helper.setUp(Pos2d.ZERO, 0, size.width);
        helper.setPosToFind(pos2d);
        helper.setDoDraw(false);
        helper.draw(text);
        if (helper.getFoundIndex() < 0) {
            if (pos2d.x > helper.getWidth() || (pos2d.y >= helper.getHeight() - helper.getFontHeight() && pos2d.y <= helper.getHeight())) {
                return text.length();
            }
            return 0;
        }
        return helper.getFoundIndex();
    }

    @Override
    public boolean shouldGetFocus() {
        return true;
    }

    @Override
    public void onRemoveFocus() {
        super.onRemoveFocus();
        renderer.setRenderCursor(false);
        cursorTimer = 0;
        setCursorEnd(cursor);
        text = validator.apply(text);
    }

    @Nullable
    @Override
    protected Size determineSize() {
        if (maxWidth < 0) {
            maxWidth = 80;
        }
        if (maxLines <= 0) {
            maxLines = 1;
        }
        return new Size(maxWidth - 1, (int) (renderer.getFontHeight() * maxLines + 0.5));
    }

    @Override
    public void readServerData(int id, PacketBuffer buf) {

    }

    @Override
    public void readClientData(int id, PacketBuffer buf) {

    }

    public TextFieldWidget setPattern(Pattern pattern) {
        this.pattern = pattern;
        return this;
    }

    public TextFieldWidget setTextColor(int textColor) {
        this.textColor = textColor;
        return this;
    }

    public TextFieldWidget setMarkedColor(int color) {
        renderer.setMarkedColor(color);
        return this;
    }

    public TextFieldWidget setMaxLines(int maxLines) {
        this.maxLines = maxLines;
        return this;
    }

    public TextFieldWidget setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
        return this;
    }

    public TextFieldWidget setBounds(int maxWidth, int maxLines) {
        setMaxWidth(maxWidth);
        return setMaxLines(maxLines);
    }

    public TextFieldWidget setScale(float scale) {
        renderer.setScale(scale);
        helper.setScale(scale);
        return this;
    }

    public TextFieldWidget setValidator(Function<String, String> validator) {
        this.validator = validator;
        return this;
    }

    public TextFieldWidget setNumbersLong(Function<Long, Long> validator) {
        setPattern(WHOLE_NUMS);
        setValidator(val -> {
            long num;
            if (val.isEmpty()) {
                num = 0;
            } else {
                try {
                    num = Long.parseLong(val);
                } catch (NumberFormatException e) {
                    num = 0;
                }
            }
            return String.valueOf(validator.apply(num));
        });
        return this;
    }

    public TextFieldWidget setNumbers(Function<Integer, Integer> validator) {
        setPattern(WHOLE_NUMS);
        return setValidator(val -> {
            int num;
            if (val.isEmpty()) {
                num = 0;
            } else {
                try {
                    num = Integer.parseInt(val);
                } catch (NumberFormatException e) {
                    num = 0;
                }
            }
            return String.valueOf(validator.apply(num));
        });
    }

    public TextFieldWidget setNumbersDouble(Function<Double, Double> validator) {
        setPattern(DECIMALS);
        return setValidator(val -> {
            double num;
            if (val.isEmpty()) {
                num = 0;
            } else {
                try {
                    num = Double.parseDouble(val);
                } catch (NumberFormatException e) {
                    num = 0;
                }
            }
            return String.valueOf(validator.apply(num));
        });
    }

    public TextFieldWidget setNumbers(Supplier<Integer> min, Supplier<Integer> max) {
        return setNumbers(val -> Math.min(max.get(), Math.max(min.get(), val)));
    }

    public TextFieldWidget setNumbersLong(Supplier<Long> min, Supplier<Long> max) {
        return setNumbersLong(val -> Math.min(max.get(), Math.max(min.get(), val)));
    }

    public TextFieldWidget setNumbers(int min, int max) {
        return setNumbers(val -> Math.min(max, Math.max(min, val)));
    }
}