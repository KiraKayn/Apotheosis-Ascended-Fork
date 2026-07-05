package net.kayn.fallen_gems_affixes.adventure.set.trickster;

import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.kayn.fallen_gems_affixes.entity.ShadowCloneEntity;
import net.kayn.fallen_gems_affixes.entity.ShadowCloneRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class TricksterEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, FallenGemsAffixes.MOD_ID);

    public static final RegistryObject<EntityType<ShadowCloneEntity>> SHADOW_CLONE =
            ENTITY_TYPES.register("shadow_clone", () ->
                    EntityType.Builder.<ShadowCloneEntity>of(ShadowCloneEntity::new, MobCategory.MISC)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build("shadow_clone"));

    public static final ModelLayerLocation SHADOW_CLONE_LAYER =
            new ModelLayerLocation(FallenGemsAffixes.id("shadow_clone"), "main");

    public static void bootstrap(IEventBus modBus) {
        ENTITY_TYPES.register(modBus);
        modBus.addListener(TricksterEntities::registerAttributes);
        modBus.addListener(TricksterEntities::registerRenderers);
        modBus.addListener(TricksterEntities::registerLayers);
    }

    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(SHADOW_CLONE.get(), ShadowCloneEntity.createAttributes().build());
    }

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(SHADOW_CLONE.get(), ShadowCloneRenderer::new);
    }

    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(SHADOW_CLONE_LAYER, () ->
                LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F), 64, 64));
    }
}