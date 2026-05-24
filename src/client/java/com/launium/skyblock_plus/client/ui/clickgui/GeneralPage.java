package com.launium.skyblock_plus.client.ui.clickgui;

import com.launium.skyblock_plus.client.config.ConfigManager;
import com.launium.skyblock_plus.client.ui.clickgui.fubuki.list.ListView;
import com.launium.skyblock_plus.client.ui.clickgui.fubuki.list.MeasurableElement;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

public class GeneralPage extends AbstractPage {
    public GeneralPage(Minecraft client) {
        super(client, new ListView<MeasurableElement>(new ArrayList<>(List.of(
                new ModuleItemView(client, "Background blur", "Blur for this ClickGUI.", ConfigManager.GENERAL.CLICK_GUI_BLUR, newValue -> {
                    ConfigManager.GENERAL.CLICK_GUI_BLUR = newValue;
                    ConfigManager.GENERAL.markAsChanged();
                })
        )), 4F, LAYER_DEPTH + 1));
    }
}
