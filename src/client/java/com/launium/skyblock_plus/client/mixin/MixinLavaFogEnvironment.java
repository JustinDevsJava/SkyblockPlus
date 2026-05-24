package com.launium.skyblock_plus.client.mixin;

import com.launium.skyblock_plus.client.config.ConfigManager;
import net.minecraft.client.renderer.fog.environment.LavaFogEnvironment;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LavaFogEnvironment.class)
public class MixinLavaFogEnvironment {
    @Redirect(method = "setupFog", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;isSpectator()Z"))
    private static boolean skyblock_plus$alwaysUsesSpectatorFluidFog(Entity instance) {
        return ConfigManager.PATCHES.USE_SPECTATOR_FOG || instance.isSpectator();
    }
}
