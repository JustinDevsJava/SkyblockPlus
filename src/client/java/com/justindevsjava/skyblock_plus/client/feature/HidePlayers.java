package com.justindevsjava.skyblock_plus.client.feature;

import com.justindevsjava.skyblock_plus.client.config.ConfigManager;
import com.justindevsjava.skyblock_plus.client.util.SkyblockLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class HidePlayers {
    public static boolean shouldRender(Entity entity) {
        Minecraft client = Minecraft.getInstance();
        if (!ConfigManager.FEATURES.ENABLE_HIDE_PLAYERS) return true;
        if (!(entity instanceof Player player)) return true;
        if (client.player == null || player == client.player) return true;
        if (player.getUUID().version() != 4) return true;
        if (ConfigManager.FEATURES.HIDE_PLAYERS_ONLY_DUNGEONS && !SkyblockLocation.isInDungeons()) return true;
        if (ConfigManager.FEATURES.HIDE_PLAYERS_ALL) return false;
        float distance = ConfigManager.FEATURES.HIDE_PLAYERS_DISTANCE;
        return player.distanceToSqr(client.player) > distance * distance;
    }
}
