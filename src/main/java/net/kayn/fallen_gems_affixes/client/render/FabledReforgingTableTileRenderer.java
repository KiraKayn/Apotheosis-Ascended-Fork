package net.kayn.fallen_gems_affixes.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Axis;
import dev.shadowsoffire.apotheosis.Apotheosis;
import net.kayn.fallen_gems_affixes.adventure.reforging.FabledReforging;
import net.kayn.fallen_gems_affixes.adventure.reforging.FabledReforgingTableTile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class FabledReforgingTableTileRenderer implements BlockEntityRenderer<FabledReforgingTableTile> {

    private static final ResourceLocation HAMMER = new ResourceLocation(Apotheosis.MODID, "item/hammer");

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(FabledReforging.TILE_TYPE.get(), ctx -> new FabledReforgingTableTileRenderer());
    }

    @Override
    @SuppressWarnings("deprecation")
    public void render(FabledReforgingTableTile tile, float partials, PoseStack matrix, MultiBufferSource pBufferSource, int light, int overlay) {
        ItemRenderer irenderer = Minecraft.getInstance().getItemRenderer();
        BakedModel base = irenderer.getItemModelShaper().getModelManager().getModel(HAMMER);
        matrix.pushPose();

        double px = 1 / 16D;

        matrix.scale(1.25F, 1.25F, 1.25F);
        matrix.translate(8.5 * px / 1.25, 16 * px / 1.25 - 0.015, 7 * px / 1.25);
        matrix.mulPose(Axis.YP.rotationDegrees(45));
        matrix.mulPose(Axis.XP.rotationDegrees(90));

        if (tile.step1) {
            float factor = tile.time % 60 + partials;
            float sin = Mth.sin(factor * Mth.PI / 120);
            float sinSq = sin * sin;
            matrix.translate(0.125 * sinSq, -0, -0.15 * sinSq);
            matrix.mulPose(Axis.YN.rotationDegrees(45 * sinSq));
        } else {
            float factor = tile.time % 5 + partials;
            float sin = Mth.sin(Mth.HALF_PI + factor * Mth.PI / 10);
            float sinSq = sin * sin;
            matrix.translate(0.125 * sinSq, -0, -0.15 * sinSq);
            matrix.mulPose(Axis.YN.rotationDegrees(45 * sinSq));
        }

        MultiBufferSource.BufferSource src = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        irenderer.renderModelLists(base, ItemStack.EMPTY, light, overlay, matrix, ItemRenderer.getFoilBufferDirect(src, ItemBlockRenderTypes.getRenderType(tile.getBlockState(), true), true, false));
        src.endBatch();

        matrix.popPose();
    }
}