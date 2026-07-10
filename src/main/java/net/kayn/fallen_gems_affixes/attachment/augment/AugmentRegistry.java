package net.kayn.fallen_gems_affixes.attachment.augment;

import com.google.common.base.Predicates;
import com.google.common.collect.*;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.kayn.fallen_gems_affixes.network.ClientLikeSyncAugmentPacket;
import net.kayn.fallen_gems_affixes.types.augment.IAugment;
import net.minecraft.resources.ResourceLocation;
import net.rtxyd.fallen.lib.runtime.forgemod.network.AbstractPacketBoundRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class AugmentRegistry extends AbstractPacketBoundRegistry<AugmentMeta, ClientLikeSyncAugmentPacket.Begin, ClientLikeSyncAugmentPacket, ClientLikeSyncAugmentPacket.End> {
    private final BiMap<ResourceLocation, IAugment> REGISTRY = HashBiMap.create();
    private Map<ResourceLocation, AugmentMeta> META_DATA = new HashMap<>();
    private BiMap<ResourceLocation, IAugment> REGISTRY_VIEW;

    public AugmentRegistry() {
        super(FallenGemsAffixes.LOGGER, "augments", "augment", Predicates.alwaysTrue(), true, true);
    }

    @Override
    public void beginReload() {
        super.beginReload();
        REGISTRY_VIEW = null;
        META_DATA = new HashMap<>();
    }

    @Override
    public void onReload() {
        FallenGemsAffixes.LOGGER.info("Loading augments...");
        super.onReload();
        FallenGemsAffixes.LOGGER.info("Finalize loading...");
        for (Map.Entry<ResourceLocation, AugmentMeta> entry : this.registry.entrySet()) {
            IAugment augment = REGISTRY.get(entry.getKey());
            if (augment != null) {
                META_DATA.put(entry.getKey(), entry.getValue());
            }
        }
        FallenGemsAffixes.LOGGER.info("Loading complete with {} entries", META_DATA.size());
    }

    public AugmentMeta getMetaData(IAugment augment) {
        return META_DATA.get(augment.getId());
    }

    public AugmentMeta getMetaData(ResourceLocation id) {
        IAugment augment = getAug(id);
        if (augment != null) {
            return getMetaData(augment);
        }
        return null;
    }

    @Override
    protected void registerBuiltinCodecs() {}

    @Override
    public void validateItem(ResourceLocation loc, AugmentMeta item) {

    }

    // Register an augment
    public IAugment register(IAugment augment) {
       REGISTRY.put(augment.getId(), augment);
       return augment;
    }
    // Get an augment by ID
    public IAugment getAug(ResourceLocation id) {
        return REGISTRY.get(id);
    }
    public IAugment getAug(String id) {
        return this.getAug(ResourceLocation.tryParse(id));
    }
    public ResourceLocation getId(IAugment augment) {
        return REGISTRY.inverse().get(augment);
    }
    public void forEach(BiConsumer<ResourceLocation, IAugment> consumer) {
        REGISTRY.forEach(consumer);
    }
    public BiMap<ResourceLocation, IAugment> registryView() {
        if (REGISTRY_VIEW == null) {
            REGISTRY_VIEW = ImmutableBiMap.copyOf(REGISTRY);
        }
        return REGISTRY_VIEW;
    }
}