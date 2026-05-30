package com.justindevsjava.skyblock_plus.client.feature;

import com.google.gson.annotations.SerializedName;
import com.justindevsjava.skyblock_plus.client.SkyblockPlusClient;
import com.justindevsjava.skyblock_plus.client.config.ConfigManager;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.JarFile;

public final class AutoUpdater {
    private static final long CHECK_INTERVAL_MS = 15L * 60L * 1000L;
    private static final AtomicBoolean CHECKING = new AtomicBoolean();
    private static final AtomicBoolean DOWNLOADING = new AtomicBoolean();
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    private static final String PENDING_UPDATE_SUFFIX = ".skyblock-plus-update";
    private static final String UPDATE_REPO = "JustinDevsJava/SkyblockPlus";
    private static final String EXPECTED_MOD_ID = "skyblock_plus";
    private static final String EXPECTED_ASSET_PREFIX = "skyblock_plus-";

    private static volatile long lastCheckStartedMs;
    private static volatile String lastPromptedTag = "";
    private static volatile String statusLine = "Ready. Use /skyblock_plus update status.";
    private static volatile AvailableUpdate availableUpdate;
    private static volatile boolean installApprovedThisSession;

    private AutoUpdater() {
    }

    public static void init() {
        if (ConfigManager.GENERAL.AUTO_UPDATE_ENABLED) {
            checkForUpdatesAsync(false);
        } else {
            statusLine = "Auto updater is disabled.";
        }
    }

    public static void onClientTick(Minecraft client) {
        if (!ConfigManager.GENERAL.AUTO_UPDATE_ENABLED || CHECKING.get()) {
            return;
        }
        long now = Util.getMillis();
        if (lastCheckStartedMs == 0L || now - lastCheckStartedMs >= CHECK_INTERVAL_MS) {
            checkForUpdatesAsync(false);
        }
    }

    public static void onClientStopping() {
        if (!installApprovedThisSession) {
            return;
        }
        trySchedulePendingInstall();
    }

    public static String getStatusLine() {
        return statusLine;
    }

    public static String getMenuActionTitle() {
        if (DOWNLOADING.get()) {
            return "Downloading update";
        }
        if (CHECKING.get()) {
            return "Checking for updates";
        }
        if (availableUpdate != null && installApprovedThisSession) {
            return "Update approved";
        }
        if (availableUpdate != null) {
            return "Update available";
        }
        return "Check for updates";
    }

    public static String getMenuActionSubtitle() {
        if (DOWNLOADING.get()) {
            return "Downloading the update file you approved.";
        }
        if (CHECKING.get()) {
            return "Reading GitHub release metadata.";
        }
        AvailableUpdate update = availableUpdate;
        if (update != null && installApprovedThisSession) {
            return "Close the game to install " + update.displayTag() + ".";
        }
        if (update != null) {
            return "Press to download " + update.displayTag() + " from GitHub (" + formatBytes(update.size) + ").";
        }
        return "Press to check. Downloads need another press.";
    }

    public static void runMenuUpdateAction() {
        if (availableUpdate == null) {
            checkForUpdatesAsync(true);
        } else {
            downloadAvailableUpdateAsync();
        }
    }

    public static void setEnabledState(boolean enabled) {
        if (enabled) {
            statusLine = "Auto updater enabled. Updates will ask before downloading.";
            return;
        }

        availableUpdate = null;
        installApprovedThisSession = false;
        statusLine = "Auto updater disabled.";
        clearPendingUpdate();
    }

