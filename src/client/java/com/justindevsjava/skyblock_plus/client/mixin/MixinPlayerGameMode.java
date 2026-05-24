package com.justindevsjava.skyblock_plus.client.mixin;

import com.justindevsjava.skyblock_plus.client.feature.AutoClicker;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public class MixinPlayerGameMode {
    @Inject(method = "hasMissTime", at = @At("HEAD"), cancellable = true)
    public void skyblock_plus$clickFix(CallbackInfoReturnable<Boolean> cir) {
        if (AutoClicker.INSTANCE.isEnabled) {
            cir.setReturnValue(false);
        }
    }
}
