package com.launium.skyblock_plus.client.ui.clickgui;

import com.launium.skyblock_plus.client.config.ConfigManager;
import com.launium.skyblock_plus.client.ui.Easy2D;
import com.launium.skyblock_plus.client.ui.animation.Animation;
import com.launium.skyblock_plus.client.ui.animation.Smooth;
import com.launium.skyblock_plus.client.ui.clickgui.fubuki.FragmentView;
import com.launium.skyblock_plus.client.ui.font.FontManager;
import com.launium.skyblock_plus.client.ui.font.RenderInfo;
import com.launium.skyblock_plus.client.ui.font.RenderedText;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;

import java.util.PriorityQueue;

public class ClickGUIScreen extends Screen {
    public static final int LAYER_DEPTH = 1;

    private final Screen parent;
    private final Window window;
    private final PriorityQueue<Element> children = new PriorityQueue<>();
    private final FragmentView listFragment;

    private static final float PANEL_WIDTH = 680F;
    private static final float PANEL_HEIGHT = 430F;
    private static final float OUTER_RADIUS = 8F;
    private static final float OUTER_PADDING = 14F;
    private static final int PANEL_COLOR = 0xF4090910;
    private static final Identifier LOGO_TEXTURE = Identifier.fromNamespaceAndPath("skyblock_plus", "textures/image/logo.png");

    private float lastMouseClickRelativeX = 0, lastMouseClickRelativeY = 0;
    private long lastTickTime = 0;
    private final Animation alpha = new Smooth(0F, 0xEF);

    float centerX, centerY;

    public ClickGUIScreen(Minecraft client, Screen parent) {
        super(Component.literal("Skyblock+ ClickGUI"));
        this.parent = parent;

        this.window = client.getWindow();
        this.centerX = (float) Math.ceil(window.getGuiScaledWidth() * 0.5F);
        this.centerY = (float) Math.ceil(window.getGuiScaledHeight() * 0.5F);

        this.listFragment = new FragmentView(new FeaturesPage(client), LAYER_DEPTH + 1);
        this.children.add(this.listFragment);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float partialTick) {
        super.render(context, mouseX, mouseY, partialTick);
        Easy2D.configure(context);

        float scale = (float) window.getGuiScale();
        long now = Util.getMillis();
        long timeDiff = now - lastTickTime;
        if (timeDiff > 40) timeDiff = 40;

        // tick animations
        alpha.tick(timeDiff * 0.005F);

        float panelWidth = Math.min(PANEL_WIDTH, window.getGuiScaledWidth() - 28F);
        float panelHeight = Math.min(PANEL_HEIGHT, window.getGuiScaledHeight() - 28F);
        float startX = centerX - panelWidth * 0.5F;
        float startY = centerY - panelHeight * 0.5F;
        int alphaValue = (int) alpha.current;
        int overlayColor = ARGB.color((int) (alphaValue * 0.36F), 0x03040A);
        context.fill(0, 0, window.getGuiScaledWidth(), window.getGuiScaledHeight(), overlayColor);

        Easy2D.drawRoundRect(startX, startY, startX + panelWidth, startY + panelHeight,
                OUTER_RADIUS, 10F, ARGB.color(alphaValue, PANEL_COLOR), 0x99000000);

        context.blit(RenderPipelines.GUI_TEXTURED, LOGO_TEXTURE,
                Mth.floor(startX + panelWidth * 0.5F - 13F), Mth.floor(startY + 10F),
                0F, 0F, 28, 31, 782, 868, 782, 868);

        this.listFragment.updateStartPosition(startX + OUTER_PADDING, startY + 52F);
        this.listFragment.updateEndPosition(startX + panelWidth - OUTER_PADDING, startY + panelHeight - OUTER_PADDING);

        // draw children
        for (Element child : children) {
            child.render(context, mouseX, mouseY, timeDiff);
        }

        Easy2D.cleanup();
        lastTickTime = now;
    }

    @Override
    protected void renderBlurredBackground(GuiGraphics context) {
        if (ConfigManager.GENERAL.CLICK_GUI_BLUR) {
            super.renderBlurredBackground(context);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        float mouseXF = (float) event.x();
        float mouseYF = (float) event.y();
        lastMouseClickRelativeX = mouseXF - centerX;
        lastMouseClickRelativeY = mouseYF - centerY;

        for (Element child : children) {
            if (child.mouseClicked(mouseXF, mouseYF, event.button())) return true;
        }
        return super.mouseClicked(event, isDoubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        float mouseXF = (float) event.x();
        float mouseYF = (float) event.y();

        for (Element child : children) {
            if (child.mouseDragged(mouseXF, mouseYF, (float) dragX, (float) dragY)) return true;
        }

        centerX = mouseXF - lastMouseClickRelativeX;
        centerY = mouseYF - lastMouseClickRelativeY;
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        for (Element child : children) {
            if (child.mouseScrolled((float) mouseX, (float) mouseY, (float) scrollX, (float) scrollY)) {
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        for (Element child : children) {
            if (child.keyPressed(event)) return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        for (Element child : children) {
            if (child.charTyped(event)) return true;
        }
        return super.charTyped(event);
    }

    @Override
    public void resize(int width, int height) {
        for (Element child : children) {
            child.resize();
        }
        super.resize(width, height);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return children.stream().allMatch(Element::shouldCloseOnEsc);
    }

    @Override
    public void removed() {
        for (Element child : children) {
            child.remove();
        }
        super.removed();
    }

    @Override
    public void onClose() {
        ConfigManager.processChanges();
        this.minecraft.setScreen(parent);
    }
}
