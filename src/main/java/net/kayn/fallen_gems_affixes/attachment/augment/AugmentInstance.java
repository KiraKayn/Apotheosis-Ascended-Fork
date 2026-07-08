package net.kayn.fallen_gems_affixes.attachment.augment;

import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.types.augment.IAugment;
import net.kayn.fallen_gems_affixes.types.augment.IAugmentInnerData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.*;

import static net.kayn.fallen_gems_affixes.Fallen.AugmentMisc.UNIQUE_ID;

public class AugmentInstance implements INBTSerializable<CompoundTag> {
    public static final Map<UUID, AugmentInstance> augmentInstanceUuidMap = new HashMap<>();
    public static final String AUGMENT_ID = "augmentId";
    public static final String AUGMENT_DATA = "augmentData";

    private IAugment augment;
    private IAugmentInnerData augmentData;
    private UUID uuid;

    public AugmentInstance(IAugment augment, IAugmentInnerData augmentData) {
        this.augment = augment;
        this.augmentData = augmentData;
    }

    AugmentInstance() {
        this.augment = null;
        this.augmentData = null;
    }

    public ResourceLocation getId() {
        return augment != null ? augment.getId() : null;
    }

    public IAugment getAugment() {
        return augment;
    }

    public IAugmentInnerData getData() {
        return augmentData;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void enable() {
        augmentData.enable();
    }

    public void disable() {
        augmentData.disable();
    }

    public boolean isFunctional() {
        return augmentData.isFunctional();
    }

    public static void store(UUID uuid, AugmentInstance instance) {
        augmentInstanceUuidMap.put(uuid, instance);
    }

    public static void delete(UUID uuid) {
        augmentInstanceUuidMap.remove(uuid);
    }

    public static AugmentInstance get(UUID uuid) {
        return augmentInstanceUuidMap.get(uuid);
    }

    public static boolean contains(UUID uuid) {
        return augmentInstanceUuidMap.containsKey(uuid);
    }

    // We can't set augment at will, they should be bound to instance.
//    public void setAugment(IAugment augment) {
//        this.augment = augment;
//    }

    public UUID generateUniqueUUID() {
        UUID newUUID;
        int attempts = 0;

        do {
            newUUID = UUID.randomUUID();
            attempts++;

            if (attempts > 1000) {
                throw new RuntimeException("UUID error, check random generator");
            }
        } while (augmentInstanceUuidMap.containsKey(newUUID));

        this.uuid = newUUID;

        return newUUID;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();

        if (augment != null) {
            tag.putString(AUGMENT_ID, augment.getId().toString());
            tag.putUUID(UNIQUE_ID, uuid);
            if (augmentData != null) {
                CompoundTag augmentData1 = augmentData.serializeNBT();
                tag.put(AUGMENT_DATA, augmentData1);
            }
        }

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains(AUGMENT_ID)) {
            String augmentIdString = tag.getString(AUGMENT_ID);
            ResourceLocation augmentId = ResourceLocation.parse(augmentIdString);

            IAugment template = Fallen.Registries.AUGMENT_REGISTRY.getAug(augmentId);
            if (template != null) {
                this.augment = template;

                if (tag.contains(AUGMENT_DATA)) {
                    this.augmentData = this.augment.parseEntityAugment(tag.getCompound(AUGMENT_DATA));
                }
            }
        }
        if (tag.contains(UNIQUE_ID)) {
            this.uuid = tag.getUUID(UNIQUE_ID);
        }
    }

    @Override
    public String toString() {
        return "AugmentInstance{" +
                "augment=" + (augment != null ? augment.toString() : "null") +
                "augmentData=" + (augmentData != null ? augmentData.toString() : "null") +
                "}";
    }
}