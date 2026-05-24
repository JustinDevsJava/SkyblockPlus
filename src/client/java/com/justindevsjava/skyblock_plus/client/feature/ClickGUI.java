package com.justindevsjava.skyblock_plus.client.feature;

import com.justindevsjava.skyblock_plus.client.SkyblockPlusClient;
import com.justindevsjava.skyblock_plus.client.ui.clickgui.ClickGUIScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class ClickGUI implements ClientTickEvents.StartTick {
    public static final ClickGUI INSTANCE = new ClickGUI();

    private static final KeyMapping CLICK_GUI_KEY = KeyBindingHelper.registerKeyBinding(
            new KeyMapping("key.skyblock_plus.show_click_gui", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, SkyblockPlusClient.KEY_CATEGORY)
    );

    @Override
    public void onStartTick(Minecraft client) {
        boolean clicked = false;
        while (CLICK_GUI_KEY.consumeClick()) {
            clicked = true;
        }
        if (clicked) {
            client.setScreen(new ClickGUIScreen(client, client.screen));
        }
    }
}
