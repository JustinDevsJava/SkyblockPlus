package com.justindevsjava.skyblock_plus.client.feature;

import com.justindevsjava.skyblock_plus.client.config.ConfigManager;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class EtherwarpPreview implements WorldRenderEvents.EndExtraction, WorldRenderEvents.AfterEntities {
    public static final EtherwarpPreview INSTANCE = new EtherwarpPreview();

    private EtherTarget target = EtherTarget.NONE;

    @Override
    public void endExtraction(WorldExtractionContext context) {
        target = EtherTarget.NONE;
        if (!ConfigManager.FEATURES.ENABLE_ETHERWARP_PREVIEW) return;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || Minecraft.getInstance().screen != null) return;

        CompoundTag etherData = getEtherwarpData(player.getMainHandItem());
        if (etherData == null) return;
        String itemId = etherData.getString("id").orElse("");
        if (!player.isShiftKeyDown() && !"ETHERWARP_CONDUIT".equals(itemId)) return;

        int tunedTransmission = etherData.getInt("tuned_transmission").orElse(0);
        target = findTarget(player, 57.0 + tunedTransmission);
    }

    @Override
    public void afterEntities(WorldRenderContext context) {
        if (!ConfigManager.FEATURES.ENABLE_ETHERWARP_PREVIEW || target.pos == null) return;
        if (!target.succeeded && !ConfigManager.FEATURES.ETHERWARP_SHOW_FAILED) return;
        int color = target.succeeded ? 0xD9D8C7FF : 0xD9FF5E76;
        AABB bounds = new AABB(target.pos);
        Gizmos.cuboid(bounds, new GizmoStyle(color, 2.5F, ARGB.multiplyAlpha(color, 0.18F)));
    }

    private EtherTarget findTarget(LocalPlayer player, double distance) {
        Vec3 start = player.position().add(0.0, player.isCrouching() ? 1.54 : 1.62, 0.0);
        Vec3 end = start.add(player.getLookAngle().scale(distance));
        BlockHitResult hit = player.level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (hit.getType() != HitResult.Type.BLOCK) return EtherTarget.NONE;

        BlockPos pos = hit.getBlockPos();
        BlockState state = player.level().getBlockState(pos);
        double collisionTop = state.getCollisionShape(player.level(), pos).max(Direction.Axis.Y);
        int clearanceY = pos.getY() + Math.max(1, (int) Math.ceil(collisionTop));

        boolean clearFeet = player.level().getBlockState(new BlockPos(pos.getX(), clearanceY, pos.getZ())).getCollisionShape(player.level(), new BlockPos(pos.getX(), clearanceY, pos.getZ())).isEmpty();
        boolean clearHead = player.level().getBlockState(new BlockPos(pos.getX(), clearanceY + 1, pos.getZ())).getCollisionShape(player.level(), new BlockPos(pos.getX(), clearanceY + 1, pos.getZ())).isEmpty();
        return new EtherTarget(clearFeet && clearHead, pos);
    }

    private CompoundTag getEtherwarpData(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        if (tag.getInt("ethermerge").orElse(0) == 1 || "ETHERWARP_CONDUIT".equals(tag.getString("id").orElse(""))) {
            return tag;
        }
        return null;
    }

    private record EtherTarget(boolean succeeded, BlockPos pos) {
        static final EtherTarget NONE = new EtherTarget(false, null);
    }
}
