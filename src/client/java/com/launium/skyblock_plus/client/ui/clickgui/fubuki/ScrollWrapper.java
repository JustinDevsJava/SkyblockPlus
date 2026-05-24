package com.launium.skyblock_plus.client.ui.clickgui.fubuki;

import com.launium.skyblock_plus.client.ui.animation.Animation;
import com.launium.skyblock_plus.client.ui.animation.Smooth;
import com.launium.skyblock_plus.client.ui.clickgui.Element;
import com.launium.skyblock_plus.client.ui.clickgui.fubuki.list.ListView;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

public class ScrollWrapper implements Element, CullingProvider {
    public Element child;
    private float startX, startY, endX, endY;
    private final Animation verticalScroll = new Smooth(0F, 0F);

    public ScrollWrapper(Element child) {
        this.child = child;
        if (child instanceof CullingReceiver receiver) {
            receiver.setCullingProvider(this);
        }
    }

    public void setVerticalOffset(float offset) {
        verticalScroll.target = clampOffset(offset);
        verticalScroll.current = clampOffset(verticalScroll.current);
    }

    public void clampVerticalOffset() {
        verticalScroll.target = clampOffset(verticalScroll.target);
        verticalScroll.current = clampOffset(verticalScroll.current);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, long timeDiff) {
        verticalScroll.target = clampOffset(verticalScroll.target);
        // tick animations
        verticalScroll.tick(timeDiff * 0.05F);
        verticalScroll.current = clampOffset(verticalScroll.current);

        child.updateStartPosition(startX, startY + verticalScroll.current);
        child.updateEndPosition(endX, endY + verticalScroll.current);

        context.enableScissor(Mth.floor(startX), Mth.floor(startY),
                Mth.ceil(endX), Mth.ceil(endY));
        child.render(context, mouseX, mouseY, timeDiff);
        context.disableScissor();
    }

    @Override
    public boolean canBeCulled(float startX, float startY, float endX, float endY) {
        return startX > this.endX || startY > this.endY
                || endX < this.startX || endY < this.startY;
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
    public void resize() {
        child.resize();
    }

    @Override
    public void remove() {
        child.remove();
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return child.shouldCloseOnEsc();
    }

    @Override
    public boolean mouseClicked(float mouseX, float mouseY) {
        return child.mouseClicked(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(float mouseX, float mouseY, int button) {
        return child.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(float mouseX, float mouseY, float dragX, float dragY) {
        return child.mouseDragged(mouseX, mouseY, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(float mouseX, float mouseY, float scrollX, float scrollY) {
        if (!child.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
            verticalScroll.target = clampOffset(verticalScroll.target + scrollY * 14);
        }
        return true;
    }

    @Override
    public int getLayerDepth() {
        return child.getLayerDepth();
    }

    private float clampOffset(float offset) {
        float viewportHeight = endY - startY;
        if (viewportHeight <= 0F) return 0F;
        float contentHeight = child instanceof ListView<?> listView ? listView.measureContentHeight() : viewportHeight;
        float minOffset = Math.min(0F, viewportHeight - contentHeight);
        return Mth.clamp(offset, minOffset, 0F);
    }
}
