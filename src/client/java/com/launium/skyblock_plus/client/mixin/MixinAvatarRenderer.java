package com.launium.skyblock_plus.client.mixin;

import com.launium.skyblock_plus.client.feature.PlayerSize;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
public class MixinAvatarRenderer {
    @Inject(method = "scale(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;)V", at = @At("HEAD"))
    private void skyblock_plus$scaleLocalPlayer(AvatarRenderState state, PoseStack poseStack, CallbackInfo ci) {
        PlayerSize.scaleLocalPlayer(state, poseStack);
    }
}
