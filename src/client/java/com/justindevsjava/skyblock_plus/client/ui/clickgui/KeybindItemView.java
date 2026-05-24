package com.justindevsjava.skyblock_plus.client.ui.clickgui;

import com.justindevsjava.skyblock_plus.client.ui.Easy2D;
import com.justindevsjava.skyblock_plus.client.ui.clickgui.fubuki.list.MeasurableElement;
import com.justindevsjava.skyblock_plus.client.ui.font.FontManager;
import com.justindevsjava.skyblock_plus.client.ui.font.RenderInfo;
import com.justindevsjava.skyblock_plus.client.ui.font.RenderedText;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class KeybindItemView implements MeasurableElement {
    private static final float HEIGHT = 34F;

    private final Window window;
    private final String title;
    private final IntSupplier getter;
    private final IntConsumer setter;
    private float startX, startY, endX, endY;
    private boolean listening = false;

    public KeybindItemView(Minecraft client, String title, IntSupplier getter, IntConsumer setter) {
        this.window = client.getWindow();
        this.title = title;
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public float measureHeight() {
        return HEIGHT;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, long timeDiff) {
        float scale = (float) window.getGuiScale();
        boolean hovered = startX <= mouseX && mouseX <= endX && startY <= mouseY && mouseY <= endY;
        if (hovered || listening) {
            Easy2D.drawRoundRect(startX, startY, endX, endY, 6F, 0F, listening ? 0x33211438 : 0x2515141D, 0x00000000);
        }
        context.fill(Mth.floor(startX), Mth.floor(endY - 1F), Mth.ceil(endX), Mth.ceil(endY), 0x224A435F);

        float keyBoxWidth = 58F;
        float titleEndX = endX - keyBoxWidth - 12F;
        RenderedText titleText = FontManager.requestRenderedText(new RenderInfo(FontManager.DEFAULT_FONT, title, 8F), scale);
        context.enableScissor(Mth.floor(startX + 8F), Mth.floor(startY), Mth.ceil(titleEndX), Mth.ceil(endY));
        titleText.draw(context, startX + 8F, startY + 11F, scale, 0xFFEDEAF5);
        context.disableScissor();

        float keyBoxStart = endX - keyBoxWidth;
        Easy2D.drawRoundRect(keyBoxStart, startY + 6F, endX - 8F, endY - 6F, 5F, 0F,
                listening ? 0x66382060 : 0x33181723, 0x00000000);
        String keyName = listening ? "Press..." : keyName(getter.getAsInt());
        RenderedText keyText = FontManager.requestRenderedText(new RenderInfo(FontManager.BOLD_FONT, keyName, 7F), scale);
        keyText.draw(context, keyBoxStart + Math.max(4F, (keyBoxWidth - 8F - keyText.bounds.width / scale) * 0.5F),
                startY + 12F, scale, listening ? 0xFFD8C7FF : 0xFFBDB6CC);
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
        listening = true;
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (!listening) return false;
        setter.accept(event.key() == GLFW.GLFW_KEY_ESCAPE ? GLFW.GLFW_KEY_UNKNOWN : event.key());
        listening = false;
        return true;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return !listening;
    }

    @Override
    public int getLayerDepth() {
        return ClickGUIScreen.LAYER_DEPTH + 3;
    }

    private static String keyName(int key) {
        if (key == GLFW.GLFW_KEY_UNKNOWN) return "None";
        String name = GLFW.glfwGetKeyName(key, 0);
        if (name != null) return name.toUpperCase();
        return switch (key) {
            case GLFW.GLFW_KEY_LEFT -> "Left";
            case GLFW.GLFW_KEY_RIGHT -> "Right";
            case GLFW.GLFW_KEY_BACKSPACE -> "Back";
            case GLFW.GLFW_KEY_DELETE -> "Del";
            case GLFW.GLFW_KEY_ENTER -> "Enter";
            case GLFW.GLFW_KEY_SPACE -> "Space";
            default -> "Key " + key;
        };
    }
}
