package net.kayn.fallen_gems_affixes.attachment.augment;

import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixInstance;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import dev.shadowsoffire.placebo.util.CachedObject;
import net.kayn.fallen_gems_affixes.Fallen;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.rtxyd.fallen.lib.runtime.forgemod.addon.apotheosis.event.AffixApplyEvent;
import net.rtxyd.fallen.lib.runtime.forgemod.addon.apotheosis.event.AffixCacheRefreshEvent;
import net.rtxyd.fallen.lib.runtime.forgemod.addon.apotheosis.event.AffixGetFinishEvent;
import net.rtxyd.fallen.lib.runtime.forgemod.util.GameLifecycleHelper;
import net.rtxyd.fallen.lib.runtime.forgemod.util.ItemStackCakyHandler;
import net.rtxyd.fallen.lib.util.IObjectCaky;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static net.kayn.fallen_gems_affixes.Fallen.AugmentMisc.*;

public class SpecialAffixEventHandler {

    public static void register() {
        MinecraftForge.EVENT_BUS.addListener(SpecialAffixEventHandler::onAffixApply);
        MinecraftForge.EVENT_BUS.addListener(SpecialAffixEventHandler::inAffixCacheRefresh);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, SpecialAffixEventHandler::onAffixGetFinish);
    }

    private static void onAffixApply(AffixApplyEvent event) {
        GameLifecycleHelper.submitContextCall(Fallen.ContextKeys.APPLIED_AFFIX, event::getAffix);
    }

    // it triggers *in* affix cache logic chain, to block or manually refresh the cache
    // better don't change original cache.
    private static void inAffixCacheRefresh(AffixCacheRefreshEvent event) {
        getToModifyAffixesRefresh(event.getItem(), event.getAffixes());
    }

    // it triggers at getAffixes return, can modify the affix instances or check them.
    private static void onAffixGetFinish(AffixGetFinishEvent event) {
        ItemStack stack = event.getItem();
        ToModifyAffixes toModifyAffixes = getToModifyAffixes(stack);
        if (toModifyAffixes == ToModifyAffixes.EMPTY) return;
        var augs = AugmentHelper.getAugments(stack);
        if (toModifyAffixes.getFactor() != augs || toModifyAffixes.getInput() != event.getAffixesView()) {
            toModifyAffixes = getToModifyAffixesManualRefresh(stack, event.getAffixesView(), augs);
        }
        event.setTransientAffixes(toModifyAffixes.getOutput());
    }

    private static Map<DynamicHolder<? extends Affix>, AffixInstance> getAffixesManualRefresh(ItemStack stack, Map<DynamicHolder<? extends Affix>, AffixInstance> inses, int refresh) {
        if (refresh == 1) {
            stack.getOrCreateTagElement(AffixHelper.AFFIX_DATA).putBoolean(AFFIXES_REFRESH_MARKER, true);
            return CachedObject.CachedObjectSource.getOrCreate(stack, AffixHelper.AFFIX_CACHED_OBJECT,
                    AffixHelper::getAffixesImpl, CachedObject.hashSubkey(AffixHelper.AFFIX_DATA));
        } else {
            stack.getOrCreateTagElement(AffixHelper.AFFIX_DATA).remove(AFFIXES_REFRESH_MARKER);
            return CachedObject.CachedObjectSource.getOrCreate(stack, AffixHelper.AFFIX_CACHED_OBJECT,
                    AffixHelper::getAffixesImpl, CachedObject.hashSubkey(AffixHelper.AFFIX_DATA));
        }
    }

    private static Map<DynamicHolder<? extends Affix>, AffixInstance> getAffixesCC(ItemStack stack, Map<DynamicHolder<? extends Affix>, AffixInstance> inses, LiveAugments augs, int refresh) {
        if (refresh == 1) {
            return inses;
        } else {
            return getToModifyAffixesManualRefresh(stack, inses, augs).output();
        }
    }

    public static Map<DynamicHolder<? extends Affix>, AffixInstance> getAffixesFromNbt(ItemStack stack) {
        return CachedObject.CachedObjectSource.getOrCreate(stack, AffixHelper.AFFIX_CACHED_OBJECT, AffixHelper::getAffixesImpl, CachedObject.hashSubkey(AffixHelper.AFFIX_DATA));
    }

    public static ToModifyAffixes getToModifyAffixes(ItemStack stack) {
        return ItemStackCakyHandler.resolveWith(stack, TO_MODIFY_AFFIXES_OBJECT, IObjectCaky.Type.MANUAL,
                s -> getToModifyAffixesA(stack, getAffixesFromNbt(stack)),
                i -> getTmaFingerprintRefresher(i).get());
    }

    private static AtomicInteger getTmaFingerprintRefresher(ItemStack stack) {
        return ItemStackCakyHandler.resolveWith(stack, TO_MODIFY_AFFIXES_VERSION, IObjectCaky.Type.MANUAL, s -> new AtomicInteger(), i -> 0);
    }

    private static ToModifyAffixes getToModifyAffixes(ItemStack stack, Map<DynamicHolder<? extends Affix>, AffixInstance> affixes) {
        return ItemStackCakyHandler.resolveWith(stack, TO_MODIFY_AFFIXES_OBJECT, IObjectCaky.Type.MANUAL, s -> getToModifyAffixesA(s, affixes), i -> getTmaFingerprintRefresher(i).get());
    }

    private static ToModifyAffixes getToModifyAffixesRefresh(ItemStack stack, Map<DynamicHolder<? extends Affix>, AffixInstance> affixes) {
        return ItemStackCakyHandler.resolveWith(stack, TO_MODIFY_AFFIXES_OBJECT, IObjectCaky.Type.MANUAL, s -> getToModifyAffixesA(s, affixes), i -> getTmaFingerprintRefresher(i).incrementAndGet());
    }

    private static ToModifyAffixes getToModifyAffixesA(ItemStack stack, Map<DynamicHolder<? extends Affix>, AffixInstance> affixes) {
        if (stack.isEmpty()) return ToModifyAffixes.EMPTY;
        LiveAugments augments = AugmentHelper.getAugments(stack);
        if (affixes.isEmpty() && augments.isEmpty()) return ToModifyAffixes.EMPTY;
        return new ToModifyAffixes(affixes, augments);
    }

    private static ToModifyAffixes getToModifyAffixesManualRefresh(ItemStack stack, Map<DynamicHolder<? extends Affix>, AffixInstance> affixes, LiveAugments augments) {
        return ItemStackCakyHandler.resolveWith(stack, TO_MODIFY_AFFIXES_OBJECT, IObjectCaky.Type.MANUAL, s -> new ToModifyAffixes(affixes, augments), i -> getTmaFingerprintRefresher(i).incrementAndGet());
    }
}
