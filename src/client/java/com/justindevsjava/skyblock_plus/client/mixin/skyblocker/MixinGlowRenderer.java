package com.justindevsjava.skyblock_plus.client.mixin.skyblocker;

import com.justindevsjava.skyblock_plus.client.config.ConfigManager;
import de.hysky.skyblocker.utils.render.GlowRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GlowRenderer.class)
@Pseudo
public class MixinGlowRenderer {
    @Inject(method = "startRenderingGlow", at = @At("HEAD"), cancellable = true)
    private void skyblock_plus$preventSkyblockerGlowDepthTest(CallbackInfo ci) {
        if (ConfigManager.PATCHES.OVERRULE_SKYBLOCKER_GLOW_DEPTH_TEST) {
            ci.cancel();
        }
    }
}
