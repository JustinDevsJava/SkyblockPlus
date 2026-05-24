package com.justindevsjava.skyblock_plus.client.feature;

import com.justindevsjava.skyblock_plus.client.config.ConfigManager;
import com.justindevsjava.skyblock_plus.client.events.SimpleChatEventHandler;
import com.justindevsjava.skyblock_plus.client.util.PlatformNotification;
import com.justindevsjava.skyblock_plus.client.util.SkyblockLocation;
import net.minecraft.client.Minecraft;

public class EntranceNotifier implements SimpleChatEventHandler.NonOverlay {
    public static final EntranceNotifier INSTANCE = new EntranceNotifier();

    private static final String SPLIT_LINE = "-----------------------------";

    @Override
    public void onReceiveChat(String message) {
        if (ConfigManager.FEATURES.ENABLE_ENTRANCE_NOTIFIER
                && SkyblockLocation.isInHypixel()
                && !SkyblockLocation.LOCATION_STRING.isEmpty()
                && !Minecraft.getInstance().isWindowActive()
                && message.startsWith(SPLIT_LINE)
                && message.endsWith(SPLIT_LINE)
                && message.contains(" entered ")) {
            PlatformNotification.show("Game started!", message.substring(SPLIT_LINE.length(), message.length() - SPLIT_LINE.length()));
        }
    }
}
