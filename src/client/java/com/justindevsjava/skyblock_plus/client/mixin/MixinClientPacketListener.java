package com.justindevsjava.skyblock_plus.client.mixin;

import com.justindevsjava.skyblock_plus.client.config.ConfigManager;
import com.justindevsjava.skyblock_plus.client.feature.DayViewer;
import com.justindevsjava.skyblock_plus.client.feature.ForagingStyleWarning;
import com.justindevsjava.skyblock_plus.client.feature.PickobulusPreview;
import com.justindevsjava.skyblock_plus.client.ui.container.ServerTPSContainer;
import com.justindevsjava.skyblock_plus.client.util.SkyblockLocation;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.*;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class MixinClientPacketListener {
    @Shadow
    public abstract void sendCommand(String command);

    @Inject(method = "handleSetTime", at = @At("TAIL"))
    private void skyblock_plus$handleSetTime(ClientboundSetTimePacket packet, CallbackInfo ci) {
        ServerTPSContainer.INSTANCE.whenClientboundSetTime();
        DayViewer.INSTANCE.whenClientboundSetTime();
    }

    @Inject(method = "handleRespawn", at = @At("TAIL"))
    private void skyblock_plus$handleRespawn(ClientboundRespawnPacket packet, CallbackInfo ci) {
        ServerTPSContainer.INSTANCE.whenRespawn();
        PickobulusPreview.INSTANCE.resetCooldown();
        SkyblockLocation.whenRespawn();
    }

    @Inject(method = "handleSoundEvent", at = @At("TAIL"))
    private void skyblock_plus$handleSoundEvent(ClientboundSoundPacket packet, CallbackInfo ci) {
        SoundEvent soundEvent = packet.getSound().value();
        if (SoundEvents.LADDER_BREAK.equals(soundEvent)) {
            ForagingStyleWarning.whenWoodBreakSound(packet);
        }
    }

    @Inject(method = "handlePlayerInfoUpdate", at = @At("TAIL"))
    private void skyblock_plus$handleScoreboardUpdate(ClientboundPlayerInfoUpdatePacket packet, CallbackInfo ci) {
        SkyblockLocation.whenScoreboardUpdate(packet.entries());
    }

    @Inject(method = "openCommandSendConfirmationWindow", at = @At("HEAD"), cancellable = true)
    private void skyblock_plus$noCommandConfirmation(String command, String titleKey, Screen previousScreen, CallbackInfo ci) {
        if (ConfigManager.PATCHES.NO_COMMAND_EXECUTION_CONFIRMATION) {
            this.sendCommand(command);
            ci.cancel();
        }
    }
}
