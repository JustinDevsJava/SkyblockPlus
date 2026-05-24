package com.launium.skyblock_plus.client.ui.clickgui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import org.jetbrains.annotations.NotNull;

public interface Element extends Comparable<Element> {
    default void render(GuiGraphics context, int mouseX, int mouseY, long timeDiff) {
    }

    void updateStartPosition(float newX, float newY);

    void updateEndPosition(float newX, float newY);

    default void resize() {
    }

    default void remove() {
    }

    default boolean shouldCloseOnEsc() {
        return true;
    }

    default boolean mouseClicked(float mouseX, float mouseY) {
        return false;
    }

    default boolean mouseClicked(float mouseX, float mouseY, int button) {
        return mouseClicked(mouseX, mouseY);
    }

    default boolean mouseDragged(float mouseX, float mouseY, float dragX, float dragY) {
        return false;
    }

    default boolean mouseScrolled(float mouseX, float mouseY, float scrollX, float scrollY) {
        return false;
    }

    default boolean keyPressed(KeyEvent event) {
        return false;
    }

    default boolean charTyped(CharacterEvent event) {
        return false;
    }

    int getLayerDepth();

    @Override
    default int compareTo(@NotNull Element o) {
        return Integer.compare(this.getLayerDepth(), o.getLayerDepth());
    }
}
