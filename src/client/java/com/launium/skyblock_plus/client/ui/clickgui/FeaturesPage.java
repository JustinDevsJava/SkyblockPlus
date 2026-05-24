package com.launium.skyblock_plus.client.ui.clickgui;

import com.launium.skyblock_plus.client.SkyblockPlusClient;
import com.launium.skyblock_plus.client.config.ConfigManager;
import com.launium.skyblock_plus.client.feature.AutoUpdater;
import com.launium.skyblock_plus.client.feature.AutoTip;
import com.launium.skyblock_plus.client.feature.DayViewer;
import com.launium.skyblock_plus.client.feature.HarpBot;
import com.launium.skyblock_plus.client.ui.Easy2D;
import com.launium.skyblock_plus.client.ui.animation.Smooth;
import com.launium.skyblock_plus.client.ui.clickgui.fubuki.list.ListView;
import com.launium.skyblock_plus.client.ui.clickgui.fubuki.list.MeasurableElement;
import com.launium.skyblock_plus.client.ui.font.FontManager;
import com.launium.skyblock_plus.client.ui.font.RenderInfo;
import com.launium.skyblock_plus.client.ui.font.RenderedText;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

public class FeaturesPage extends AbstractPage {
    private static final float SEARCH_HEIGHT = 25F;
    private static final float SIDEBAR_WIDTH = 156F;
    private static final float SIDEBAR_GAP = 16F;
    private static final float SIDE_PANEL_WIDTH = 212F;
    private static final float SIDE_PANEL_GAP = 14F;
    private static final float CATEGORY_HEIGHT = 28F;
    private static final float CATEGORY_GAP = 8F;
    private static final float SIDEBAR_FOOTER_HEIGHT = 45F;

    private final Minecraft client;
    private final List<FeatureEntry> entries;
    private final List<MeasurableElement> visibleEntries = new ArrayList<>();
    private final List<String> categories = new ArrayList<>();
    private final Smooth sidePanelProgress = new Smooth(0F, 0F);
    private final Smooth sidebarScroll = new Smooth(0F, 0F);
    private final Smooth sideOptionsScroll = new Smooth(0F, 0F);
    private String selectedFeature = null;
    private String selectedCategory = "General";
    private String search = "";
    private boolean searchFocused = false;
    private float contentStartX, contentEndX, sideStartX, sideStartY, sideEndX, sideEndY;

    public FeaturesPage(Minecraft client) {
        super(client, new ListView<>(new ArrayList<>(), 5F, LAYER_DEPTH + 1));
        this.client = client;
        this.entries = createEntries(client);
        collectCategories();
        applySearch();
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, long timeDiff) {
        sidePanelProgress.update(selectedFeature == null ? 0F : 1F);
        sidePanelProgress.tick(timeDiff / 120F);
        sidebarScroll.update(clampSidebarScroll(sidebarScroll.target));
        sidebarScroll.tick(timeDiff * 0.05F);
        sidebarScroll.current = clampSidebarScroll(sidebarScroll.current);
        sideOptionsScroll.update(clampSideOptionsScroll(sideOptionsScroll.target));
        sideOptionsScroll.tick(timeDiff * 0.05F);
        sideOptionsScroll.current = clampSideOptionsScroll(sideOptionsScroll.current);
        renderSidebar(context, mouseX, mouseY);
        super.render(context, mouseX, mouseY, timeDiff);
        renderSidePanel(context, mouseX, mouseY, timeDiff);
    }

    @Override
    public void updateStartPosition(float newX, float newY) {
        this.startX = newX;
        this.startY = newY;
        updateLayout();
    }

    @Override
    public void updateEndPosition(float newX, float newY) {
        this.endX = newX;
        this.endY = newY;
        updateLayout();
    }

