package com.justindevsjava.skyblock_plus.client.config;

import com.google.gson.annotations.SerializedName;

public class GeneralConfig extends AbstractConfig {
    static String CONFIG_NAME = "general.json";

    @Override
    protected String getConfigName() {
        return CONFIG_NAME;
    }

    @SerializedName("click_gui_blur")
    public boolean CLICK_GUI_BLUR = true;

    @SerializedName("show_feature_list")
    public boolean SHOW_FEATURE_LIST = false;

    @SerializedName("auto_update_enabled")
    public boolean AUTO_UPDATE_ENABLED = false;
}
