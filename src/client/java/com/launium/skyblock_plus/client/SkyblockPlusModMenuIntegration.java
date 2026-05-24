package com.launium.skyblock_plus.client;

import com.launium.skyblock_plus.client.ui.clickgui.ClickGUIScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.Minecraft;

public class SkyblockPlusModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return screen -> new ClickGUIScreen(Minecraft.getInstance(), screen);
    }
}