    @Override
    public boolean mouseClicked(float mouseX, float mouseY) {
        searchFocused = isInSearch(mouseX, mouseY);
        if (searchFocused) return true;
        if (clickCategory(mouseX, mouseY)) return true;
        if (selectedFeature != null && sideStartX <= mouseX && mouseX <= sideEndX && sideStartY <= mouseY && mouseY <= sideEndY) {
            return clickSidePanel(mouseX, mouseY);
        }
        return super.mouseClicked(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(float mouseX, float mouseY, int button) {
        searchFocused = isInSearch(mouseX, mouseY);
        if (searchFocused) return true;
        if (clickCategory(mouseX, mouseY)) return true;
        if (selectedFeature != null && sideStartX <= mouseX && mouseX <= sideEndX && sideStartY <= mouseY && mouseY <= sideEndY) {
            return clickSidePanel(mouseX, mouseY);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(float mouseX, float mouseY, float dragX, float dragY) {
        if (sideStartX <= mouseX && mouseX <= sideEndX && selectedFeature != null) {
            float currentY = sideOptionsStartY() + sideOptionsScroll.current;
            for (FeatureEntry entry : entries) {
                if (!entry.option || !selectedFeature.equals(entry.parent)) continue;
                entry.view.updateStartPosition(sideStartX + 12F, currentY);
                entry.view.updateEndPosition(sideEndX - 12F, currentY + entry.view.measureHeight());
                if (entry.view.mouseDragged(mouseX, mouseY, dragX, dragY)) return true;
                currentY += entry.view.measureHeight() + 7F;
            }
        }
        return super.mouseDragged(mouseX, mouseY, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(float mouseX, float mouseY, float scrollX, float scrollY) {
        if (isInSideOptions(mouseX, mouseY)) {
            sideOptionsScroll.target = clampSideOptionsScroll(sideOptionsScroll.target + scrollY * 14F);
            return true;
        }
        if (isInSidebarCategories(mouseX, mouseY)) {
            sidebarScroll.target = clampSidebarScroll(sidebarScroll.target + scrollY * 14F);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (selectedFeature != null) {
            for (FeatureEntry entry : entries) {
                if (entry.option && selectedFeature.equals(entry.parent) && entry.view.keyPressed(event)) return true;
            }
        }
        if (!searchFocused) return false;
        if (event.key() == GLFW.GLFW_KEY_BACKSPACE && !search.isEmpty()) {
            search = search.substring(0, search.length() - 1);
            applySearch();
            return true;
        }
        if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
            searchFocused = false;
            return false;
        }
        return true;
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (!searchFocused || !event.isAllowedChatCharacter()) return false;
        search += event.codepointAsString();
        applySearch();
        return true;
    }

    private void renderSidebar(GuiGraphics context, int mouseX, int mouseY) {
        Easy2D.drawRoundRect(startX, startY, startX + SIDEBAR_WIDTH, endY, 7F, 2F, 0x49100F17, 0x00000000);
        context.fill(Mth.floor(startX + SIDEBAR_WIDTH), Mth.floor(startY + 2F), Mth.ceil(startX + SIDEBAR_WIDTH + 1F), Mth.ceil(endY - 2F), 0x223B354D);

        float searchEndX = startX + SIDEBAR_WIDTH;
        boolean hovered = isInSearch(mouseX, mouseY);
        Easy2D.drawRoundRect(startX, startY, searchEndX, startY + SEARCH_HEIGHT, 5F, 0F,
                hovered || searchFocused ? 0x55181622 : 0x44121118, 0x00000000);

        float scale = (float) client.getWindow().getGuiScale();
        String value = search.isEmpty() ? "Search features..." : search;
        int color = search.isEmpty() ? 0xFF7D768D : 0xFFF6F3FF;
        RenderedText text = FontManager.requestRenderedText(new RenderInfo(FontManager.DEFAULT_FONT, value, 8F), scale);
        text.draw(context, startX + 10F, startY + 8F, scale, color);

        float categoryTop = categoryStartY();
        float categoryBottom = categoryEndY();
        context.enableScissor(Mth.floor(startX), Mth.floor(categoryTop), Mth.ceil(startX + SIDEBAR_WIDTH), Mth.ceil(categoryBottom));
        float currentY = categoryTop + sidebarScroll.current;
        for (String category : categories) {
            boolean selected = category.equals(selectedCategory) && search.trim().isEmpty();
            boolean categoryHovered = startX <= mouseX && mouseX <= startX + SIDEBAR_WIDTH
                    && currentY <= mouseY && mouseY <= currentY + CATEGORY_HEIGHT;
            if (selected || categoryHovered) {
                Easy2D.drawRoundRect(startX, currentY, startX + SIDEBAR_WIDTH, currentY + CATEGORY_HEIGHT,
                        5F, 0F, selected ? 0x5A171421 : 0x33171621, 0x00000000);
            }
            RenderedText categoryText = FontManager.requestRenderedText(new RenderInfo(FontManager.BOLD_FONT, category, 8F), scale);
            categoryText.draw(context, startX + 14F, currentY + 9F, scale, selected ? 0xFFF6F3FF : 0xFFB6B0C1);
            currentY += CATEGORY_HEIGHT + CATEGORY_GAP;
        }
        context.disableScissor();

        RenderedText brandText = FontManager.requestRenderedText(new RenderInfo(FontManager.BOLD_FONT, "Skyblock+", 10F), scale);
        brandText.draw(context, startX + 14F, endY - 28F, scale, 0xFFEDEAF5);
    }

    private void applySearch() {
        String needle = search.trim().toLowerCase(Locale.ROOT);
        visibleEntries.clear();
        for (FeatureEntry entry : entries) {
            if (entry.option) continue;
            boolean categoryMatches = needle.isEmpty() && entry.category.equals(selectedCategory);
            boolean searchMatches = !needle.isEmpty() && entry.matches(needle);
            if (categoryMatches || searchMatches) {
                if (entry.view instanceof ModuleItemView moduleItemView) {
                    moduleItemView.setOptionsOpen(entry.title.equals(selectedFeature));
                }
                visibleEntries.add(entry.view);
            }
        }
        listView.elementList = visibleEntries;
        scrollWrapper.clampVerticalOffset();
    }

    private void openOptions(String feature) {
        selectedFeature = feature.equals(selectedFeature) ? null : feature;
        sideOptionsScroll.reset(0F, 0F);
        updateLayout();
        applySearch();
    }

    private void updateLayout() {
        this.contentStartX = startX + SIDEBAR_WIDTH + SIDEBAR_GAP;
        this.sideEndX = endX;
        float availableWidth = Math.max(0F, sideEndX - contentStartX);
        float panelWidth = Math.min(SIDE_PANEL_WIDTH, Math.max(152F, availableWidth * 0.44F));
        this.sideStartX = selectedFeature == null ? sideEndX : sideEndX - panelWidth;
        this.contentEndX = selectedFeature == null ? endX : Math.max(contentStartX, sideStartX - SIDE_PANEL_GAP);
        this.sideStartY = startY;
        this.sideEndY = endY;
        scrollWrapper.updateStartPosition(contentStartX, startY);
        scrollWrapper.updateEndPosition(contentEndX, endY);
    }

    private boolean isInSearch(float mouseX, float mouseY) {
        return startX <= mouseX && mouseX <= startX + SIDEBAR_WIDTH && startY <= mouseY && mouseY <= startY + SEARCH_HEIGHT;
    }

    private boolean clickCategory(float mouseX, float mouseY) {
        if (!isInSidebarCategories(mouseX, mouseY)) return false;
        float currentY = categoryStartY() + sidebarScroll.current;
        for (String category : categories) {
            if (startX <= mouseX && mouseX <= startX + SIDEBAR_WIDTH && currentY <= mouseY && mouseY <= currentY + CATEGORY_HEIGHT) {
                selectedCategory = category;
                search = "";
                searchFocused = false;
                selectedFeature = null;
                updateLayout();
                applySearch();
                return true;
            }
            currentY += CATEGORY_HEIGHT + CATEGORY_GAP;
        }
        return false;
    }

    private boolean isInSidebarCategories(float mouseX, float mouseY) {
        return startX <= mouseX && mouseX <= startX + SIDEBAR_WIDTH
                && categoryStartY() <= mouseY && mouseY <= categoryEndY();
    }

    private float categoryStartY() {
        return startY + SEARCH_HEIGHT + 15F;
    }

    private float categoryEndY() {
        return Math.max(categoryStartY(), endY - SIDEBAR_FOOTER_HEIGHT);
    }

    private float clampSidebarScroll(float offset) {
        float viewportHeight = categoryEndY() - categoryStartY();
        float contentHeight = categories.isEmpty() ? 0F : categories.size() * CATEGORY_HEIGHT + (categories.size() - 1) * CATEGORY_GAP;
        float minOffset = Math.min(0F, viewportHeight - contentHeight);
        return Mth.clamp(offset, minOffset, 0F);
    }

    private void renderSidePanel(GuiGraphics context, int mouseX, int mouseY, long timeDiff) {
        if (selectedFeature == null || sidePanelProgress.current <= 0.01F) return;
        float panelWidth = sideEndX - sideStartX;
        float offsetX = (1F - sidePanelProgress.current) * (panelWidth + SIDE_PANEL_GAP);
        float renderedStartX = sideStartX + offsetX;
        float renderedEndX = sideEndX + offsetX;
        context.enableScissor(Mth.floor(sideStartX), Mth.floor(sideStartY), Mth.ceil(sideEndX), Mth.ceil(sideEndY));
        Easy2D.drawRoundRect(renderedStartX, sideStartY, renderedEndX, sideEndY, 10F, 7F,
                0x6F554B70, 0x00000000);
        Easy2D.drawRoundRect(renderedStartX + 1F, sideStartY + 1F, renderedEndX - 1F, sideEndY - 1F, 9F, 0F,
                0xF00C0B12, 0x00000000);
        float scale = (float) client.getWindow().getGuiScale();
        RenderedText titleText = FontManager.requestRenderedText(new RenderInfo(FontManager.BOLD_FONT, selectedFeature, 9F), scale);
        titleText.draw(context, renderedStartX + 14F, sideStartY + 14F, scale, 0xFFF6F3FF);
        RenderedText subtitleText = FontManager.requestRenderedText(new RenderInfo(FontManager.DEFAULT_FONT, "Options", 7F), scale);
        subtitleText.draw(context, renderedStartX + 14F, sideStartY + 30F, scale, 0xFF9B96A7);
        float optionTop = sideOptionsStartY();
        context.enableScissor(Mth.floor(renderedStartX + 1F), Mth.floor(optionTop), Mth.ceil(renderedEndX - 1F), Mth.ceil(sideEndY - 8F));
        float currentY = optionTop + sideOptionsScroll.current;
        for (FeatureEntry entry : entries) {
            if (!entry.option || !selectedFeature.equals(entry.parent)) continue;
            entry.view.updateStartPosition(renderedStartX + 12F, currentY);
            entry.view.updateEndPosition(renderedEndX - 12F, currentY + entry.view.measureHeight());
            entry.view.render(context, mouseX, mouseY, timeDiff);
            currentY += entry.view.measureHeight() + 7F;
        }
        context.disableScissor();
        context.disableScissor();
    }

    private boolean clickSidePanel(float mouseX, float mouseY) {
        if (selectedFeature == null) return true;
        if (!isInSideOptions(mouseX, mouseY)) return true;
        float currentY = sideOptionsStartY() + sideOptionsScroll.current;
        for (FeatureEntry entry : entries) {
            if (!entry.option || !selectedFeature.equals(entry.parent)) continue;
            entry.view.updateStartPosition(sideStartX + 12F, currentY);
            entry.view.updateEndPosition(sideEndX - 12F, currentY + entry.view.measureHeight());
            if (entry.view.mouseClicked(mouseX, mouseY)) return true;
            currentY += entry.view.measureHeight() + 7F;
        }
        return true;
    }

    private boolean isInSideOptions(float mouseX, float mouseY) {
        return selectedFeature != null
                && sideStartX <= mouseX && mouseX <= sideEndX
                && sideOptionsStartY() <= mouseY && mouseY <= sideEndY - 8F;
    }

    private float sideOptionsStartY() {
        return sideStartY + 52F;
    }

    private float clampSideOptionsScroll(float offset) {
        if (selectedFeature == null) return 0F;
        float viewportHeight = Math.max(0F, sideEndY - 8F - sideOptionsStartY());
        float contentHeight = 0F;
        for (FeatureEntry entry : entries) {
            if (!entry.option || !selectedFeature.equals(entry.parent)) continue;
            contentHeight += entry.view.measureHeight() + 7F;
        }
        if (contentHeight > 0F) contentHeight -= 7F;
        float minOffset = Math.min(0F, viewportHeight - contentHeight);
        return Mth.clamp(offset, minOffset, 0F);
    }

    private List<FeatureEntry> createEntries(Minecraft client) {
        List<FeatureEntry> list = new ArrayList<>();
        add(list, "General", new ModuleItemView(client, "Background blur", "Blur for this ClickGUI.", ConfigManager.GENERAL.CLICK_GUI_BLUR, newValue -> {
            ConfigManager.GENERAL.CLICK_GUI_BLUR = newValue;
            ConfigManager.GENERAL.markAsChanged();
        }));
        add(list, "General", new ModuleItemView(client, "Feature list HUD", "Show active feature pills on the top-right HUD.", ConfigManager.GENERAL.SHOW_FEATURE_LIST, newValue -> {
            ConfigManager.GENERAL.SHOW_FEATURE_LIST = newValue;
            ConfigManager.GENERAL.markAsChanged();
        }));
        add(list, "General", new ModuleItemView(client, "Auto updater", "Checks GitHub releases and installs staged updates after restart.", ConfigManager.GENERAL.AUTO_UPDATE_ENABLED, newValue -> {
            ConfigManager.GENERAL.AUTO_UPDATE_ENABLED = newValue;
            ConfigManager.GENERAL.markAsChanged();
            AutoUpdater.setEnabledState(newValue);
            if (newValue) {
                AutoUpdater.checkForUpdatesAsync(true);
            }
        }));
        add(list, "HUD", new ModuleItemView(client, "Day viewer", "Display the date of the current world.", ConfigManager.FEATURES.ENABLE_DAY_VIEWER, newValue -> {
            ConfigManager.FEATURES.ENABLE_DAY_VIEWER = newValue;
            if (ConfigManager.FEATURES.ENABLE_DAY_VIEWER) SkyblockPlusClient.moduleList.showModule(DayViewer.INSTANCE);
            ConfigManager.FEATURES.markAsChanged();
        }));
        add(list, "Automation", new ModuleItemView(client, "Harp bot", "Rhythm games should have autoplay.", ConfigManager.FEATURES.ENABLE_HARP_BOT, newValue -> {
            ConfigManager.FEATURES.ENABLE_HARP_BOT = newValue;
            if (ConfigManager.FEATURES.ENABLE_HARP_BOT) SkyblockPlusClient.moduleList.showModule(HarpBot.INSTANCE);
            ConfigManager.FEATURES.markAsChanged();
        }));
        add(list, "Automation", new ModuleItemView(client, "Auto tip", "Send /tipall regularly when in Hypixel.", ConfigManager.FEATURES.ENABLE_AUTO_TIP, newValue -> {
            ConfigManager.FEATURES.ENABLE_AUTO_TIP = newValue;
            if (ConfigManager.FEATURES.ENABLE_AUTO_TIP) {
                AutoTip.INSTANCE.setupTask();
                SkyblockPlusClient.moduleList.showModule(AutoTip.INSTANCE);
            }
            ConfigManager.FEATURES.markAsChanged();
        }));
        add(list, "Automation", new ModuleItemView(client, "Auto experimentation", "Solves Chronomatron and Ultrasequencer clicks.", ConfigManager.FEATURES.ENABLE_AUTO_EXPERIMENTATION, newValue -> {
            ConfigManager.FEATURES.ENABLE_AUTO_EXPERIMENTATION = newValue;
            ConfigManager.FEATURES.markAsChanged();
        }));
        add(list, "Notifications", new ModuleItemView(client, "RNG drop summary", "Notify RNG drops and play config/Skyblock+/rng_music.ogg.", ConfigManager.FEATURES.ENABLE_RNG_DROP_SUMMARY, newValue -> {
            ConfigManager.FEATURES.ENABLE_RNG_DROP_SUMMARY = newValue;
            ConfigManager.FEATURES.markAsChanged();
        }));
        add(list, "Notifications", new ModuleItemView(client, "Entrance notifier", "Send a system notification when entering Dungeon or Kuudra while unfocused.", ConfigManager.FEATURES.ENABLE_ENTRANCE_NOTIFIER, newValue -> {
            ConfigManager.FEATURES.ENABLE_ENTRANCE_NOTIFIER = newValue;
            ConfigManager.FEATURES.markAsChanged();
        }));
        add(list, "Gameplay", new ModuleItemView(client, "Foraging style warning", "Warn when you chop trees in the wrong order.", ConfigManager.FEATURES.ENABLE_FORAGING_STYLE_WARNING, newValue -> {
            ConfigManager.FEATURES.ENABLE_FORAGING_STYLE_WARNING = newValue;
            ConfigManager.FEATURES.markAsChanged();
        }));
        add(list, "Gameplay", new ModuleItemView(client, "Allow legacy Ghost Pickaxe", "Allow legacy client-side block removal mode.", ConfigManager.FEATURES.ENABLE_LEGACY_GHOST_PICKAXE, newValue -> {
            ConfigManager.FEATURES.ENABLE_LEGACY_GHOST_PICKAXE = newValue;
            ConfigManager.FEATURES.markAsChanged();
        }));
        add(list, "Gameplay", new ModuleItemView(client, "Force toggle use on specific items", "Always use toggle mode when holding Tribal Spear and similar items.", ConfigManager.FEATURES.ENABLE_FORCE_TOGGLE_USE, newValue -> {
            ConfigManager.FEATURES.ENABLE_FORCE_TOGGLE_USE = newValue;
            ConfigManager.FEATURES.markAsChanged();
        }));
        add(list, "Gameplay", new ModuleItemView(client, "Prevent attacking on Goons", "Prevent accidental attacks on Goons.", ConfigManager.FEATURES.ENABLE_PREVENT_ATTACKING_ON_GOONS, newValue -> {
            ConfigManager.FEATURES.ENABLE_PREVENT_ATTACKING_ON_GOONS = newValue;
            ConfigManager.FEATURES.markAsChanged();
        }));
        add(list, "Movement", new ModuleItemView(client, "Auto sprint", "Automatically sprint while moving.", ConfigManager.FEATURES.ENABLE_AUTO_SPRINT, newValue -> {
            ConfigManager.FEATURES.ENABLE_AUTO_SPRINT = newValue;
            ConfigManager.FEATURES.markAsChanged();
        }));
        add(list, "Render", new ModuleItemView(client, "Player size", "Scale your local player model client-side.", ConfigManager.FEATURES.ENABLE_PLAYER_SIZE, newValue -> {
            ConfigManager.FEATURES.ENABLE_PLAYER_SIZE = newValue;
            ConfigManager.FEATURES.markAsChanged();
        }).withOptions(() -> openOptions("Player size")));
        addOption(list, "Render", "Player size", "Player size X", new NumberItemView(client, "Player size X",
                () -> ConfigManager.FEATURES.PLAYER_SIZE_X,
                newValue -> {
                    ConfigManager.FEATURES.PLAYER_SIZE_X = newValue;
                    ConfigManager.FEATURES.markAsChanged();
                }, 0.5F, 2.5F, 0.05F));
        addOption(list, "Render", "Player size", "Player size Y", new NumberItemView(client, "Player size Y",
                () -> ConfigManager.FEATURES.PLAYER_SIZE_Y,
                newValue -> {
                    ConfigManager.FEATURES.PLAYER_SIZE_Y = newValue;
                    ConfigManager.FEATURES.markAsChanged();
                }, 0.5F, 2.5F, 0.05F));
        addOption(list, "Render", "Player size", "Player size Z", new NumberItemView(client, "Player size Z",
                () -> ConfigManager.FEATURES.PLAYER_SIZE_Z,
                newValue -> {
                    ConfigManager.FEATURES.PLAYER_SIZE_Z = newValue;
                    ConfigManager.FEATURES.markAsChanged();
                }, 0.5F, 2.5F, 0.05F));
        add(list, "Render", new ModuleItemView(client, "Etherwarp preview", "Show the block Etherwarp will target.", ConfigManager.FEATURES.ENABLE_ETHERWARP_PREVIEW, newValue -> {
            ConfigManager.FEATURES.ENABLE_ETHERWARP_PREVIEW = newValue;
            ConfigManager.FEATURES.markAsChanged();
        }).withOptions(() -> openOptions("Etherwarp preview")));
        addOption(list, "Render", "Etherwarp preview", new ModuleItemView(client, "Etherwarp failed targets", "Show red target boxes when Etherwarp cannot land.", ConfigManager.FEATURES.ETHERWARP_SHOW_FAILED, newValue -> {
            ConfigManager.FEATURES.ETHERWARP_SHOW_FAILED = newValue;
            ConfigManager.FEATURES.markAsChanged();
        }));
        add(list, "Render", new ModuleItemView(client, "Hide players", "Hides nearby players from your view.", ConfigManager.FEATURES.ENABLE_HIDE_PLAYERS, newValue -> {
            ConfigManager.FEATURES.ENABLE_HIDE_PLAYERS = newValue;
            ConfigManager.FEATURES.markAsChanged();
        }).withOptions(() -> openOptions("Hide players")));
        addOption(list, "Render", "Hide players", new ModuleItemView(client, "Only in dungeons", "Only hide players while in dungeons.", ConfigManager.FEATURES.HIDE_PLAYERS_ONLY_DUNGEONS, newValue -> {
            ConfigManager.FEATURES.HIDE_PLAYERS_ONLY_DUNGEONS = newValue;
            ConfigManager.FEATURES.markAsChanged();
        }));
        addOption(list, "Render", "Hide players", new ModuleItemView(client, "Hide everyone", "Ignore distance and hide every other player.", ConfigManager.FEATURES.HIDE_PLAYERS_ALL, newValue -> {
            ConfigManager.FEATURES.HIDE_PLAYERS_ALL = newValue;
            ConfigManager.FEATURES.markAsChanged();
        }));
        addOption(list, "Render", "Hide players", "Hide distance", new NumberItemView(client, "Hide distance",
                () -> ConfigManager.FEATURES.HIDE_PLAYERS_DISTANCE,
                newValue -> {
                    ConfigManager.FEATURES.HIDE_PLAYERS_DISTANCE = newValue;
                    ConfigManager.FEATURES.markAsChanged();
                }, 1F, 32F, 0.5F));
        add(list, "Render", new ModuleItemView(client, "Wardrobe Keybinds", "WARNING: bannable. Keybind wardrobe navigation.", ConfigManager.FEATURES.ENABLE_WARDROBE_KEYBINDS, newValue -> {
            ConfigManager.FEATURES.ENABLE_WARDROBE_KEYBINDS = newValue;
            ConfigManager.FEATURES.markAsChanged();
        }).withOptions(() -> openOptions("Wardrobe Keybinds")));
        addOption(list, "Render", "Wardrobe Keybinds", "Wardrobe warning",
                new WarningItemView(client, "Bannable", "Use at your own risk on Hypixel."));
        addWardrobeKeybind(list, "Next page", () -> ConfigManager.FEATURES.WARDROBE_NEXT_PAGE_KEY, newValue -> ConfigManager.FEATURES.WARDROBE_NEXT_PAGE_KEY = newValue);
        addWardrobeKeybind(list, "Previous page", () -> ConfigManager.FEATURES.WARDROBE_PREVIOUS_PAGE_KEY, newValue -> ConfigManager.FEATURES.WARDROBE_PREVIOUS_PAGE_KEY = newValue);
        addWardrobeKeybind(list, "Unequip", () -> ConfigManager.FEATURES.WARDROBE_UNEQUIP_KEY, newValue -> ConfigManager.FEATURES.WARDROBE_UNEQUIP_KEY = newValue);
        addWardrobeKeybind(list, "Wardrobe 1", () -> ConfigManager.FEATURES.WARDROBE_SLOT_1_KEY, newValue -> ConfigManager.FEATURES.WARDROBE_SLOT_1_KEY = newValue);
        addWardrobeKeybind(list, "Wardrobe 2", () -> ConfigManager.FEATURES.WARDROBE_SLOT_2_KEY, newValue -> ConfigManager.FEATURES.WARDROBE_SLOT_2_KEY = newValue);
        addWardrobeKeybind(list, "Wardrobe 3", () -> ConfigManager.FEATURES.WARDROBE_SLOT_3_KEY, newValue -> ConfigManager.FEATURES.WARDROBE_SLOT_3_KEY = newValue);
        addWardrobeKeybind(list, "Wardrobe 4", () -> ConfigManager.FEATURES.WARDROBE_SLOT_4_KEY, newValue -> ConfigManager.FEATURES.WARDROBE_SLOT_4_KEY = newValue);
        addWardrobeKeybind(list, "Wardrobe 5", () -> ConfigManager.FEATURES.WARDROBE_SLOT_5_KEY, newValue -> ConfigManager.FEATURES.WARDROBE_SLOT_5_KEY = newValue);
        addWardrobeKeybind(list, "Wardrobe 6", () -> ConfigManager.FEATURES.WARDROBE_SLOT_6_KEY, newValue -> ConfigManager.FEATURES.WARDROBE_SLOT_6_KEY = newValue);
        addWardrobeKeybind(list, "Wardrobe 7", () -> ConfigManager.FEATURES.WARDROBE_SLOT_7_KEY, newValue -> ConfigManager.FEATURES.WARDROBE_SLOT_7_KEY = newValue);
        addWardrobeKeybind(list, "Wardrobe 8", () -> ConfigManager.FEATURES.WARDROBE_SLOT_8_KEY, newValue -> ConfigManager.FEATURES.WARDROBE_SLOT_8_KEY = newValue);
        addWardrobeKeybind(list, "Wardrobe 9", () -> ConfigManager.FEATURES.WARDROBE_SLOT_9_KEY, newValue -> ConfigManager.FEATURES.WARDROBE_SLOT_9_KEY = newValue);
        add(list, "Dungeon", new ModuleItemView(client, "Leap Menu", "Custom leap menu for Spirit Leap.", ConfigManager.FEATURES.ENABLE_LEAP_MENU, newValue -> {
            ConfigManager.FEATURES.ENABLE_LEAP_MENU = newValue;
            ConfigManager.FEATURES.markAsChanged();
        }));
        add(list, "Dungeon", new ModuleItemView(client, "Map Info", "Displays score, secrets, deaths, and dungeon stats.", ConfigManager.FEATURES.ENABLE_MAP_INFO, newValue -> {
            ConfigManager.FEATURES.ENABLE_MAP_INFO = newValue;
            ConfigManager.FEATURES.markAsChanged();
        }));
        add(list, "Dungeon", new ModuleItemView(client, "Puzzle Solvers", "Displays solutions for dungeon puzzles.", ConfigManager.FEATURES.ENABLE_PUZZLE_SOLVERS, newValue -> {
            ConfigManager.FEATURES.ENABLE_PUZZLE_SOLVERS = newValue;
            ConfigManager.FEATURES.markAsChanged();
        }));
        add(list, "Boss", new ModuleItemView(client, "Extra Stats", "Shows extra dungeon stats at run end.", ConfigManager.FEATURES.ENABLE_EXTRA_STATS, newValue -> {
            ConfigManager.FEATURES.ENABLE_EXTRA_STATS = newValue;
            ConfigManager.FEATURES.markAsChanged();
        }));
        add(list, "Boss", new ModuleItemView(client, "Terminal Simulator", "Practice Floor 7 terminals locally.", ConfigManager.FEATURES.ENABLE_TERMINAL_SIMULATOR, newValue -> {
            ConfigManager.FEATURES.ENABLE_TERMINAL_SIMULATOR = newValue;
            ConfigManager.FEATURES.markAsChanged();
        }));
        add(list, "Boss", new ModuleItemView(client, "Terminal Solver", "Renders solutions for Floor 7 terminals.", ConfigManager.FEATURES.ENABLE_TERMINAL_SOLVER, newValue -> {
            ConfigManager.FEATURES.ENABLE_TERMINAL_SOLVER = newValue;
            ConfigManager.FEATURES.markAsChanged();
        }));
        add(list, "Boss", new ModuleItemView(client, "Terminal Sounds", "Plays sounds for correct terminal clicks.", ConfigManager.FEATURES.ENABLE_TERMINAL_SOUNDS, newValue -> {
            ConfigManager.FEATURES.ENABLE_TERMINAL_SOUNDS = newValue;
            ConfigManager.FEATURES.markAsChanged();
        }));
        add(list, "Boss", new ModuleItemView(client, "Terminal Times", "Records Floor 7 terminal completion times.", ConfigManager.FEATURES.ENABLE_TERMINAL_TIMES, newValue -> {
            ConfigManager.FEATURES.ENABLE_TERMINAL_TIMES = newValue;
            ConfigManager.FEATURES.markAsChanged();
        }));
        add(list, "Boss", new ModuleItemView(client, "Terminal Titles", "Custom terminal completion titles.", ConfigManager.FEATURES.ENABLE_TERMINAL_TITLES, newValue -> {
            ConfigManager.FEATURES.ENABLE_TERMINAL_TITLES = newValue;
            ConfigManager.FEATURES.markAsChanged();
        }));
        add(list, "Patches", new ModuleItemView(client, "0 Ping Dungeonbreaker", "Ignore mining fatigue when holding Dungeonbreaker in Dungeons.", ConfigManager.PATCHES.ZERO_PING_DUNGEONBREAKER, newValue -> {
            ConfigManager.PATCHES.ZERO_PING_DUNGEONBREAKER = newValue;
            ConfigManager.PATCHES.markAsChanged();
        }));
        add(list, "Patches", new ModuleItemView(client, "Always use spectator fog", "Clear in-lava and in-powder-snow camera fog.", ConfigManager.PATCHES.USE_SPECTATOR_FOG, newValue -> {
            ConfigManager.PATCHES.USE_SPECTATOR_FOG = newValue;
            ConfigManager.PATCHES.markAsChanged();
        }));
        add(list, "Patches", new ModuleItemView(client, "Remove suffocation screen", "Remove the block texture overlay when suffocating.", ConfigManager.PATCHES.REMOVE_SUFFOCATION_SCREEN, newValue -> {
            ConfigManager.PATCHES.REMOVE_SUFFOCATION_SCREEN = newValue;
            ConfigManager.PATCHES.markAsChanged();
        }));
        add(list, "Patches", new ModuleItemView(client, "Cancel shortbow pull animation", "Avoid pulling shortbows when arrows are in your inventory.", ConfigManager.PATCHES.CANCEL_SHORTBOW_PULL, newValue -> {
            ConfigManager.PATCHES.CANCEL_SHORTBOW_PULL = newValue;
            ConfigManager.PATCHES.markAsChanged();
        }));
        add(list, "Patches", new ModuleItemView(client, "Fix dungeon block place", "Allow placing onto enchantment tables in Dungeons.", ConfigManager.PATCHES.FIX_DUNGEON_BLOCK_PLACE, newValue -> {
            ConfigManager.PATCHES.FIX_DUNGEON_BLOCK_PLACE = newValue;
            ConfigManager.PATCHES.markAsChanged();
        }));
        add(list, "Patches", new ModuleItemView(client, "No command execution confirmation", "Skip Mojang's command execution confirmation dialog.", ConfigManager.PATCHES.NO_COMMAND_EXECUTION_CONFIRMATION, newValue -> {
            ConfigManager.PATCHES.NO_COMMAND_EXECUTION_CONFIRMATION = newValue;
            ConfigManager.PATCHES.markAsChanged();
        }));
        add(list, "Patches", new ModuleItemView(client, "Overrule Skyblocker glowing depth test", "Adjust Skyblocker glow rendering depth behavior.", ConfigManager.PATCHES.OVERRULE_SKYBLOCKER_GLOW_DEPTH_TEST, newValue -> {
            ConfigManager.PATCHES.OVERRULE_SKYBLOCKER_GLOW_DEPTH_TEST = newValue;
            ConfigManager.PATCHES.markAsChanged();
        }));
        return list;
    }

    private void collectCategories() {
        LinkedHashSet<String> uniqueCategories = new LinkedHashSet<>();
        for (FeatureEntry entry : entries) {
            uniqueCategories.add(entry.category);
        }
        categories.clear();
        categories.addAll(uniqueCategories);
        if (!categories.contains(selectedCategory) && !categories.isEmpty()) {
            selectedCategory = categories.getFirst();
        }
    }

    private void add(List<FeatureEntry> list, String category, ModuleItemView view) {
        list.add(new FeatureEntry(category, view.title, view.subtitle, view, false, null));
    }

    private void addOption(List<FeatureEntry> list, String category, String parent, String title, MeasurableElement view) {
        list.add(new FeatureEntry(category, title, null, view, true, parent));
    }

    private void addOption(List<FeatureEntry> list, String category, String parent, ModuleItemView view) {
        list.add(new FeatureEntry(category, view.title, view.subtitle, view.compact(), true, parent));
    }

    private void addWardrobeKeybind(List<FeatureEntry> list, String title, java.util.function.IntSupplier getter, java.util.function.IntConsumer setter) {
        addOption(list, "Render", "Wardrobe Keybinds", title, new KeybindItemView(client, title, getter, newValue -> {
            setter.accept(newValue);
            ConfigManager.FEATURES.markAsChanged();
        }));
    }

    private record FeatureEntry(String category, String title, String subtitle, MeasurableElement view, boolean option, String parent) {
        boolean matches(String needle) {
            return category.toLowerCase(Locale.ROOT).contains(needle)
                    || title.toLowerCase(Locale.ROOT).contains(needle)
                    || (subtitle != null && subtitle.toLowerCase(Locale.ROOT).contains(needle));
        }
    }
}