    public static void clearPendingUpdate() {
        installApprovedThisSession = false;
        resolveCurrentJarPath()
                .map(AutoUpdater::pendingPathFor)
                .ifPresent(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException e) {
                        SkyblockPlusClient.LOGGER.warn("[Skyblock+ AutoUpdater] Failed to clear pending update {}", path, e);
                    }
                });
    }

    public static void checkForUpdatesAsync(boolean manual) {
        if (!ConfigManager.GENERAL.AUTO_UPDATE_ENABLED && !manual) {
            statusLine = "Auto updater is disabled.";
            return;
        }
        if (!CHECKING.compareAndSet(false, true)) {
            statusLine = "Already checking GitHub for updates...";
            return;
        }
        lastCheckStartedMs = Util.getMillis();
        statusLine = "Checking GitHub releases...";

        Thread.startVirtualThread(() -> {
            try {
                performCheck(manual);
            } catch (Exception e) {
                statusLine = "Update check failed. See log for details.";
                SkyblockPlusClient.LOGGER.error("[Skyblock+ AutoUpdater] Failed to check for updates", e);
            } finally {
                CHECKING.set(false);
            }
        });
    }

    public static void downloadAvailableUpdateAsync() {
        AvailableUpdate update = availableUpdate;
        if (update == null) {
            statusLine = "No update is ready. Run /skyblock_plus update check first.";
            sendClientMessage(statusLine);
            return;
        }
        if (!DOWNLOADING.compareAndSet(false, true)) {
            statusLine = "Already downloading update " + update.displayTag() + '.';
            return;
        }

        statusLine = "Downloading update " + update.displayTag() + " after user approval...";
        Thread.startVirtualThread(() -> {
            try {
                performDownload(update);
            } catch (Exception e) {
                statusLine = "Update download failed. See log for details.";
                SkyblockPlusClient.LOGGER.error("[Skyblock+ AutoUpdater] Failed to download update", e);
                sendClientMessage(statusLine);
            } finally {
                DOWNLOADING.set(false);
            }
        });
    }

    private static void performCheck(boolean manual) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.github.com/repos/" + UPDATE_REPO + "/releases/latest"))
                .header("Accept", "application/vnd.github+json")
                .header("X-GitHub-Api-Version", "2022-11-28")
                .header("User-Agent", "Skyblock+ AutoUpdater")
                .timeout(Duration.ofSeconds(20))
                .GET()
                .build();

        HttpResponse<String> releaseResponse = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
        if (releaseResponse.statusCode() != 200) {
            statusLine = "GitHub responded with HTTP " + releaseResponse.statusCode() + '.';
            return;
        }

        GithubRelease release = SkyblockPlusClient.GSON.fromJson(releaseResponse.body(), GithubRelease.class);
        if (release == null || release.assets == null || release.assets.length == 0) {
            availableUpdate = null;
            statusLine = "No downloadable release jar found yet.";
            return;
        }

        Optional<GithubAsset> chosenAsset = Arrays.stream(release.assets)
                .filter(asset -> asset != null && asset.browserDownloadUrl != null && asset.name != null)
                .filter(asset -> asset.name.endsWith(".jar"))
                .filter(asset -> !asset.name.contains("-sources"))
                .filter(asset -> asset.name.startsWith(EXPECTED_ASSET_PREFIX))
                .findFirst();
        if (chosenAsset.isEmpty()) {
            availableUpdate = null;
            statusLine = "Release exists, but there is no Skyblock+ jar asset to download.";
            return;
        }

        String currentVersion = FabricLoader.getInstance().getModContainer(EXPECTED_MOD_ID)
                .map(container -> container.getMetadata().getVersion().getFriendlyString())
                .orElse("unknown");
        String latestVersion = normalizeVersion(release.tagName);
        String normalizedCurrentVersion = normalizeVersion(currentVersion);
        GithubAsset asset = chosenAsset.get();

        if (latestVersion.equals(normalizedCurrentVersion)) {
            availableUpdate = null;
            resolveCurrentJarPath().map(AutoUpdater::pendingPathFor).ifPresent(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    SkyblockPlusClient.LOGGER.debug("[Skyblock+ AutoUpdater] Failed to clear stale pending update {}", path, e);
                }
            });
            statusLine = "You're up to date on " + currentVersion + ".";
            return;
        }

        AvailableUpdate update = new AvailableUpdate(release.tagName, latestVersion, currentVersion,
                asset.name, asset.size, asset.browserDownloadUrl);
        availableUpdate = update;

        Optional<Path> currentJar = resolveCurrentJarPath();
        Path destination = destinationFor(update, currentJar, false);
        if (Files.exists(destination) && (update.size <= 0 || Files.size(destination) == update.size)) {
            statusLine = currentJar.isPresent()
                    ? "Update " + update.displayTag() + " is already staged. Press update or run /skyblock_plus update download to approve installing it on close."
                    : "Update " + update.displayTag() + " is already downloaded. Press update or run /skyblock_plus update download to approve it.";
            notifyUpdateAvailable(update, manual);
            return;
        }

        statusLine = "Update " + update.displayTag() + " available from GitHub: " + update.assetName
                + " (" + formatBytes(update.size) + "). Run /skyblock_plus update download to download it.";
        notifyUpdateAvailable(update, manual);
    }

    private static void performDownload(AvailableUpdate update) throws IOException, InterruptedException {
        Optional<Path> currentJar = resolveCurrentJarPath();
        Path destination = destinationFor(update, currentJar, true);

        if (Files.exists(destination) && (update.size <= 0 || Files.size(destination) == update.size)) {
            installApprovedThisSession = true;
            statusLine = currentJar.isPresent()
                    ? "Update " + update.displayTag() + " is approved. Close the game to install it."
                    : "Update " + update.displayTag() + " is already downloaded to config/Skyblock+/updates.";
            sendClientMessage(statusLine);
            return;
        }

        if (currentJar.isPresent()) {
            cleanupSiblingPending(currentJar.get(), destination);
        } else {
            cleanupFallbackDownloads(destination.getParent(), destination);
        }

        HttpRequest downloadRequest = HttpRequest.newBuilder(URI.create(update.browserDownloadUrl))
                .header("User-Agent", "Skyblock+ AutoUpdater")
                .timeout(Duration.ofSeconds(60))
                .GET()
                .build();
        HttpResponse<InputStream> downloadResponse = HTTP.send(downloadRequest, HttpResponse.BodyHandlers.ofInputStream());
        if (downloadResponse.statusCode() != 200) {
            statusLine = "Download failed with HTTP " + downloadResponse.statusCode() + '.';
            sendClientMessage(statusLine);
            return;
        }

        Path tempFile = destination.resolveSibling(destination.getFileName() + ".part");
        try (InputStream inputStream = downloadResponse.body()) {
            Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
        }
        if (!verifyDownloadedJar(tempFile)) {
            Files.deleteIfExists(tempFile);
            statusLine = "Downloaded file failed verification.";
            sendClientMessage(statusLine);
            return;
        }
        try {
            Files.move(tempFile, destination, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException ignored) {
            Files.move(tempFile, destination, StandardCopyOption.REPLACE_EXISTING);
        }

        installApprovedThisSession = true;
        statusLine = currentJar.isPresent()
                ? "Downloaded " + update.displayTag() + ". Close the game to install it."
                : "Downloaded " + update.displayTag() + " to config/Skyblock+/updates.";
        sendClientMessage(statusLine);
        SkyblockPlusClient.LOGGER.info("[Skyblock+ AutoUpdater] Downloaded {} to {}", update.assetName, destination.toAbsolutePath());
    }

    private static Optional<Path> resolveCurrentJarPath() {
        try {
            Path path = Path.of(SkyblockPlusClient.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toAbsolutePath();
            if (Files.isRegularFile(path) && path.getFileName().toString().endsWith(".jar")) {
                return Optional.of(path);
            }
        } catch (Exception e) {
            SkyblockPlusClient.LOGGER.debug("[Skyblock+ AutoUpdater] Unable to resolve current jar path", e);
        }
        return Optional.empty();
    }

    private static Path destinationFor(AvailableUpdate update, Optional<Path> currentJar, boolean createDirectories) throws IOException {
        if (currentJar.isPresent()) {
            return pendingPathFor(currentJar.get());
        }

        Path updateDir = Path.of("config", "Skyblock+", "updates");
        if (createDirectories) {
            Files.createDirectories(updateDir);
        }
        return updateDir.resolve(update.assetName);
    }

    private static Path pendingPathFor(Path currentJar) {
        return currentJar.resolveSibling(currentJar.getFileName().toString() + PENDING_UPDATE_SUFFIX);
    }

    private static void trySchedulePendingInstall() {
        Optional<Path> currentJar = resolveCurrentJarPath();
        if (currentJar.isEmpty()) {
            return;
        }
        Path stagedFile = pendingPathFor(currentJar.get());
        if (!Files.exists(stagedFile)) {
            return;
        }
        try {
            launchInstaller(currentJar.get(), stagedFile);
            statusLine = "Update is queued and will be installed as the game closes.";
        } catch (IOException e) {
            statusLine = "Update downloaded, but failed to schedule install.";
            SkyblockPlusClient.LOGGER.error("[Skyblock+ AutoUpdater] Failed to launch installer", e);
        }
    }

    private static void launchInstaller(Path currentJar, Path stagedFile) throws IOException {
        String javaExe = Path.of(
                System.getProperty("java.home"),
                "bin",
                System.getProperty("os.name", "").toLowerCase().contains("win") ? "javaw.exe" : "java"
        ).toString();

        new ProcessBuilder(
                javaExe,
                "-cp",
                currentJar.toAbsolutePath().toString(),
                "com.justindevsjava.skyblock_plus.client.feature.UpdateInstaller",
                Long.toString(ProcessHandle.current().pid()),
                stagedFile.toAbsolutePath().toString(),
                currentJar.toAbsolutePath().toString()
        )
                .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                .redirectError(ProcessBuilder.Redirect.DISCARD)
                .start();
    }

    private static boolean verifyDownloadedJar(Path jarPath) {
        try (JarFile jarFile = new JarFile(jarPath.toFile())) {
            var entry = jarFile.getEntry("fabric.mod.json");
            if (entry == null) {
                return false;
            }

            try (InputStream in = jarFile.getInputStream(entry)) {
                String json = new String(in.readAllBytes());
                return json.contains("\"id\": \"" + EXPECTED_MOD_ID + "\"")
                        || json.contains("\"id\":\"" + EXPECTED_MOD_ID + "\"");
            }
        } catch (Exception e) {
            SkyblockPlusClient.LOGGER.warn("[Skyblock+ AutoUpdater] Downloaded jar failed verification", e);
            return false;
        }
    }

    private static void cleanupSiblingPending(Path currentJar, Path keepFile) {
        Path siblingDir = currentJar.getParent();
        String currentJarName = currentJar.getFileName().toString();
        try (var files = Files.list(siblingDir)) {
            files.filter(path -> !path.equals(keepFile))
                    .filter(path -> path.getFileName().toString().startsWith(currentJarName) && path.getFileName().toString().contains(PENDING_UPDATE_SUFFIX))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            SkyblockPlusClient.LOGGER.warn("[Skyblock+ AutoUpdater] Failed to delete old pending update {}", path, e);
                        }
                    });
        } catch (IOException e) {
            SkyblockPlusClient.LOGGER.warn("[Skyblock+ AutoUpdater] Failed to clean pending updates near {}", currentJar, e);
        }
    }

    private static void cleanupFallbackDownloads(Path updateDir, Path keepFile) {
        try (var files = Files.list(updateDir)) {
            files.filter(path -> !path.equals(keepFile))
                    .sorted(Comparator.comparing(Path::toString))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            SkyblockPlusClient.LOGGER.warn("[Skyblock+ AutoUpdater] Failed to delete old update file {}", path, e);
                        }
                    });
        } catch (IOException e) {
            SkyblockPlusClient.LOGGER.warn("[Skyblock+ AutoUpdater] Failed to clean update directory {}", updateDir, e);
        }
    }

    private static void notifyUpdateAvailable(AvailableUpdate update, boolean manual) {
        String promptTag = update.displayTag();
        if (!manual && promptTag.equals(lastPromptedTag)) {
            return;
        }
        lastPromptedTag = promptTag;
        sendClientMessage("Update " + promptTag + " is available from GitHub for " + update.currentVersion
                + ": " + update.assetName + " (" + formatBytes(update.size)
                + "). Run /skyblock_plus update download to download and stage it.");
    }

    private static void sendClientMessage(String message) {
        Minecraft client = Minecraft.getInstance();
        client.execute(() -> {
            if (client.player != null) {
                client.player.displayClientMessage(Component.literal("[Skyblock+] " + message), false);
            }
        });
    }

    private static String normalizeVersion(String version) {
        if (version == null) {
            return "";
        }
        String normalized = version.trim();
        if (normalized.startsWith("v") || normalized.startsWith("V")) {
            normalized = normalized.substring(1);
        }
        return normalized;
    }

    private static String formatBytes(long bytes) {
        if (bytes <= 0) {
            return "unknown size";
        }
        if (bytes < 1024L) {
            return bytes + " B";
        }
        if (bytes < 1024L * 1024L) {
            return String.format(Locale.ROOT, "%.1f KB", bytes / 1024.0);
        }
        return String.format(Locale.ROOT, "%.1f MB", bytes / (1024.0 * 1024.0));
    }

    private static final class AvailableUpdate {
        private final String tagName;
        private final String latestVersion;
        private final String currentVersion;
        private final String assetName;
        private final long size;
        private final String browserDownloadUrl;

        private AvailableUpdate(String tagName, String latestVersion, String currentVersion, String assetName,
                                long size, String browserDownloadUrl) {
            this.tagName = tagName;
            this.latestVersion = latestVersion;
            this.currentVersion = currentVersion;
            this.assetName = assetName;
            this.size = size;
            this.browserDownloadUrl = browserDownloadUrl;
        }

        private String displayTag() {
            if (tagName != null && !tagName.isBlank()) {
                return tagName;
            }
            if (latestVersion != null && !latestVersion.isBlank()) {
                return latestVersion;
            }
            return "latest";
        }
    }

    private static final class GithubRelease {
        @SerializedName("tag_name")
        String tagName;
        GithubAsset[] assets;
    }

    private static final class GithubAsset {
        String name;
        long size;
        @SerializedName("browser_download_url")
        String browserDownloadUrl;
    }
}
