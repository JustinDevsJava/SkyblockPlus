package com.launium.skyblock_plus.client.feature;

import com.launium.skyblock_plus.client.config.ConfigManager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;

public class PlayerSize {
    public static void scaleLocalPlayer(AvatarRenderState state, PoseStack poseStack) {
        if (!ConfigManager.FEATURES.ENABLE_PLAYER_SIZE) return;
        if (Minecraft.getInstance().player == null || state.id != Minecraft.getInstance().player.getId()) return;
        poseStack.scale(ConfigManager.FEATURES.PLAYER_SIZE_X,
                ConfigManager.FEATURES.PLAYER_SIZE_Y,
                ConfigManager.FEATURES.PLAYER_SIZE_Z);
    }
}
