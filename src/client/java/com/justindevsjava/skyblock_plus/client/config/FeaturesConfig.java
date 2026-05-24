package com.justindevsjava.skyblock_plus.client.config;

import com.google.gson.annotations.SerializedName;

public class FeaturesConfig extends AbstractConfig {
    static String CONFIG_NAME = "features.json";

    @Override
    protected String getConfigName() {
        return CONFIG_NAME;
    }

    @SerializedName("enable_day_viewer")
    public boolean ENABLE_DAY_VIEWER = false;

    @SerializedName("enable_harp_bot")
    public boolean ENABLE_HARP_BOT = false;

    @SerializedName("enable_auto_tip")
    public boolean ENABLE_AUTO_TIP = false;

    @SerializedName("enable_rng_drop_summary")
    public boolean ENABLE_RNG_DROP_SUMMARY = true;

    @SerializedName("enable_foraging_style_warning")
    public boolean ENABLE_FORAGING_STYLE_WARNING = true;

    @SerializedName("enable_entrance_notifier")
    public boolean ENABLE_ENTRANCE_NOTIFIER = true;

    @SerializedName("enable_legacy_ghost_pickaxe")
    public boolean ENABLE_LEGACY_GHOST_PICKAXE = false;

    @SerializedName("enable_force_toggle_use")
    public boolean ENABLE_FORCE_TOGGLE_USE = false;

    @SerializedName("enable_prevent_attacking_on_goons")
    public boolean ENABLE_PREVENT_ATTACKING_ON_GOONS = false;

    @SerializedName("enable_auto_sprint")
    public boolean ENABLE_AUTO_SPRINT = false;

    @SerializedName("enable_player_size")
    public boolean ENABLE_PLAYER_SIZE = false;

    @SerializedName("player_size_scale")
    public float PLAYER_SIZE_SCALE = 1.35F;

    @SerializedName("player_size_x")
    public float PLAYER_SIZE_X = 1.35F;

    @SerializedName("player_size_y")
    public float PLAYER_SIZE_Y = 1.35F;

    @SerializedName("player_size_z")
    public float PLAYER_SIZE_Z = 1.35F;

    @SerializedName("enable_etherwarp_preview")
    public boolean ENABLE_ETHERWARP_PREVIEW = true;

    @SerializedName("etherwarp_show_failed")
    public boolean ETHERWARP_SHOW_FAILED = true;

    @SerializedName("enable_auto_experimentation")
    public boolean ENABLE_AUTO_EXPERIMENTATION = false;

    @SerializedName("enable_extra_stats")
    public boolean ENABLE_EXTRA_STATS = false;

    @SerializedName("enable_leap_menu")
    public boolean ENABLE_LEAP_MENU = false;

    @SerializedName("enable_map_info")
    public boolean ENABLE_MAP_INFO = false;

    @SerializedName("enable_puzzle_solvers")
    public boolean ENABLE_PUZZLE_SOLVERS = false;

    @SerializedName("enable_wardrobe_keybinds")
    public boolean ENABLE_WARDROBE_KEYBINDS = false;

    @SerializedName("wardrobe_next_page_key")
    public int WARDROBE_NEXT_PAGE_KEY = org.lwjgl.glfw.GLFW.GLFW_KEY_UNKNOWN;

    @SerializedName("wardrobe_previous_page_key")
    public int WARDROBE_PREVIOUS_PAGE_KEY = org.lwjgl.glfw.GLFW.GLFW_KEY_UNKNOWN;

    @SerializedName("wardrobe_unequip_key")
    public int WARDROBE_UNEQUIP_KEY = org.lwjgl.glfw.GLFW.GLFW_KEY_UNKNOWN;

    @SerializedName("wardrobe_slot_1_key")
    public int WARDROBE_SLOT_1_KEY = org.lwjgl.glfw.GLFW.GLFW_KEY_UNKNOWN;

    @SerializedName("wardrobe_slot_2_key")
    public int WARDROBE_SLOT_2_KEY = org.lwjgl.glfw.GLFW.GLFW_KEY_UNKNOWN;

    @SerializedName("wardrobe_slot_3_key")
    public int WARDROBE_SLOT_3_KEY = org.lwjgl.glfw.GLFW.GLFW_KEY_UNKNOWN;

    @SerializedName("wardrobe_slot_4_key")
    public int WARDROBE_SLOT_4_KEY = org.lwjgl.glfw.GLFW.GLFW_KEY_UNKNOWN;

    @SerializedName("wardrobe_slot_5_key")
    public int WARDROBE_SLOT_5_KEY = org.lwjgl.glfw.GLFW.GLFW_KEY_UNKNOWN;

    @SerializedName("wardrobe_slot_6_key")
    public int WARDROBE_SLOT_6_KEY = org.lwjgl.glfw.GLFW.GLFW_KEY_UNKNOWN;

    @SerializedName("wardrobe_slot_7_key")
    public int WARDROBE_SLOT_7_KEY = org.lwjgl.glfw.GLFW.GLFW_KEY_UNKNOWN;

    @SerializedName("wardrobe_slot_8_key")
    public int WARDROBE_SLOT_8_KEY = org.lwjgl.glfw.GLFW.GLFW_KEY_UNKNOWN;

    @SerializedName("wardrobe_slot_9_key")
    public int WARDROBE_SLOT_9_KEY = org.lwjgl.glfw.GLFW.GLFW_KEY_UNKNOWN;

    @SerializedName("enable_hide_players")
    public boolean ENABLE_HIDE_PLAYERS = false;

    @SerializedName("hide_players_only_dungeons")
    public boolean HIDE_PLAYERS_ONLY_DUNGEONS = false;

    @SerializedName("hide_players_all")
    public boolean HIDE_PLAYERS_ALL = false;

    @SerializedName("hide_players_distance")
    public float HIDE_PLAYERS_DISTANCE = 3F;

    @SerializedName("enable_terminal_simulator")
    public boolean ENABLE_TERMINAL_SIMULATOR = false;

    @SerializedName("enable_terminal_solver")
    public boolean ENABLE_TERMINAL_SOLVER = false;

    @SerializedName("enable_terminal_sounds")
    public boolean ENABLE_TERMINAL_SOUNDS = false;

    @SerializedName("enable_terminal_times")
    public boolean ENABLE_TERMINAL_TIMES = false;

    @SerializedName("enable_terminal_titles")
    public boolean ENABLE_TERMINAL_TITLES = false;
}
