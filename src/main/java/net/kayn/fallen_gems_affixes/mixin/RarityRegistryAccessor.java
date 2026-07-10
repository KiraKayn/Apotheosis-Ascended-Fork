package net.kayn.fallen_gems_affixes.mixin;

import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(value = RarityRegistry.class, remap = false)
public interface RarityRegistryAccessor {
    @Accessor
    List<DynamicHolder<LootRarity>> getOrdered();
    @Accessor
    void setOrdered(List<DynamicHolder<LootRarity>> ordered);
}
