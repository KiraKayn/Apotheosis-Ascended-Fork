package net.kayn.fallen_gems_affixes.mixin;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import dev.shadowsoffire.placebo.reload.WeightedDynamicRegistry;
import net.kayn.fallen_gems_affixes.Fallen;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RarityRegistry.class, remap = false)
public abstract class RarityRegistryMixin extends WeightedDynamicRegistry<LootRarity> {
    @Shadow @Final public static RarityRegistry INSTANCE;

    public RarityRegistryMixin(Logger logger, String path, boolean synced, boolean subtypes) {
        super(logger, path, synced, subtypes);
    }

    @Inject(method = "onReload", at = @At(value = "INVOKE", target = "Ldev/shadowsoffire/placebo/reload/WeightedDynamicRegistry;onReload()V", shift = At.Shift.AFTER))
    private void hookRarityPre(CallbackInfo ci) {
        BiMap<ResourceLocation, LootRarity> map = HashBiMap.create(this.registry);
        map.keySet().removeIf(Fallen.Common.FALLEN_RARITIES::contains);
        this.registry = ImmutableBiMap.copyOf(map);
    }
}
