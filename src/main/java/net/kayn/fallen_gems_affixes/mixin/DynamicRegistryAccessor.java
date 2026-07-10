package net.kayn.fallen_gems_affixes.mixin;

import com.google.common.collect.BiMap;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import dev.shadowsoffire.placebo.reload.DynamicRegistry;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(value = DynamicRegistry.class, remap = false)
public interface DynamicRegistryAccessor {
    @Accessor
    BiMap<ResourceLocation, LootRarity> getRegistry();
    @Accessor
    void setRegistry(BiMap<ResourceLocation, LootRarity> registry);
    @Accessor("holders")
    Map<ResourceLocation, DynamicHolder<?>> getHolders();
}
