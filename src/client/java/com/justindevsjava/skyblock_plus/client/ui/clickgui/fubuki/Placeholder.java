package com.justindevsjava.skyblock_plus.client.ui.clickgui.fubuki;

import com.justindevsjava.skyblock_plus.client.ui.clickgui.fubuki.list.MeasurableElement;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Placeholder implements MeasurableElement {
    public float width;
    public float height;

    @Override
    public void updateStartPosition(float newX, float newY) {
    }

    @Override
    public void updateEndPosition(float newX, float newY) {
    }

    @Override
    public int getLayerDepth() {
        return 0; // not necessary
    }

    @Override
    public float measureHeight() {
        return height;
    }
}
