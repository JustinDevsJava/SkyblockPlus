package com.launium.skyblock_plus.client.feature;

import com.launium.skyblock_plus.client.config.ConfigManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import org.lwjgl.glfw.GLFW;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WardrobeKeybinds {
    private static final Pattern WARDROBE_TITLE = Pattern.compile("Wardrobe \\((\\d)/(\\d)\\)");
    private static final Pattern EQUIPPED_SLOT = Pattern.compile("Slot (\\d): Equipped");

    public static boolean onKeyPressed(AbstractContainerScreen<?> screen, KeyEvent event) {
        if (!ConfigManager.FEATURES.ENABLE_WARDROBE_KEYBINDS) return false;
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.gameMode == null) return false;

        Matcher matcher = WARDROBE_TITLE.matcher(screen.getTitle().getString());
        if (!matcher.matches()) return false;

        int currentPage = Integer.parseInt(matcher.group(1));
        int totalPages = Integer.parseInt(matcher.group(2));
        int key = event.key();
        if (key == GLFW.GLFW_KEY_UNKNOWN) return false;
        int slot = -1;
        if (isBound(ConfigManager.FEATURES.WARDROBE_NEXT_PAGE_KEY) && key == ConfigManager.FEATURES.WARDROBE_NEXT_PAGE_KEY) {
            slot = currentPage < totalPages ? 53 : -1;
        } else if (isBound(ConfigManager.FEATURES.WARDROBE_PREVIOUS_PAGE_KEY) && key == ConfigManager.FEATURES.WARDROBE_PREVIOUS_PAGE_KEY) {
            slot = currentPage > 1 ? 45 : -1;
        } else if (isBound(ConfigManager.FEATURES.WARDROBE_UNEQUIP_KEY) && key == ConfigManager.FEATURES.WARDROBE_UNEQUIP_KEY) {
            slot = equippedSlot(screen);
        } else {
            int wardrobeSlot = wardrobeSlotForKey(key);
            if (wardrobeSlot >= 0) {
                slot = 36 + wardrobeSlot;
            }
        }
        if (slot < 0 || slot >= screen.getMenu().slots.size()) return false;
        if (screen.getMenu().slots.get(slot).getItem().isEmpty()) return false;

        client.gameMode.handleInventoryMouseClick(screen.getMenu().containerId, slot, GLFW.GLFW_MOUSE_BUTTON_LEFT,
                ClickType.PICKUP, client.player);
        return true;
    }

    private static int wardrobeSlotForKey(int key) {
        int[] keys = {
                ConfigManager.FEATURES.WARDROBE_SLOT_1_KEY,
                ConfigManager.FEATURES.WARDROBE_SLOT_2_KEY,
                ConfigManager.FEATURES.WARDROBE_SLOT_3_KEY,
                ConfigManager.FEATURES.WARDROBE_SLOT_4_KEY,
                ConfigManager.FEATURES.WARDROBE_SLOT_5_KEY,
                ConfigManager.FEATURES.WARDROBE_SLOT_6_KEY,
                ConfigManager.FEATURES.WARDROBE_SLOT_7_KEY,
                ConfigManager.FEATURES.WARDROBE_SLOT_8_KEY,
                ConfigManager.FEATURES.WARDROBE_SLOT_9_KEY
        };
        for (int i = 0; i < keys.length; i++) {
            if (isBound(keys[i]) && key == keys[i]) return i;
        }
        return -1;
    }

    private static boolean isBound(int key) {
        return key != GLFW.GLFW_KEY_UNKNOWN;
    }

    private static int equippedSlot(AbstractContainerScreen<?> screen) {
        for (Slot slot : screen.getMenu().slots) {
            if (EQUIPPED_SLOT.matcher(slot.getItem().getHoverName().getString()).matches()) {
                return slot.index;
            }
        }
        return -1;
    }
}
