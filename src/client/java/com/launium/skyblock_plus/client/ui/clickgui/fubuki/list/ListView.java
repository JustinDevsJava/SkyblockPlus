package com.launium.skyblock_plus.client.ui.clickgui.fubuki.list;

import com.launium.skyblock_plus.client.ui.clickgui.Element;
import com.launium.skyblock_plus.client.ui.clickgui.fubuki.CullingProvider;
import com.launium.skyblock_plus.client.ui.clickgui.fubuki.CullingReceiver;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;

import java.util.SequencedCollection;

public class ListView<T extends MeasurableElement> implements Element, CullingReceiver {
    public SequencedCollection<T> elementList;
    public float gap;
    public int layerDepth;
    private float startX, startY, endX, endY;
    private CullingProvider cullingProvider;

    public ListView(SequencedCollection<T> elementList, float gap, int layerDepth) {
        this.elementList = elementList;
        this.gap = gap;
        this.layerDepth = layerDepth;
    }

    public float measureContentHeight() {
        if (elementList == null || elementList.isEmpty()) return 0F;
        boolean grid = useGridLayout();
        float height = gap;
        boolean hasOpenGridRow = false;
        for (T element : elementList) {
            if (grid && isGridCell(element)) {
                if (!hasOpenGridRow) {
                    height += element.measureHeight() + gap;
                    hasOpenGridRow = true;
                } else {
                    hasOpenGridRow = false;
                }
            } else {
                height += element.measureHeight() + gap;
                hasOpenGridRow = false;
            }
        }
        return height;
    }

    private Element getElementByPosition(float x, float y) {
        if (startX > x || endX < x) {
            return null;
        }
        float currentY = startY + gap;
        if (currentY > y) return null;
        boolean leftCell = true;
        boolean grid = useGridLayout();
        float columnGap = gap;
        float cellWidth = (endX - startX - columnGap) * 0.5F;
        float openGridHeight = 0F;
        for (T element : elementList) {
            float measuredHeight = element.measureHeight();
            if (grid && isGridCell(element)) {
                openGridHeight = measuredHeight;
                float cellStartX = leftCell ? startX : startX + cellWidth + columnGap;
                float cellEndX = cellStartX + cellWidth;
                if (cellStartX <= x && x <= cellEndX && currentY <= y && currentY + measuredHeight >= y) {
                    return element;
                }
                if (leftCell) {
                    leftCell = false;
                } else {
                    leftCell = true;
                    currentY += measuredHeight + gap;
                }
            } else {
                if (!leftCell) {
                    currentY += openGridHeight + gap;
                    leftCell = true;
                }
                if (currentY <= y && currentY + measuredHeight >= y) {
                    return element;
                }
                leftCell = true;
                currentY += element.measureHeight() + gap;
            }
        }
        return null;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, long timeDiff) {
        float currentY = startY + gap;
        boolean leftCell = true;
        boolean grid = useGridLayout();
        float columnGap = gap;
        float cellWidth = (endX - startX - columnGap) * 0.5F;
        float openGridHeight = 0F;
        for (T element : elementList) {
            float measuredHeight = element.measureHeight();
            if (grid && isGridCell(element)) {
                openGridHeight = measuredHeight;
                float cellStartX = leftCell ? startX : startX + cellWidth + columnGap;
                float cellEndX = cellStartX + cellWidth;
                if (cullingProvider != null && !cullingProvider.canBeCulled(cellStartX, currentY, cellEndX, currentY + measuredHeight)) {
                    element.updateStartPosition(cellStartX, currentY);
                    element.updateEndPosition(cellEndX, currentY + measuredHeight);
                    element.render(context, mouseX, mouseY, timeDiff);
                }
                if (leftCell) {
                    leftCell = false;
                } else {
                    leftCell = true;
                    currentY += measuredHeight + gap;
                }
            } else {
                if (!leftCell) {
                    currentY += openGridHeight + gap;
                    leftCell = true;
                }
                if (cullingProvider != null && !cullingProvider.canBeCulled(startX, currentY, endX, currentY + measuredHeight)) {
                    element.updateStartPosition(startX, currentY);
                    element.updateEndPosition(endX, currentY + measuredHeight);
                    element.render(context, mouseX, mouseY, timeDiff);
                }
                leftCell = true;
                currentY += measuredHeight + gap;
            }
        }
    }

    private boolean isGridCell(T element) {
        return element.getClass().getSimpleName().equals("ModuleItemView");
    }

    private boolean useGridLayout() {
        return endX - startX >= 320F;
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
        if (elementList != null) {
            elementList.forEach(Element::resize);
        }
    }

    @Override
    public void remove() {
        if (elementList != null) {
            elementList.forEach(Element::remove);
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return elementList == null || elementList.stream().allMatch(Element::shouldCloseOnEsc);
    }

    @Override
    public boolean mouseClicked(float mouseX, float mouseY) {
        Element selected = getElementByPosition(mouseX, mouseY);
        if (selected != null) {
            return selected.mouseClicked(mouseX, mouseY);
        }
        return false;
    }

    @Override
    public boolean mouseClicked(float mouseX, float mouseY, int button) {
        Element selected = getElementByPosition(mouseX, mouseY);
        if (selected != null) {
            return selected.mouseClicked(mouseX, mouseY, button);
        }
        return false;
    }

    @Override
    public boolean mouseDragged(float mouseX, float mouseY, float dragX, float dragY) {
        Element selected = getElementByPosition(mouseX, mouseY);
        if (selected != null) {
            return selected.mouseDragged(mouseX, mouseY, dragX, dragY);
        }
        return false;
    }

    @Override
    public int getLayerDepth() {
        return layerDepth;
    }

    @Override
    public int compareTo(@NotNull Element o) {
        return 0;
    }

    @Override
    public void setCullingProvider(@NotNull CullingProvider provider) {
        this.cullingProvider = provider;
    }
}
