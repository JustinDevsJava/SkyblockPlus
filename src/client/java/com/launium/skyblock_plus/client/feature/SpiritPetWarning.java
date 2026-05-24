package com.launium.skyblock_plus.client.feature;

import com.launium.skyblock_plus.client.SkyblockPlusClient;
import com.launium.skyblock_plus.client.events.SimpleChatEventHandler;
import com.launium.skyblock_plus.client.ui.container.SpiritPetWarningContainer;
import net.minecraft.util.Util;

public class SpiritPetWarning implements SimpleChatEventHandler.NonOverlay {
    public static final SpiritPetWarning INSTANCE = new SpiritPetWarning();

    @Override
    public void onReceiveChat(String text) {
        if (text.startsWith("Your Spirit Pet hit ")) {
            SpiritPetWarningContainer.instance.lastTriggeredTimestamp = Util.getMillis();
            SkyblockPlusClient.island.show(SpiritPetWarningContainer.instance);
        }
    }
}
