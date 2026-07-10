package net.kayn.fallen_gems_affixes.attachment.rarity;

import com.google.common.base.Predicates;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import dev.shadowsoffire.placebo.reload.DynamicRegistry;
import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.kayn.fallen_gems_affixes.mixin.DynamicHolderAccessor;
import net.kayn.fallen_gems_affixes.mixin.DynamicRegistryAccessor;
import net.kayn.fallen_gems_affixes.mixin.RarityRegistryAccessor;
import net.minecraft.resources.ResourceLocation;
import net.rtxyd.fallen.lib.runtime.forgemod.addon.apotheosis.SimpleRarityRegistry;
import net.rtxyd.fallen.lib.runtime.forgemod.network.AbstractPacketBoundRegistry;
import net.rtxyd.fallen.lib.runtime.forgemod.util.GameLifecycleHelper;
import net.rtxyd.fallen.lib.runtime.forgemod.util.ILocalRarity;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class FallenRarityRegistry extends AbstractPacketBoundRegistry<FallenRarity, ClientLikeSyncFallenRarityPacket.Begin, ClientLikeSyncFallenRarityPacket, ClientLikeSyncFallenRarityPacket.End> implements Iterable<Map.Entry<ResourceLocation, FallenRarity>> {
    public final SimpleRarityRegistry<ResourceLocation, ILocalRarity> rarityRegistry = new SimpleRarityRegistry<>();

    public FallenRarityRegistry() {
        super(FallenGemsAffixes.LOGGER, "fallen_rarities", "type", Predicates.alwaysTrue(), true, false);
    }

    @Override
    protected void registerBuiltinCodecs() {
        this.registerCodec(ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "fallen_rarity"), FallenRarity.CODEC);
    }

    @Override
    public void beginReload() {
        super.beginReload();
        rarityRegistry.reset();
    }

    @Override
    public void onReload() {
        super.onReload();
        FallenGemsAffixes.LOGGER.info("Loading rarities...");
        for (DynamicHolder<LootRarity> ra : RarityRegistry.INSTANCE.getOrderedRarities()) {
            if (ra.get() instanceof ILocalRarity rarity) {
                rarityRegistry.register(new FallenRarity(rarity.fallen_lib$getId(), rarity));
            }
        }
        for (Map.Entry<ResourceLocation, FallenRarity> en : this.registry.entrySet()) {
            FallenRarity rarity = en.getValue();
            rarityRegistry.register(rarity);
        }
        FallenGemsAffixes.LOGGER.info("Finalize loading...");
        processApothRarities();
        FallenGemsAffixes.LOGGER.info("Loading complete with {} entries", rarityRegistry.getRarityMapView().size());
    }

    private void processApothRarities() {
        var regAccessor = (DynamicRegistryAccessor)RarityRegistry.INSTANCE;
        var ordAccessor = (RarityRegistryAccessor)RarityRegistry.INSTANCE;
        BiMap<ResourceLocation, LootRarity> map = HashBiMap.create(regAccessor.getRegistry());
        // clean ordered
        var ordered = new ArrayList<>(ordAccessor.getOrdered());
        ordered.removeIf(i -> Fallen.Common.FALLEN_RARITIES.contains(i.getId()));
        var fallenRarities = registry.values();
        // force registering fallen rarity into RarityRegister
        for (FallenRarity fallenRarity : fallenRarities) {
            ResourceLocation location = fallenRarity.getClassifier();
            LootRarity rarity = (LootRarity) fallenRarity.getRarity();
            if (rarity == null) continue;
            // refresh the holder
            regAccessor.getHolders().remove(location);
            RarityRegistry.INSTANCE.holder(location);
            map.put(location, rarity);
        }
        // freeze again
        ordAccessor.setOrdered(ImmutableList.copyOf(ordered));
        regAccessor.setRegistry(ImmutableBiMap.copyOf(map));
        // rebind the holders
        regAccessor.getHolders().values().forEach(h -> ((DynamicHolderAccessor)h).invokeBind());
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull Iterator<Map.Entry<ResourceLocation, FallenRarity>> iterator() {
        return (Iterator<Map.Entry<ResourceLocation, FallenRarity>>)(Object)rarityRegistry.getRarityMapView().entrySet().iterator();
    }
}
