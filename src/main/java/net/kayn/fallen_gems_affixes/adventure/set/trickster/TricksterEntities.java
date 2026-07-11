package net.kayn.fallen_gems_affixes.adventure.set.trickster;

import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.kayn.fallen_gems_affixes.entity.ShadowCloneEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
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

    public static void bootstrap(IEventBus modBus) {
        ENTITY_TYPES.register(modBus);
        modBus.addListener(TricksterEntities::registerAttributes);
    }

    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(SHADOW_CLONE.get(), ShadowCloneEntity.createAttributes().build());
    }
}