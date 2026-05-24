package com.launium.skyblock_plus.client.config;

import com.launium.skyblock_plus.client.SkyblockPlusClient;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class AbstractConfig {
    protected abstract String getConfigName();

    private transient boolean isChanged;

    public void save() throws IOException {
        try (BufferedWriter fileOut = Files.newBufferedWriter(Path.of("config/Skyblock+/" + getConfigName()))) {
            SkyblockPlusClient.GSON.toJson(this, fileOut);
        }
    }

    public void markAsChanged() {
        isChanged = true;
        processChanges();
    }

    void processChanges() {
        if (isChanged) {
            isChanged = false;
            try {
                save();
            } catch (IOException e) {
                SkyblockPlusClient.LOGGER.error("[Skyblock+ Config Manager] Failed to save {}", getConfigName(), e);
            }
        }
    }
}
