package com.justindevsjava.skyblock_plus.client.ui.clickgui.fubuki.nav;

import com.justindevsjava.skyblock_plus.client.ui.Easy2D;
import com.justindevsjava.skyblock_plus.client.ui.animation.Animation;
import com.justindevsjava.skyblock_plus.client.ui.animation.Smooth;
import com.justindevsjava.skyblock_plus.client.ui.clickgui.Element;
import com.justindevsjava.skyblock_plus.client.ui.font.FontManager;
import com.justindevsjava.skyblock_plus.client.ui.font.RenderInfo;
import com.justindevsjava.skyblock_plus.client.ui.font.RenderedText;
import com.mojang.blaze3d.platform.Window;
import it.unimi.dsi.fastutil.floats.FloatFloatImmutablePair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.ARGB;

import java.util.List;

public class NavigationCategories implements Element {
    public float startX, startY;
    public float width, height;
    public float buttonHeight;
    public float fontSize;
    public final List<NavigationDestination> destinations = new ObjectArrayList<>();
    public int layerDepth;

    private final Window window;
    private int selectedIndex = 0;
    private Animation highlightStartX = new Smooth(0, 0);
    private Animation highlightEndX = new Smooth(0, 0);
    private List<FloatFloatImmutablePair> categoryTextBounds;

    public NavigationCategories(float fontSize, Window window, int layerDepth) {
        this.buttonHeight = height;
        this.fontSize = fontSize;
        this.window = window;
        this.layerDepth = layerDepth;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, long timeDiff) {
        float scale = (float) window.getGuiScale();

        // tick animations
        highlightStartX.tick(timeDiff * 0.02F);
        highlightEndX.tick(timeDiff * 0.02F);

        // draw navigation buttons
        if (destinations.isEmpty()) { // should be avoided
            highlightStartX.target = startX;
            highlightEndX.target = startX + width;
        } else {
            List<RenderedText> categoryRenderedTexts = destinations.stream()
                    .map(destination -> {
                        String name = destination.name();
                        if (name == null || name.isBlank()) name = "|EMPTY|"; // wow
                        return FontManager.requestRenderedText(
                                new RenderInfo(FontManager.DEFAULT_FONT, name, fontSize), (float) window.getGuiScale()
                        );
                    }).toList();
            float currentY = startY;
            List<FloatFloatImmutablePair> bounds = new ObjectArrayList<>(categoryRenderedTexts.size());
            for (int i = 0; i < categoryRenderedTexts.size(); i++) {
                RenderedText text = categoryRenderedTexts.get(i);
                boolean selected = i == selectedIndex;
                if (i == selectedIndex) {
                    highlightStartX.target = currentY - startY;
                    highlightEndX.target = currentY + buttonHeight - startY;
                }
                Easy2D.drawRoundRect(startX, currentY, startX + width, currentY + buttonHeight,
                        5F, selected ? 7F : 2F, selected ? 0x3324153F : 0x10141720, 0x00000000);
                if (selected) {
                    Easy2D.drawRoundRect(startX + 4F, currentY + 6F, startX + 7F, currentY + buttonHeight - 6F,
                            1.5F, 2F, 0xFFD8C7FF, 0x00000000);
                }
                text.draw(context, startX + 15F, currentY + (buttonHeight + text.bounds.y / scale) * 0.5F,
                        scale, selected ? 0xFFFFFFFF : ARGB.color(0xCC, 0xB7B1C5));
                bounds.add(new FloatFloatImmutablePair(currentY, currentY + buttonHeight));
                currentY += buttonHeight + 7F;
            }
            categoryTextBounds = bounds;
        }
    }

    @Override
    public void updateStartPosition(float newX, float newY) {
        this.startX = newX;
        this.startY = newY;
    }

    @Override
    public void updateEndPosition(float newX, float newY) {
    }

    @Override
    public boolean mouseClicked(float mouseX, float mouseY) {
        if (categoryTextBounds != null
                && startX <= mouseX && mouseX <= startX + width
                && startY <= mouseY && mouseY <= startY + height) {
            List<FloatFloatImmutablePair> bounds = categoryTextBounds;
            for (int i = 0; i < bounds.size(); i++) {
                FloatFloatImmutablePair bound = categoryTextBounds.get(i);
                if (bound.leftFloat() <= mouseY && mouseY <= bound.rightFloat()) {
                    if (selectedIndex != i && destinations.get(i).navigate()) {
                        selectedIndex = i;
                    }
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void resize() {
        // avoid unwanted animations caused by window size changes
        highlightStartX.current = highlightStartX.target;
        highlightEndX.current = highlightEndX.target;
    }

    @Override
    public void remove() {
        if (categoryTextBounds != null) {
            this.categoryTextBounds.clear();
            this.categoryTextBounds = null;
        }
        Element.super.remove();
    }

    @Override
    public int getLayerDepth() {
        return layerDepth;
    }
}
