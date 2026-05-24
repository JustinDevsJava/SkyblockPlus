package com.justindevsjava.skyblock_plus.client.feature.experimentation;

import com.justindevsjava.skyblock_plus.client.SkyblockPlusClient;
import com.justindevsjava.skyblock_plus.client.config.ConfigManager;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;

public abstract class AbstractExperimentSolver implements ScreenEvents.AfterTick, ScreenEvents.Remove {
    public static AbstractExperimentSolver ACTIVE_SOLVER = null;

    public static void init() {
        ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof ContainerScreen) {
                if (!ConfigManager.FEATURES.ENABLE_AUTO_EXPERIMENTATION) {
                    ACTIVE_SOLVER = null;
                    return;
                }
                String title = screen.getTitle().getString();
                if (title.startsWith(ChronomatronSolver.TITLE_PREFIX)) {
                    SkyblockPlusClient.LOGGER.info("Chronomatron started.");
                    ACTIVE_SOLVER = ChronomatronSolver.INSTANCE;
                } else if (title.startsWith(UltraSequencerSolver.TITLE_PREFIX)) {
                    SkyblockPlusClient.LOGGER.info("Ultrasequencer started.");
                    ACTIVE_SOLVER = UltraSequencerSolver.INSTANCE;
                } else if (ACTIVE_SOLVER != null) {
                    SkyblockPlusClient.LOGGER.info("Fixed experiment solver status.");
                    ACTIVE_SOLVER.onRemove(null);
                    ACTIVE_SOLVER = null;
                }
                if (ACTIVE_SOLVER != null) {
                    ScreenEvents.afterTick(screen).register(ACTIVE_SOLVER);
                    ScreenEvents.remove(screen).register(ACTIVE_SOLVER);
                }
            }
        });
    }

    public long startSolvingTimestamp = 0;

    protected ExperimentState state = ExperimentState.INITIALIZE;

    protected enum ExperimentState {
        INITIALIZE,
        REMEMBER,
        SOLVE,
    }

    public abstract boolean willRedirectClick();

    public abstract int redirectedSlot();
}
