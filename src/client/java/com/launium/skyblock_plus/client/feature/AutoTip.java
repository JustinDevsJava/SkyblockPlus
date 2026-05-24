package com.launium.skyblock_plus.client.feature;

import com.launium.skyblock_plus.client.SkyblockPlusClient;
import com.launium.skyblock_plus.client.config.ConfigManager;
import com.launium.skyblock_plus.client.util.ClientTaskScheduler;
import com.launium.skyblock_plus.client.util.Remember;
import com.launium.skyblock_plus.client.util.SimpleDuration;
import com.launium.skyblock_plus.client.util.SkyblockLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AutoTip extends AbstractModule {
    public static final AutoTip INSTANCE = new AutoTip();

    private long nextTipTimestamp = 0L;

    private final Remember<SimpleDuration> rememberLeftTime = new Remember<>();
    private @NotNull String leftTime = "";

    static {
        if (ConfigManager.FEATURES.ENABLE_AUTO_TIP) {
            SkyblockPlusClient.moduleList.showModule(INSTANCE);
        }
    }

    public void whenServerBrandUpdate(String brand) {
        setupTask();
    }

    public void reset() {
        nextTipTimestamp = 0L;
        setupTask();
    }

    public void setupTask() {
        if (SkyblockLocation.isInHypixel()) {
            long now = Util.getMillis();
            if (now > nextTipTimestamp) {
                ClientTaskScheduler.CLIENT_TASKS.add(new ClientTaskScheduler.AbstractTask(nextTipTimestamp) {
                    @Override
                    public void execute(Minecraft client) {
                        long now = Util.getMillis();
                        if (AutoTip.INSTANCE.nextTipTimestamp > now) return;

                        ClientPacketListener clientPacketListener = client.getConnection();
                        if (clientPacketListener == null || !SkyblockLocation.isInHypixel() || !ConfigManager.FEATURES.ENABLE_AUTO_TIP)
                            return;
                        clientPacketListener.sendCommand("tipall");

                        AutoTip.INSTANCE.nextTipTimestamp = now + 10 * 60 * 1000L;
                        this.scheduledTimeMs = AutoTip.INSTANCE.nextTipTimestamp;
                        ClientTaskScheduler.CLIENT_TASKS.add(this);
                    }
                });
            }
        }
    }

    @Override
    public String title() {
        return "AutoTip";
    }

    @Override
    public @Nullable String subtitle() {
        if (!SkyblockLocation.isInHypixel()) return "OFF";
        SimpleDuration left = new SimpleDuration(nextTipTimestamp - Util.getMillis())
                .truncatedToSeconds();
        if (!rememberLeftTime.updateObject(left)) {
            String newLeftTime = left.toString();
            if (newLeftTime.length() != leftTime.length()) moduleList.needResort = true;
            leftTime = newLeftTime;
        }
        return leftTime;
    }

    @Override
    public boolean isActive() {
        return ConfigManager.FEATURES.ENABLE_AUTO_TIP;
    }
}
