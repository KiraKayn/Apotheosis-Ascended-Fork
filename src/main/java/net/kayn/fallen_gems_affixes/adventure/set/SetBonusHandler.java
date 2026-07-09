package net.kayn.fallen_gems_affixes.adventure.set;

import net.kayn.fallen_gems_affixes.Fallen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

public class SetBonusHandler {
    private static final Map<Player, Map<ResourceLocation, Integer>> activeSetCounts = new WeakHashMap<>();
    private static final Map<Player, Map<ResourceLocation, Integer>> activeBonusTiers = new WeakHashMap<>();

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;
        updateSets(player);
    }

    public static void updateSets(Player player) {
        Map<ResourceLocation, Integer> counts = new HashMap<>();
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = player.getItemBySlot(slot);
            ResourceLocation setId = SetAffixHelper.getSetId(stack);
            if (setId != null) {
                counts.merge(setId, 1, Integer::sum);
            }
        }

        Map<ResourceLocation, Integer> prevCounts = activeSetCounts.getOrDefault(player, new HashMap<>());
        Map<ResourceLocation, Integer> prevTiers = activeBonusTiers.getOrDefault(player, new HashMap<>());
        Map<ResourceLocation, Integer> newTiers = new HashMap<>();

        for (var entry : counts.entrySet()) {
            ResourceLocation setId = entry.getKey();
            int count = entry.getValue();
            int tier = computeTier(setId, count);
            newTiers.put(setId, tier);
            int prevTier = prevTiers.getOrDefault(setId, 0);
            if (tier != prevTier) {
                applyBonusChange(player, setId, count, tier);
            }
        }

        for (ResourceLocation setId : prevCounts.keySet()) {
            if (!counts.containsKey(setId)) {
                removeSetBonuses(player, setId);
            }
        }

        activeSetCounts.put(player, counts);
        activeBonusTiers.put(player, newTiers);
    }

    private static int computeTier(ResourceLocation setId, int pieceCount) {
        int tier = 0;
        SetAffix affix = Fallen.Registries.SET_AFFIX_REGISTRY.getValue(setId);
        if (affix != null) {
            for (int threshold : affix.getBonusThresholds()) {
                if (pieceCount >= threshold) tier = Math.max(tier, threshold);
            }
        }
        return tier;
    }

    private static void applyBonusChange(Player player, ResourceLocation setId, int pieceCount, int tier) {
        SetAffix affix = Fallen.Registries.SET_AFFIX_REGISTRY.getValue(setId);
        if (affix != null) {
            affix.removeSetBonus(player);
            if (tier > 0) affix.applySetBonus(player, pieceCount);
        }
    }

    private static void removeSetBonuses(Player player, ResourceLocation setId) {
        SetAffix affix = Fallen.Registries.SET_AFFIX_REGISTRY.getValue(setId);
        if (affix != null) {
            affix.removeSetBonus(player);
        }
    }

    public static int getSetPieceCount(Player player, ResourceLocation setId) {
        return activeSetCounts.getOrDefault(player, Map.of()).getOrDefault(setId, 0);
    }
}