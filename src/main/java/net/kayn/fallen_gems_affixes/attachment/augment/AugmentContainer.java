package net.kayn.fallen_gems_affixes.attachment.augment;

import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.types.augment.IAugment;
import net.kayn.fallen_gems_affixes.types.augment.IAugmentContainer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.*;

public class AugmentContainer implements IAugmentContainer, INBTSerializable<CompoundTag> {
    private final Map<IAugment, List<AugmentInstance>> augments = new HashMap<>();

    @Override
    public void addAugment(AugmentInstance instance) {
        List<AugmentInstance> list = augments.get(instance.getAugment());
        if (list != null) {
            list.add(instance);
        }
        else {
            list = new ArrayList<>();
            list.add(instance);
            augments.put(instance.getAugment(), list);
        }
    }

    @Override
    public boolean removeAugment(IAugment augment) {
        return augments.remove(augment) != null;
    }

    @Override
    public boolean removeAugment(AugmentInstance instance) {
        IAugment augment = instance.getAugment();
        List<AugmentInstance> list = augments.get(augment);
        if (list != null) {
            boolean result = list.remove(instance);
            if (list.isEmpty()) {
                removeAugment(augment);
            }
            return result;
        }
        return false;
    }

    @Override
    public boolean hasAugment(IAugment augment) {
        return augments.containsKey(augment);
    }

    @Override
    public Map<IAugment, List<AugmentInstance>> getAugments() {
        return Collections.unmodifiableMap(augments);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag result = new CompoundTag();
        CompoundTag augmentsTag = new CompoundTag();

        for (Map.Entry<IAugment, List<AugmentInstance>> entry : augments.entrySet()) {
            IAugment augment = entry.getKey();
            List<AugmentInstance> instances = entry.getValue();

            ListTag instList = new ListTag();
            for (AugmentInstance inst : instances) {
                instList.add(inst.serializeNBT());
            }
            augmentsTag.put(augment.getId().toString(), instList);
        }

        result.put("Augments", augmentsTag);
        return result;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        CompoundTag augmentsTag = nbt.getCompound("Augments");
        augments.clear();

        for (String key : augmentsTag.getAllKeys()) {
            ResourceLocation id = ResourceLocation.parse(key);
            IAugment augment = Fallen.Registries.AUGMENT_REGISTRY.getAug(id);
            if (augment == null) continue;

            ListTag list = augmentsTag.getList(key, Tag.TAG_COMPOUND);
            List<AugmentInstance> instances = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                CompoundTag instTag = list.getCompound(i);
                AugmentInstance inst = new AugmentInstance();
                inst.deserializeNBT(instTag);
                instances.add(inst);
            }
            augments.put(augment, instances);
        }
    }
}