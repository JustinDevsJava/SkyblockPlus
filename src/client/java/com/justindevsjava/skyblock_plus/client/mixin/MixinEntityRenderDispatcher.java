package com.justindevsjava.skyblock_plus.client.mixin;

import com.justindevsjava.skyblock_plus.client.feature.HidePlayers;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public class MixinEntityRenderDispatcher {
    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void skyblock_plus$hidePlayers(Entity entity, Frustum frustum, double camX, double camY, double camZ,
                                           CallbackInfoReturnable<Boolean> cir) {
        if (!HidePlayers.shouldRender(entity)) {
            cir.setReturnValue(false);
        }
    }
}
