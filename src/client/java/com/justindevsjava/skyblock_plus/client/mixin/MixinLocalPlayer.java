package com.justindevsjava.skyblock_plus.client.mixin;

import com.justindevsjava.skyblock_plus.client.config.ConfigManager;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LocalPlayer.class)
public abstract class MixinLocalPlayer {
    @ModifyExpressionValue(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Input;sprint()Z"))
    private boolean skyblock_plus$autoSprint(boolean original) {
        return original || ConfigManager.FEATURES.ENABLE_AUTO_SPRINT;
    }
}
