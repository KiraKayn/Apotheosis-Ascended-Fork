package net.kayn.fallen_gems_affixes.adventure.set;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import dev.shadowsoffire.placebo.util.CachedObject;
import dev.shadowsoffire.placebo.util.CachedObject.CachedObjectSource;
import net.kayn.fallen_gems_affixes.Fallen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.rtxyd.fallen.lib.runtime.forgemod.network.BoundHolder;

import javax.annotation.Nullable;
import java.util.Optional;

public final class SetAffixHelper {
    public static final ResourceLocation SET_AFFIX_CACHED_OBJECT = Apotheosis.loc("set_affixes");

    public static final String SET_AFFIX_KEY = "set_affix";
    public static final String SET_AFFIX_LEVEL = "set_affix_lvl";

    public static void applySetAffix(ItemStack stack, SetAffix affix) {
        applySetAffix(stack, affix, 0.5F);
    }

    public static void applySetAffix(ItemStack stack, SetAffix affix, float level) {
        ResourceLocation key = Fallen.Registries.SET_AFFIX_REGISTRY.getKey(affix);
        if (key == null) return;
        CompoundTag afxData = stack.getOrCreateTagElement(AffixHelper.AFFIX_DATA);
        afxData.putString(SET_AFFIX_KEY, key.toString());
        afxData.putFloat(SET_AFFIX_LEVEL, level);
    }

    public static Optional<SetAffixInstance> getSetAffixInstance(ItemStack stack) {
        if (Fallen.Registries.SET_AFFIX_REGISTRY.getValues().isEmpty() || stack.isEmpty()) return Optional.empty();
        SetAffixInstance inst = CachedObjectSource.getOrCreate(stack, SET_AFFIX_CACHED_OBJECT, SetAffixHelper::getSetAffixImpl, CachedObject.hashSubkey(AffixHelper.AFFIX_DATA));
        return Optional.ofNullable(inst);
    }

    @Nullable
    private static SetAffixInstance getSetAffixImpl(ItemStack stack) {
        if (!stack.hasTag()) return null;
        CompoundTag afxData = stack.getTagElement(AffixHelper.AFFIX_DATA);
        if (afxData == null || !afxData.contains(SET_AFFIX_KEY)) return null;

        String raw = afxData.getString(SET_AFFIX_KEY);
        if (raw.isEmpty()) return null;

        BoundHolder<SetAffix> affix = Fallen.Registries.SET_AFFIX_REGISTRY.holder(new ResourceLocation(raw));
        DynamicHolder<LootRarity> rarity = AffixHelper.getRarity(stack);
        if (!rarity.isBound()) rarity = RarityRegistry.getMinRarity();

        LootCategory cat = LootCategory.forItem(stack);
        if (!affix.isValid() || !affix.get().canApplyTo(stack, cat, rarity.get())) return null;

        float lvl = afxData.contains(SET_AFFIX_LEVEL) ? afxData.getFloat(SET_AFFIX_LEVEL) : 0.5F;
        return new SetAffixInstance(affix, stack, rarity, lvl);
    }

    @Nullable
    public static ResourceLocation getSetAffixId(ItemStack stack) {
        return getSetAffixInstance(stack).map(inst -> inst.affix().getId()).orElse(null);
    }

    @Nullable
    public static SetAffix getSetAffix(ItemStack stack) {
        return getSetAffixInstance(stack).map(SetAffixInstance::afx).orElse(null);
    }

    @Nullable
    public static ResourceLocation getSetId(ItemStack stack) {
        return getSetAffixInstance(stack).map(inst -> inst.afx().getSetId()).orElse(null);
    }

    public static boolean hasSetAffix(ItemStack stack) {
        return getSetAffixInstance(stack).isPresent();
    }

    private SetAffixHelper() {}
}