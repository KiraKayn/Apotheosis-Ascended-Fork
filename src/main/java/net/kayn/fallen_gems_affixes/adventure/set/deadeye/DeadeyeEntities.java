package net.kayn.fallen_gems_affixes.adventure.set.deadeye;

import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.kayn.fallen_gems_affixes.entity.SpiritEchoEntity;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class DeadeyeEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, FallenGemsAffixes.MOD_ID);

    public static final RegistryObject<EntityType<SpiritEchoEntity>> SPIRIT_ECHO =
            ENTITY_TYPES.register("spirit_echo", () ->
                    EntityType.Builder.<SpiritEchoEntity>of(SpiritEchoEntity::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .fireImmune()
                            .build(null));

    public static void bootstrap(IEventBus modBus) {
        ENTITY_TYPES.register(modBus);
        modBus.addListener(DeadeyeEntities::registerAttributes);
        modBus.addListener(DeadeyeEntities::registerRenderers);
    }

    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(SPIRIT_ECHO.get(), SpiritEchoEntity.createAttributes().build());
    }

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(SPIRIT_ECHO.get(), ThrownItemRenderer::new);
    }
}