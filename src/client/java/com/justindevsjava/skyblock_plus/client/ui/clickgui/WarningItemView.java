package com.justindevsjava.skyblock_plus.client.ui.clickgui;

import com.justindevsjava.skyblock_plus.client.ui.Easy2D;
import com.justindevsjava.skyblock_plus.client.ui.clickgui.fubuki.list.MeasurableElement;
import com.justindevsjava.skyblock_plus.client.ui.font.FontManager;
import com.justindevsjava.skyblock_plus.client.ui.font.RenderInfo;
import com.justindevsjava.skyblock_plus.client.ui.font.RenderedText;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class WarningItemView implements MeasurableElement {
    private static final float HEIGHT = 56F;

    private final Window window;
    private final String title;
    private final String body;
    private float startX, startY, endX, endY;

    public WarningItemView(Minecraft client, String title, String body) {
        this.window = client.getWindow();
        this.title = title;
        this.body = body;
    }

    @Override
    public float measureHeight() {
        return HEIGHT;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, long timeDiff) {
        float scale = (float) window.getGuiScale();
        Easy2D.drawRoundRect(startX, startY, endX, endY, 14F, 2F, 0x88F1C36D, 0x00000000);
        Easy2D.drawRoundRect(startX + 1F, startY + 1F, endX - 1F, endY - 1F, 13F, 0F, 0x4A24190C, 0x00000000);

        RenderedText icon = FontManager.requestRenderedText(new RenderInfo(FontManager.BOLD_FONT, "!", 12F), scale);
        icon.draw(context, startX + 15F, startY + 18F, scale, 0xFFFFD98A);

        RenderedText titleText = FontManager.requestRenderedText(new RenderInfo(FontManager.BOLD_FONT, title, 8F), scale);
        titleText.draw(context, startX + 32F, startY + 11F, scale, 0xFFFFE5B5);

        RenderedText bodyText = FontManager.requestRenderedText(new RenderInfo(FontManager.DEFAULT_FONT, body, 7F), scale);
        bodyText.draw(context, startX + 32F, startY + 29F, scale, 0xFFD6B989);
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
}
