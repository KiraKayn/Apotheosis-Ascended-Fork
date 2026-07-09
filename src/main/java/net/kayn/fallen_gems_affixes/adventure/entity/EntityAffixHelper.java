package net.kayn.fallen_gems_affixes.adventure.entity;

import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixRegistry;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.rtxyd.fallen.lib.runtime.forgemod.util.EntityCakyHandler;
import net.rtxyd.fallen.lib.runtime.forgemod.util.NBTFingerprints;
import net.rtxyd.fallen.lib.util.IObjectCaky;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Stores and retrieves entity-level affixes from an entity's {@link net.minecraft.nbt.CompoundTag}
 * persistent data.
 */
public final class EntityAffixHelper {

    public static final String TAG = "fga.entity_affixes";
    public static final IObjectCaky.CakyReviewer<Entity> HASHER = NBTFingerprints.entitySubTag(TAG);

    private EntityAffixHelper() {
    }

    //write

    public static void addAffix(LivingEntity entity, ResourceLocation affixId, String rarityKey, float level) {
        CompoundTag data = entity.getPersistentData();
        ListTag list = data.contains(TAG, Tag.TAG_LIST) ? data.getList(TAG, Tag.TAG_COMPOUND) : new ListTag();

        CompoundTag entry = new CompoundTag();
        entry.putString("affix", affixId.toString());
        entry.putString("rarity", rarityKey);
        entry.putFloat("level", Math.max(0f, Math.min(1f, level)));
        list.add(entry);
        data.put(TAG, list);
    }

    public static List<EntityAffixInstance> getAffixes(LivingEntity entity) {
        return EntityCakyHandler.resolveWith(entity, TAG, IObjectCaky.Type.FINAL, EntityAffixHelper::getAffixesInner, HASHER);
    }

// read

    public static List<EntityAffixInstance> getAffixesInner(Entity entity) {
        CompoundTag data = entity.getPersistentData();
        if (!data.contains(TAG, Tag.TAG_LIST)) return Collections.emptyList();

        ListTag list = data.getList(TAG, Tag.TAG_COMPOUND);
        if (list.isEmpty()) return Collections.emptyList();

        List<EntityAffixInstance> result = new ArrayList<>(list.size());
        for (Tag t : list) {
            if (!(t instanceof CompoundTag entry)) continue;
            try {
                ResourceLocation affixId = ResourceLocation.parse(entry.getString("affix"));
                String rarityKey = entry.getString("rarity");
                float level = entry.getFloat("level");

                DynamicHolder<Affix> affixHolder = AffixRegistry.INSTANCE.holder(affixId);
                DynamicHolder<LootRarity> rarityHolder = RarityRegistry.byLegacyId(rarityKey);

                result.add(new EntityAffixInstance(affixHolder, rarityHolder, level));
            } catch (Exception e) {
                FallenGemsAffixes.LOGGER.warn("[FGA] Failed to read entity affix entry {}: {}", t, e.getMessage());
            }
        }
        return result;
    }

    public static boolean hasAffixes(LivingEntity entity) {
        return entity.getPersistentData().contains(TAG, Tag.TAG_LIST) && !entity.getPersistentData().getList(TAG, Tag.TAG_COMPOUND).isEmpty();
    }
}