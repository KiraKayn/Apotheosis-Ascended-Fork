package net.kayn.fallen_gems_affixes.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemRegistry;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.kayn.fallen_gems_affixes.adventure.socket.gem.storage.GemCaseAnimationState;
import net.kayn.fallen_gems_affixes.adventure.socket.gem.storage.GemCaseBlock;
import net.kayn.fallen_gems_affixes.adventure.socket.gem.storage.GemCaseTile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class GemCaseTileRenderer implements BlockEntityRenderer<GemCaseTile> {

    private final Map<DynamicHolder<Gem>, ItemStack> gemCache = new HashMap<>();

    @Override
    public void render(GemCaseTile tile, float partials, PoseStack pose, MultiBufferSource bufferSrc, int light, int overlay) {

        ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
        double px = 1 / 16D;

        Direction facing = tile.getBlockState().getValue(GemCaseBlock.FACING);
        float angle = switch (facing) {
            case NORTH -> 0;
            case EAST -> 270;
            case SOUTH -> 180;
            case WEST -> 90;
            default -> 0;
        };

        GemCaseAnimationState animState = tile.getAnimationState();
        int i = 0;

        for (DynamicHolder<Gem> gem : tile.gems.keySet()) {
            int count = tile.gems.get(gem).values().stream().mapToInt(Integer::intValue).sum();
            if (count == 0) continue;

            ItemStack stack = gemCache.computeIfAbsent(gem, g -> GemRegistry.createGemStack(g.get(), g.get().getMaxRarity()));

            pose.pushPose();

            pose.translate(8 * px, 0, 8 * px);
            pose.mulPose(Axis.YP.rotationDegrees(angle));
            pose.translate(-8 * px, 0, -8 * px);
            pose.translate(0, 16 * px, 0);

            float scale = 1 / 6F;
            pose.scale(scale, scale, scale);

            GemCaseAnimationState.PositionInfo posInfo = animState.getPosition(i, partials);
            float gridX = (posInfo.baseSlot() % 4) + posInfo.offsetX();
            float gridZ = (posInfo.baseSlot() / 4) + posInfo.offsetZ();

            float offsetX = (2.5F + gridX * 3.75F) / scale;
            float offsetZ = (3.5F + gridZ * 3.25F) / scale;
            pose.translate(offsetX * px, -2 * px / scale + 0.01 * i, offsetZ * px);

            pose.mulPose(Axis.XP.rotationDegrees(45));

            renderer.renderStatic(stack, ItemDisplayContext.FIXED, light, overlay, pose, bufferSrc, Minecraft.getInstance().level, 0);

            pose.popPose();
            if (++i >= 16) break;
        }
    }
}
