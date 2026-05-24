package com.justindevsjava.skyblock_plus.client.ui.clickgui;

import com.justindevsjava.skyblock_plus.client.ui.Easy2D;
import com.justindevsjava.skyblock_plus.client.ui.clickgui.fubuki.list.MeasurableElement;
import com.justindevsjava.skyblock_plus.client.ui.font.FontManager;
import com.justindevsjava.skyblock_plus.client.ui.font.RenderInfo;
import com.justindevsjava.skyblock_plus.client.ui.font.RenderedText;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

import java.util.function.Consumer;
import java.util.function.DoubleSupplier;

public class NumberItemView implements MeasurableElement {
    public static final int LAYER_DEPTH = ClickGUIScreen.LAYER_DEPTH + 3;
    public static final float HEIGHT = 50F;

    private final Window window;
    private final String title;
    private final DoubleSupplier getter;
    private final Consumer<Float> setter;
    private final float min;
    private final float max;
    private final float step;
    private float startX, startY, endX, endY;

    public NumberItemView(Minecraft client, String title, DoubleSupplier getter, Consumer<Float> setter, float min, float max, float step) {
        this.window = client.getWindow();
        this.title = title;
        this.getter = getter;
        this.setter = setter;
        this.min = min;
        this.max = max;
        this.step = step;
    }

    @Override
    public float measureHeight() {
        return HEIGHT;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, long timeDiff) {
        float scale = (float) window.getGuiScale();
        boolean hovered = startX <= mouseX && mouseX <= endX && startY <= mouseY && mouseY <= endY;
        Easy2D.drawRoundRect(startX, startY, endX, endY, 14F, hovered ? 3F : 1F,
                hovered ? 0x66706482 : 0x464A435F, 0x00000000);
        Easy2D.drawRoundRect(startX + 1F, startY + 1F, endX - 1F, endY - 1F, 13F, 0F,
                hovered ? 0x5A15141D : 0x48111017, 0x00000000);

        RenderedText titleText = FontManager.requestRenderedText(new RenderInfo(FontManager.DEFAULT_FONT, title, 8F), scale);
        titleText.draw(context, startX + 14F, startY + 9F, scale, 0xFFF6F3FF);
        drawValue(context, endX - 54F, startY + 6F, 42F);

        float sliderLeft = startX + 14F;
        float sliderRight = endX - 14F;
        float sliderY = startY + 34F;
        Easy2D.drawRoundRect(sliderLeft, sliderY, sliderRight, sliderY + 3F, 1.5F, 0F, 0x554A435F, 0x00000000);
        float percent = ((float) getter.getAsDouble() - min) / (max - min);
        float knobX = Mth.lerp(percent, sliderLeft, sliderRight);
        Easy2D.drawRoundRect(sliderLeft, sliderY, knobX, sliderY + 3F, 1.5F, 0F, 0xFFD8C7FF, 0x00000000);
        Easy2D.drawRoundRect(knobX - 4F, sliderY - 4F, knobX + 4F, sliderY + 7F, 4F, 2F, 0xFFD8C7FF, 0x00000000);
    }

    private void drawValue(GuiGraphics context, float x, float y, float width) {
        float scale = (float) window.getGuiScale();
        String value = String.format("%.2f", getter.getAsDouble());
        RenderedText text = FontManager.requestRenderedText(new RenderInfo(FontManager.DEFAULT_FONT, value, 8F), scale);
        text.draw(context, x + (width - text.bounds.width / scale) * 0.5F, y + 5F, scale, 0xFFBDB6CC);
    }

    @Override
    public void updateStartPosition(float newX, float newY) {
        this.startX = newX;
        this.startY = newY;
    }

    @Override
    public void updateEndPosition(float newX, float newY) {
        this.endX = newX;
        this.endY = newY;
    }

    @Override
    public boolean mouseClicked(float mouseX, float mouseY) {
        if (startX > mouseX || endX < mouseX || startY > mouseY || endY < mouseY) return false;
        setFromMouse(mouseX);
        return true;
    }

    @Override
    public boolean mouseDragged(float mouseX, float mouseY, float dragX, float dragY) {
        if (startY > mouseY || endY < mouseY) return false;
        setFromMouse(mouseX);
        return true;
    }

    private void setValue(float value) {
        setter.accept(Mth.clamp(value, min, max));
    }

    private void setFromMouse(float mouseX) {
        float sliderLeft = startX + 14F;
        float sliderRight = endX - 14F;
        float percent = Mth.clamp((mouseX - sliderLeft) / (sliderRight - sliderLeft), 0F, 1F);
        float raw = Mth.lerp(percent, min, max);
        float stepped = Math.round(raw / step) * step;
        setValue(stepped);
    }

    @Override
    public int getLayerDepth() {
        return LAYER_DEPTH;
    }
}
