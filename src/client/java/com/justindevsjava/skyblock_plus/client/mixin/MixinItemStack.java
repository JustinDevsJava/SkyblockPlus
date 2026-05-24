package com.justindevsjava.skyblock_plus.client.mixin;

import com.justindevsjava.skyblock_plus.client.interfaces.AccessItemStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.ItemLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ItemStack.class)
public abstract class MixinItemStack implements AccessItemStack {
    @Shadow
    public abstract Item getItem();

    @Unique
    private boolean skyblock_plus$isShortbow = false;

    @Unique
    private boolean skyblock_plus$hasPickobulusAbility = false;

    @Override
    public boolean skyblock_plus$isShortbow() {
        return skyblock_plus$isShortbow;
    }

    @Override
    public boolean skyblock_plus$hasPickobulusAbility() {
        return skyblock_plus$hasPickobulusAbility;
    }

    @Inject(method = "<init>(Lnet/minecraft/world/level/ItemLike;ILnet/minecraft/core/component/PatchedDataComponentMap;)V", at = @At("TAIL"))
    private void skyblock_plus$readItemComponents(ItemLike item, int count, PatchedDataComponentMap components, CallbackInfo ci) {
        List<Component> lines = components.getOrDefault(DataComponents.LORE, ItemLore.EMPTY).styledLines();
        if (this.getItem() == Items.BOW) {
            this.skyblock_plus$isShortbow = lines.stream().anyMatch(line -> line.getString().contains("Shortbow: Instantly shoots!"));
        } else {
            this.skyblock_plus$hasPickobulusAbility = lines.stream().anyMatch(line -> line.getString().startsWith("Ability: Pickobulus"));
        }
    }
}
