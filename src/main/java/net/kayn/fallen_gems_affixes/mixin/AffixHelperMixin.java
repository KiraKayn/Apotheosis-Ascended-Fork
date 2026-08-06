package net.kayn.fallen_gems_affixes.mixin;

import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixRegistry;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.adventure.socket.SocketHelper;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.adventure.set.SetAffixHelper;
import net.kayn.fallen_gems_affixes.attachment.augment.LiveAugments;
import net.kayn.fallen_gems_affixes.attachment.augment.SpecialAffixEventHandler;
import net.kayn.fallen_gems_affixes.attachment.augment.ToModifyAffixes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.rtxyd.fallen.lib.runtime.forgemod.util.GameLifecycleHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(value = AffixHelper.class, remap = false)
public class AffixHelperMixin {

    @Inject(method = "getAffixesImpl", at = @At("HEAD"), cancellable = true)
    private static void fga$injectFabledAffixPreservation(
            ItemStack stack,
            CallbackInfoReturnable<Map<DynamicHolder<? extends Affix>, AffixInstance>> cir) {

        if (stack.isEmpty()) return;

        DynamicHolder<LootRarity> rarityHolder = AffixHelper.getRarity(stack);
        if (!rarityHolder.isBound()) return;

        ResourceLocation rarityKey = RarityRegistry.INSTANCE.getKey(rarityHolder.get());
        if (rarityKey == null) return;
        if (!"fallen_gems_affixes:fabled".equals(rarityKey.toString())) return;

        if (!SetAffixHelper.hasSetAffix(stack)) return;

        CompoundTag afxData = stack.getTagElement(AffixHelper.AFFIX_DATA);
        if (afxData == null || !afxData.contains(AffixHelper.AFFIXES)) return;

        CompoundTag affixesTag = afxData.getCompound(AffixHelper.AFFIXES);
        if (affixesTag.isEmpty()) {
            cir.setReturnValue(Collections.emptyMap());
            return;
        }

        LootCategory cat = LootCategory.forItem(stack);
        Map<DynamicHolder<? extends Affix>, AffixInstance> result = new HashMap<>();

        for (String key : affixesTag.getAllKeys()) {
            ResourceLocation affixId;
            try {
                affixId = new ResourceLocation(key);
            } catch (Exception e) {
                continue;
            }

            DynamicHolder<Affix> affixHolder = AffixRegistry.INSTANCE.holder(affixId);
            if (!affixHolder.isBound()) continue;

            float lvl = affixesTag.getFloat(key);
            DynamicHolder<LootRarity> effectiveRarity = fga$findBestCompatibleRarity(affixHolder.get(), stack, cat);
            if (effectiveRarity == null) continue;
            result.put(affixHolder, new AffixInstance(affixHolder, stack, effectiveRarity, lvl));
        }

        cir.setReturnValue(Collections.unmodifiableMap(result));
    }

    @Unique
    private static DynamicHolder<LootRarity> fga$findBestCompatibleRarity(Affix affix, ItemStack stack, LootCategory cat) {
        List<DynamicHolder<LootRarity>> ordered = RarityRegistry.INSTANCE.getOrderedRarities();
        for (int i = ordered.size() - 1; i >= 0; i--) {
            DynamicHolder<LootRarity> holder = ordered.get(i);
            if (!holder.isBound()) continue;
            ResourceLocation key = RarityRegistry.INSTANCE.getKey(holder.get());
            if (key == null) continue;
            if ("fallen_gems_affixes:fabled".equals(key.toString())) continue;
            try {
                if (affix.canApplyTo(stack, cat, holder.get())) return holder;
            } catch (Exception ignored) {}
        }
        return null;
    }

