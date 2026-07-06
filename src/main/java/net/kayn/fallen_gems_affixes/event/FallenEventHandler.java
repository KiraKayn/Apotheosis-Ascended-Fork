package net.kayn.fallen_gems_affixes.event;

import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixRegistry;
import dev.shadowsoffire.apotheosis.adventure.event.GetItemSocketsEvent;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.adventure.affix.SocketBonusAffix;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.rtxyd.fallen.lib.runtime.forgemod.util.ItemStackCakyHandler;
import net.rtxyd.fallen.lib.runtime.forgemod.util.NBTFingerprints;

import static dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper.*;

public class FallenEventHandler {

    @SubscribeEvent
    public static void hookAddSocketsAffix(GetItemSocketsEvent event) {
        ItemStack stack = event.getStack();
        if (stack.isEmpty()) return;
        if (!AffixHelper.hasAffixes(stack)) return;
        event.setSockets(event.getSockets() + getAdditionalSocketsByAffix(stack));
    }

    public static int getAdditionalSocketsByAffix(ItemStack stack) {
        return ItemStackCakyHandler.resolve(stack, Fallen.AugmentMisc.ADDITIONAL_SOCKET_AFFIX_CACHE, FallenEventHandler::getAdditionalSocketsA, NBTFingerprints.subTag(AFFIX_DATA));
    }

    public static int getPureSockets(ItemStack stack) {
        CompoundTag afxData = stack.getTagElement("affix_data");
        return afxData != null ? afxData.getInt("sockets") : 0;
    }

    public static boolean isAffixCombined(ItemStack stack) {
        return stack.getTag() != null && stack.getTag().contains(Fallen.AugmentMisc.AFFIX_COMBINED);
    }

    private static int getAdditionalSocketsA(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        CompoundTag afxData = stack.getTagElement(AFFIX_DATA);
        if (afxData != null && afxData.contains(AFFIXES)) {
            CompoundTag affixes = afxData.getCompound(AFFIXES);
            DynamicHolder<LootRarity> rarity = getRarity(afxData);
            if (!rarity.isBound()) {
                rarity = RarityRegistry.getMinRarity();
            }
            LootCategory cat = LootCategory.forItem(stack);
            String id = SocketBonusAffix.ID.toString();
            for(String key : affixes.getAllKeys()) {
                if (key.equals(id)) {
                    DynamicHolder<Affix> affix = AffixRegistry.INSTANCE.holder(SocketBonusAffix.ID);
                    if (affix.isBound() && affix.get().canApplyTo(stack, cat, rarity.get())) {
                        float lvl = affixes.getFloat(key);
                        return ((SocketBonusAffix)affix.get()).getBonusSockets(rarity.get(), lvl);
                    }
                }
            }
        }
        return 0;
    }
}