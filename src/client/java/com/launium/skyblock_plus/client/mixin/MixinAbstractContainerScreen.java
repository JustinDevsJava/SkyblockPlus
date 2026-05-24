package com.launium.skyblock_plus.client.mixin;

import com.launium.skyblock_plus.client.config.ConfigManager;
import com.launium.skyblock_plus.client.feature.WardrobeKeybinds;
import com.launium.skyblock_plus.client.feature.experimentation.AbstractExperimentSolver;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.util.Util;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("unchecked")
@Mixin(AbstractContainerScreen.class)
public class MixinAbstractContainerScreen {
    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void skyblock_plus$wardrobeKeybinds(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (WardrobeKeybinds.onKeyPressed((AbstractContainerScreen<?>) (Object) this, event)) {
            cir.setReturnValue(true);
        }
    }

    @WrapMethod(method = "slotClicked")
    private void skyblock_plus$hijackSlotClick(Slot slot, int slotId, int mouseButton, ClickType type, Operation<Void> original) {
        if (ConfigManager.FEATURES.ENABLE_AUTO_EXPERIMENTATION && slot != null && slot.index == 49 && AbstractExperimentSolver.ACTIVE_SOLVER != null) {
            if (Util.getMillis() - AbstractExperimentSolver.ACTIVE_SOLVER.startSolvingTimestamp < 300L) return;
            if (AbstractExperimentSolver.ACTIVE_SOLVER.willRedirectClick()) {
                slotId = AbstractExperimentSolver.ACTIVE_SOLVER.redirectedSlot();
                slot = ((AbstractContainerScreen<ChestMenu>) (Object) this).getMenu().getSlot(slotId);
                // use middle-click
                mouseButton = GLFW.GLFW_MOUSE_BUTTON_3;
                type = ClickType.CLONE;
            } else {
                return;
            }
        }
        original.call(slot, slotId, mouseButton, type);
    }
}
