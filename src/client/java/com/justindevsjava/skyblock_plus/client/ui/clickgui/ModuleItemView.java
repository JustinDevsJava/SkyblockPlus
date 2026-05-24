package com.justindevsjava.skyblock_plus.client.ui.clickgui;

import com.justindevsjava.skyblock_plus.client.ui.Easy2D;
import com.justindevsjava.skyblock_plus.client.ui.clickgui.fubuki.Switch;
import com.justindevsjava.skyblock_plus.client.ui.clickgui.fubuki.list.MeasurableElement;
import com.justindevsjava.skyblock_plus.client.ui.font.FontManager;
import com.justindevsjava.skyblock_plus.client.ui.font.RenderInfo;
import com.justindevsjava.skyblock_plus.client.ui.font.RenderedText;
import com.mojang.blaze3d.platform.Window;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public class ModuleItemView implements MeasurableElement {
    public static final int LAYER_DEPTH = ClickGUIScreen.LAYER_DEPTH + 3;
    public static final float HEIGHT = 58F;

    public @NotNull String title;
    public @Nullable String subtitle;

    private float startX, startY, endX, endY;
    private Window window;
    private Runnable callback;
    private Runnable optionsCallback;
    private boolean optionsOpen;
    private boolean compact;
    private Switch simpleSwitcher;

    private ModuleItemView(Minecraft client, @NotNull String title, String subtitle) {
        this.title = title;
        this.subtitle = subtitle;
        this.window = client.getWindow();
    }

    ModuleItemView(Minecraft client, @NotNull String title, String subtitle, boolean switcherValue, @NotNull BooleanConsumer simpleSwitcherCallback) {
        this(client, title, subtitle);
        this.simpleSwitcher = new Switch(0xFFD8C7FF, switcherValue, LAYER_DEPTH + 1, simpleSwitcherCallback);
    }

    ModuleItemView withOptions(@NotNull Runnable callback) {
        this.optionsCallback = callback;
        return this;
    }

    void setOptionsOpen(boolean optionsOpen) {
        this.optionsOpen = optionsOpen;
    }

    ModuleItemView compact() {
        this.compact = true;
        return this;
    }

    ModuleItemView(Minecraft client, @NotNull String title, String subtitle, @NotNull Runnable callback) {
        this(client, title, subtitle);
        this.callback = callback;
    }

    @Override
    public float measureHeight() {
        return compact ? 36F : HEIGHT;
    }

    @Override
    public int getLayerDepth() {
        return LAYER_DEPTH;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, long timeDiff) {
        float scale = (float) window.getGuiScale();
        boolean hovered = startX <= mouseX && mouseX <= endX && startY <= mouseY && mouseY <= endY;
        float height = measureHeight();
        float radius = compact ? 6F : height * 0.5F;
        int borderColor = optionsOpen ? 0xA8D8B7FF : hovered ? 0x6E6B607C : 0x464A435F;
        int fillColor = optionsOpen ? 0x66211438 : hovered ? 0x5A15141D : 0x48111017;
        if (compact) {
            if (hovered || optionsOpen) {
                Easy2D.drawRoundRect(startX, startY, endX, endY, radius, 0F,
                        optionsOpen ? 0x33211438 : 0x2515141D, 0x00000000);
            }
            context.fill(Mth.floor(startX), Mth.floor(endY - 1F), Mth.ceil(endX), Mth.ceil(endY), 0x224A435F);
        } else {
            Easy2D.drawRoundRect(startX, startY, endX, endY, radius, hovered || optionsOpen ? 4F : 1F,
                    borderColor, 0x00000000);
            Easy2D.drawRoundRect(startX + 1F, startY + 1F, endX - 1F, endY - 1F, radius - 1F, 0F,
                    fillColor, 0x00000000);
        }

        float rightReserve = 12F;
        if (simpleSwitcher != null) rightReserve += Switch.WIDTH + 10F;
        if (optionsCallback != null) rightReserve += 24F;
        float textStartX = startX + (compact ? 8F : 14F);
        float textEndX = Math.max(textStartX + 28F, endX - rightReserve);
        RenderedText titleText = FontManager.requestRenderedText(
                new RenderInfo(FontManager.BOLD_FONT, title, 9F), scale);
        context.enableScissor(Mth.floor(textStartX), Mth.floor(startY), Mth.ceil(textEndX), Mth.ceil(endY));
        titleText.draw(context, textStartX, compact ? startY + 8F : startY + 13F, scale, 0xFFEDEAF5);
        if (subtitle != null) {
            RenderedText subtitleText = FontManager.requestRenderedText(
                new RenderInfo(FontManager.DEFAULT_FONT, subtitle, 7F), scale);
            subtitleText.draw(context, textStartX, compact ? startY + 21F : startY + 30F, scale, 0xFF9B96A7);
        }
        context.disableScissor();
        if (optionsCallback != null) {
            RenderedText text = FontManager.requestRenderedText(new RenderInfo(FontManager.BOLD_FONT, "...", 7F), scale);
            float dotsX = simpleSwitcher == null ? endX - 24F : endX - Switch.WIDTH - 28F;
            text.draw(context, dotsX, startY + (height - text.lineHeight / scale) * 0.5F, scale,
                    optionsOpen ? 0xFFD8C7FF : 0xFF8F879E);
        }

        if (simpleSwitcher != null) {
            float switchY = startY + (height - Switch.HEIGHT) * 0.5F;
            simpleSwitcher.updateStartPosition(endX - Switch.WIDTH - 12F, switchY);
            simpleSwitcher.updateEndPosition(endX - 12F, switchY + Switch.HEIGHT);
            simpleSwitcher.render(context, mouseX, mouseY, timeDiff);
        }
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
    public void remove() {
        MeasurableElement.super.remove();
    }

    @Override
    public boolean mouseClicked(float mouseX, float mouseY) {
        return mouseClicked(mouseX, mouseY, 0);
    }

    @Override
    public boolean mouseClicked(float mouseX, float mouseY, int button) {
        if (startX > mouseX || endX < mouseX || startY > mouseY || endY < mouseY) return false;
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && optionsCallback != null) {
            optionsCallback.run();
            return true;
        }
        if (callback != null) {
            callback.run();
        } else if (simpleSwitcher != null) {
            simpleSwitcher.mouseClicked(mouseX, mouseY);
        } else {
            return false; // but why?
        }
        return true;
    }
}
