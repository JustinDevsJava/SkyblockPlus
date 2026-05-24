package com.justindevsjava.skyblock_plus.client.ui.clickgui;

import com.justindevsjava.skyblock_plus.client.ui.clickgui.fubuki.list.MeasurableElement;
import com.justindevsjava.skyblock_plus.client.ui.font.FontManager;
import com.justindevsjava.skyblock_plus.client.ui.font.RenderInfo;
import com.justindevsjava.skyblock_plus.client.ui.font.RenderedText;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

public class SectionHeaderView implements MeasurableElement {
    private static final float HEIGHT = 22F;

    private final Window window;
    private final String title;
    private final Runnable toggleCallback;
    private boolean collapsed;
    private float startX, startY, endX, endY;

    public SectionHeaderView(Minecraft client, String title) {
        this(client, title, false, null);
    }

    public SectionHeaderView(Minecraft client, String title, boolean collapsed, Runnable toggleCallback) {
        this.window = client.getWindow();
        this.title = title;
        this.collapsed = collapsed;
        this.toggleCallback = toggleCallback;
    }

    @Override
    public float measureHeight() {
        return HEIGHT;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, long timeDiff) {
        float scale = (float) window.getGuiScale();
        boolean hovered = startX <= mouseX && mouseX <= endX && startY <= mouseY && mouseY <= endY;
        if (hovered) {
            context.fill(Mth.floor(startX), Mth.floor(startY + 3F), Mth.ceil(endX), Mth.ceil(endY), 0x24171621);
        }
        RenderedText caretText = FontManager.requestRenderedText(new RenderInfo(FontManager.BOLD_FONT, collapsed ? "+" : "-", 8F), scale);
        caretText.draw(context, startX + 2F, startY + 8F, scale, 0xFFD8C7FF);
        RenderedText titleText = FontManager.requestRenderedText(new RenderInfo(FontManager.BOLD_FONT, title, 8F), scale);
        titleText.draw(context, startX + 16F, startY + 8F, scale, 0xFFD8C7FF);
        context.fill(Mth.floor(startX + 16F + titleText.bounds.width / scale + 12F), Mth.floor(startY + 14F),
                Mth.ceil(endX), Mth.ceil(startY + 15F), 0x245A526E);
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
    public int getLayerDepth() {
        return ClickGUIScreen.LAYER_DEPTH + 3;
    }

    @Override
    public boolean mouseClicked(float mouseX, float mouseY) {
        if (toggleCallback == null || startX > mouseX || endX < mouseX || startY > mouseY || endY < mouseY) return false;
        toggleCallback.run();
        return true;
    }
}
