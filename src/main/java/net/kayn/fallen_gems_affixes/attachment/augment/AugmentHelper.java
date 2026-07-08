package net.kayn.fallen_gems_affixes.attachment.augment;

import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.attachment.augment.mod_events.ApplyAugmentEvent;
import net.kayn.fallen_gems_affixes.types.augment.IAugment;
import net.kayn.fallen_gems_affixes.types.augment.IAugmentInnerData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.rtxyd.fallen.lib.runtime.forgemod.util.ItemStackCakyHandler;
import net.rtxyd.fallen.lib.runtime.forgemod.util.NBTFingerprints;

import java.util.*;

import static net.kayn.fallen_gems_affixes.Fallen.AugmentMisc.*;

@SuppressWarnings({"ConstantConditions", "rawtypes"})
public class AugmentHelper {
    private static final ThreadLocal<Boolean> RECURSE = ThreadLocal.withInitial(() -> false);

    public static LiveAugments getAugments(ItemStack stack) {
        return ItemStackCakyHandler.resolve(stack, AUGMENT_CACHED_OBJECT, AugmentHelper::getAugmentsA, NBTFingerprints.subTag(AUGMENT_DATA));
    }

    public static void applyAugment(ItemStack stack, AugmentInstance inst) {
        try {
            if (RECURSE.get()) {
                throw new UnsupportedOperationException("Attempt to recursively apply augment.");
            }
            RECURSE.set(true);
            if (!MinecraftForge.EVENT_BUS.post(new ApplyAugmentEvent(stack, inst))) {
                Map<IAugment, AugmentInstance> augments = getAugments(stack).toMap();
                if (inst.getAugment().onApply(stack, augments, inst)) {
                    augments.put(inst.getAugment(), inst);
                    setAugments(stack, augments);
                }
            }
        } finally {
            RECURSE.set(false);
        }
    }

    public static void applyAugmentRaw(ItemStack stack, IAugment augment) {
        applyAugment(stack, new AugmentInstance(augment, augment.fallbackInnerData()));
    }

    public static boolean canApplyTo(LootCategory category, IAugment augment) {
        AugmentMeta meta = Fallen.Registries.AUGMENT_REGISTRY.getMetaData(augment);
        if (meta != null) {
            return meta.canApplyTo(category);
        }
        return false;
    }

    public static void setAugments(ItemStack stack, Map<IAugment, AugmentInstance> instances) {
        CompoundTag tag1 = stack.getOrCreateTagElement(AUGMENT_DATA);
        var listTag = new ListTag();
        for (Map.Entry<IAugment, AugmentInstance> en : instances.entrySet()) {
            CompoundTag tag = new CompoundTag();
            tag.putString(TYPE, en.getKey().getId().toString());
            tag.put(INNER_DATA, en.getValue().getData().serializeNBT());
            listTag.add(tag);
        }
        tag1.put(AUGMENTS, listTag);
    }

    public static boolean hasAugment(ItemStack stack, IAugment augment) {
        return getAugments(stack).get(augment) != null;
    }

    public static boolean hasAugments(ItemStack stack) {
        return stack.getTag() != null && stack.getTag().contains(Fallen.AugmentMisc.AUGMENT_DATA);
    }

    private static LiveAugments getAugmentsA(ItemStack stack) {
        if (stack.isEmpty()) return LiveAugments.EMPTY;
        Map<IAugment, AugmentInstance> map = new HashMap<>();
        if (!stack.isEmpty() && stack.hasTag() && stack.getTag().contains(Fallen.AugmentMisc.AUGMENT_DATA)) {
            ListTag listTag = stack.getTagElement(Fallen.AugmentMisc.AUGMENT_DATA).getList(AUGMENTS, Tag.TAG_COMPOUND);
            for (int i = 0; i < listTag.size(); i++) {
                CompoundTag tag = listTag.getCompound(i);
                IAugment augment = Fallen.Registries.AUGMENT_REGISTRY.getAug(tag.getString(TYPE));
                if (augment != null) {
                    CompoundTag inner = tag.getCompound(INNER_DATA);
                    if (inner.isEmpty()) continue;
                    IAugmentInnerData data = augment.deserializeInnerData(inner);
                    data.stackContext(stack);
                    map.put(augment, new AugmentInstance(augment, data));
                }
            }
        }
        if (map.isEmpty()) return LiveAugments.EMPTY;
        return new LiveAugments(map);
    }
}