    @Inject(method = "setAffixes", at = @At("HEAD"))
    private static void setAffixesA(ItemStack stack, Map<DynamicHolder<? extends Affix>, AffixInstance> affixes, CallbackInfo ci) {
        AffixInstance ins = GameLifecycleHelper.callAndRemoveIfSameTickFirst(Fallen.ContextKeys.APPLIED_AFFIX, GameLifecycleHelper.EMPTY_EX_CONSUMER);
        AffixInstance ins2 = GameLifecycleHelper.callAndRemoveIfSameTickFirst(Fallen.ContextKeys.REROLLED_AFFIX, GameLifecycleHelper.EMPTY_EX_CONSUMER);
        if (ins != null) {
            if (affixes.containsValue(ins)) {
                if (ins.stack() != stack) {
                    GameLifecycleHelper.submitContextCall(Fallen.ContextKeys.APPLIED_AFFIX, () -> ins);
                } else {
                    var tma = SpecialAffixEventHandler.getToModifyAffixes(stack);
                    var input = tma.getInput();
                    if (input.size() != affixes.size()) return;
                    affixes.clear();
                    affixes.putAll(input);
                    var attr = tma.getAttributes().get(ins.affix());
                    if (attr == null) return;
                    AffixInstance neoInst = attr.output(tma::createInsWith);
                    float diff = ins.level() - neoInst.level();
                    AffixInstance raw = input.get(ins.affix());
                    if (raw == null) return;
                    affixes.put(ins.affix(), ins.withNewLevel(raw.level() + diff));
                    return;
                }
            }
        }
        if (ins2 != null) {
            AffixInstance ins3 = GameLifecycleHelper.callAndRemoveIfSameTickFirst(Fallen.ContextKeys.REROLLED_REMOVE, GameLifecycleHelper.EMPTY_EX_CONSUMER);
            if (ins3 == null) return;
            if (affixes.containsValue(ins2)) {
                if (ins2.stack() != stack) {
                    GameLifecycleHelper.submitContextCall(Fallen.ContextKeys.REROLLED_AFFIX, () -> ins2);
                } else {
                    var tma = SpecialAffixEventHandler.getToModifyAffixes(stack);
                    var input = tma.getInput();
                    if (input.size() != affixes.size()) return;
                    affixes.clear();
                    affixes.putAll(tma.getInput());
                    affixes.remove(ins3.affix());
                    affixes.put(ins2.affix(), ins2);
                }
                return;
            }
        }
        var affixes1 = SpecialAffixEventHandler.getToModifyAffixes(stack);
        var affixes2 = affixes1.getOutput();
        for (AffixInstance value : affixes1.getInput().values()) {
            AffixInstance inst = affixes.get(value.affix());
            if (inst == null) continue;
            AffixInstance inst2 = affixes2.get(value.affix());
            if (inst2 == null) continue;
            if (inst.level() == inst2.level()) {
                affixes.put(value.affix(), value);
            }
        }
    }

    @Inject(method = "setAffixes", at = @At("RETURN"))
    private static void setAffixesB(ItemStack stack, Map<DynamicHolder<? extends Affix>, AffixInstance> affixes, CallbackInfo ci) {
        GameLifecycleHelper.removeWithoutCall(Fallen.ContextKeys.APPLIED_AFFIX);
        GameLifecycleHelper.removeWithoutCall(Fallen.ContextKeys.REROLLED_AFFIX);
        GameLifecycleHelper.removeWithoutCall(Fallen.ContextKeys.REROLLED_REMOVE);
    }

    // apply affix power when copy affixes to entity
    // arrows directly getAffixes and getGems from NBT
    @Inject(method = "copyFrom", at = @At(value = "RETURN"))
    private static void copyTweak(ItemStack stack, Entity entity, CallbackInfo ci) {
        CompoundTag entityAffixData = entity.getPersistentData().getCompound(AffixHelper.AFFIX_DATA);
        if (entityAffixData.isEmpty()) return;
        ToModifyAffixes toModifyAffixes = SpecialAffixEventHandler.getToModifyAffixes(stack);
        if (toModifyAffixes.getInput().isEmpty() || toModifyAffixes.getFactor().isEmpty()) return;

        CompoundTag affixesTag = entityAffixData.getCompound(AffixHelper.AFFIXES);
        for(AffixInstance inst : toModifyAffixes.getOutput().values()) {
            affixesTag.putFloat(inst.affix().getId().toString(), inst.level());
        }
        // apply maybe modified sockets
        entityAffixData.putInt(SocketHelper.SOCKETS, SocketHelper.getSockets(stack));
    }
}