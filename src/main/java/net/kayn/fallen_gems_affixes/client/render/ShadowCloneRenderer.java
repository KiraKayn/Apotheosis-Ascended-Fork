package net.kayn.fallen_gems_affixes.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.kayn.fallen_gems_affixes.adventure.set.trickster.TricksterEntities;
import net.kayn.fallen_gems_affixes.entity.ShadowCloneEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
@OnlyIn(Dist.CLIENT)
public class ShadowCloneRenderer extends MobRenderer<ShadowCloneEntity, HumanoidModel<ShadowCloneEntity>> {

    public static final ResourceLocation TEXTURE = FallenGemsAffixes.id("textures/entity/shadow_clone.png");

    public ShadowCloneRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new HumanoidModel<>(ctx.bakeLayer(SHADOW_CLONE_LAYER)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(ShadowCloneEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(ShadowCloneEntity entity, float yaw, float partials, PoseStack pose,
                       MultiBufferSource buffers, int light) {
        super.render(entity, yaw, partials, pose, buffers, light);
    }


    public static final ModelLayerLocation SHADOW_CLONE_LAYER =
            new ModelLayerLocation(FallenGemsAffixes.id("shadow_clone"), "main");

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(TricksterEntities.SHADOW_CLONE.get(), ShadowCloneRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(SHADOW_CLONE_LAYER, () ->
                LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F), 64, 64));
    }

    @Override
    protected RenderType getRenderType(ShadowCloneEntity entity, boolean visible, boolean invisible, boolean glowing) {
        return RenderType.entityTranslucent(TEXTURE);
    }
}