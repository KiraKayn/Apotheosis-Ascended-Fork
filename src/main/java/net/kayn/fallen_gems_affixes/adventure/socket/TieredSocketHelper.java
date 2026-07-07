package net.kayn.fallen_gems_affixes.adventure.socket;

import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.socket.SocketHelper;
import dev.shadowsoffire.apotheosis.adventure.socket.SocketedGems;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemInstance;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;

public final class TieredSocketHelper {

    private TieredSocketHelper() {}

    public static final String SOCKET_TIERS_KEY = "tiered_socket_tiers";
    public static final int REGULAR_SOCKET = -1;

    public static int[] getSocketTiers(ItemStack stack) {
        CompoundTag afxData = stack.getTagElement("affix_data");
        if (afxData == null || !afxData.contains(SOCKET_TIERS_KEY)) return new int[0];
        return afxData.getIntArray(SOCKET_TIERS_KEY);
    }

    public static int getSocketTier(ItemStack stack, int index) {
        int[] tiers = getSocketTiers(stack);
        if (index < 0 || index >= tiers.length) return REGULAR_SOCKET;
        return tiers[index];
    }

    public static void setSocketTiers(ItemStack stack, int[] tiers) {
        stack.getOrCreateTagElement("affix_data").putIntArray(SOCKET_TIERS_KEY, tiers);
    }


    public static int getGemRarityOrdinal(GemInstance gem) {
        DynamicHolder<LootRarity> holder = gem.rarity();
        if (!holder.isBound()) return -1;
        return holder.get().ordinal();
    }

    public static boolean canGemFitSocket(ItemStack item, GemInstance gem, int socketIndex, TieredSocketMode mode) {
        return canGemFitTier(getSocketTier(item, socketIndex), getGemRarityOrdinal(gem), mode);
    }

    public static boolean canGemFitTier(int socketTier, int gemOrdinal, TieredSocketMode mode) {
        if (socketTier == REGULAR_SOCKET) return true;
        if (gemOrdinal < 0) return false;

        return switch (mode) {
            case HARDCORE -> gemOrdinal == socketTier;
            case ON       -> gemOrdinal <= socketTier;
        };
    }

    public static boolean hasCompatibleEmptySocket(ItemStack item, GemInstance gem, TieredSocketMode mode) {
        return getFirstCompatibleEmptySocket(item, gem, mode) >= 0;
    }

    public static int getFirstCompatibleEmptySocket(ItemStack item, GemInstance gem, TieredSocketMode mode) {
        SocketedGems gems = SocketHelper.getGems(item);
        int gemOrdinal = getGemRarityOrdinal(gem);

        int bestIndex     = -1;
        int bestEffective = Integer.MAX_VALUE;

        for (int i = 0; i < gems.size(); i++) {
            if (gems.get(i).isValid()) continue;

            int tier = getSocketTier(item, i);
            if (!canGemFitTier(tier, gemOrdinal, mode)) continue;

            int effective = (tier == REGULAR_SOCKET) ? Integer.MAX_VALUE - 1 : tier;
            if (effective < bestEffective) {
                bestEffective = effective;
                bestIndex     = i;
            }
        }

        return bestIndex;
    }

    public static int getFirstCompatibleEmptySocket(int[] tiers, boolean[] occupied, int gemOrdinal, TieredSocketMode mode) {
        int bestIndex     = -1;
        int bestEffective = Integer.MAX_VALUE;

        for (int i = 0; i < occupied.length; i++) {
            if (occupied[i]) continue;

            int tier = (i < tiers.length) ? tiers[i] : REGULAR_SOCKET;
            if (!canGemFitTier(tier, gemOrdinal, mode)) continue;

            int effective = (tier == REGULAR_SOCKET) ? Integer.MAX_VALUE - 1 : tier;
            if (effective < bestEffective) {
                bestEffective = effective;
                bestIndex     = i;
            }
        }

        return bestIndex;
    }

    public static boolean hasEmptyRegularSocket(ItemStack item) {
        return getFirstEmptyRegularSocket(item) >= 0;
    }

    public static int getFirstEmptyRegularSocket(ItemStack item) {
        SocketedGems gems = SocketHelper.getGems(item);
        for (int i = 0; i < gems.size(); i++) {
            if (!gems.get(i).isValid() && getSocketTier(item, i) == REGULAR_SOCKET) {
                return i;
            }
        }
        return -1;
    }

    public static void assignSocketTiersToLootItem(ItemStack stack, RandomSource rand) {
        if (!SocketTierManager.INSTANCE.isEnabled()) return;

        CompoundTag afxData = stack.getTagElement("affix_data");
        int totalSockets = afxData != null ? afxData.getInt("sockets") : 0;
        if (totalSockets <= 0) return;

        int[] existing = getSocketTiers(stack);
        int[] newTiers = new int[totalSockets];

        SocketedGems gems = SocketHelper.getGems(stack);

        for (int i = 0; i < totalSockets; i++) {
            if (i < existing.length && i < gems.size() && gems.get(i).isValid()) {
                newTiers[i] = existing[i];
            }
            else if (i < existing.length && existing[i] == REGULAR_SOCKET) {
                newTiers[i] = REGULAR_SOCKET;
            }
            else {
                newTiers[i] = SocketTierManager.INSTANCE.rollSocketTier(rand);
            }
        }

        setSocketTiers(stack, newTiers);
    }

    public static void addRegularSocket(ItemStack stack) {
        CompoundTag afxData = stack.getTagElement("affix_data");
        int totalSockets = afxData != null ? afxData.getInt("sockets") : 0;

        int[] existing = getSocketTiers(stack);
        if (existing.length >= totalSockets) return;

        int[] newTiers = Arrays.copyOf(existing, totalSockets);
        for (int i = existing.length; i < totalSockets; i++) {
            newTiers[i] = REGULAR_SOCKET;
        }
        setSocketTiers(stack, newTiers);
    }

    public static void removeSocketTierAt(ItemStack stack, int index) {
        int[] existing = getSocketTiers(stack);
        if (index < 0 || index >= existing.length) return;

        int[] newTiers = new int[existing.length - 1];
        System.arraycopy(existing, 0, newTiers, 0, index);
        System.arraycopy(existing, index + 1, newTiers, index, existing.length - index - 1);
        setSocketTiers(stack, newTiers);
    }

    public static void trimSocketTiers(ItemStack stack) {
        CompoundTag afxData = stack.getTagElement("affix_data");
        int totalSockets = Math.max(0, afxData != null ? afxData.getInt("sockets") : 0);
        int[] existing = getSocketTiers(stack);
        if (existing.length <= totalSockets) return;
        setSocketTiers(stack, Arrays.copyOf(existing, totalSockets));
    }


    public static String getEmptySocketTranslationKey(int ordinal) {
        SocketTierDefinition def = SocketTierManager.INSTANCE.getByOrdinal(ordinal);
        if (def == null) return "socket_tier.fallen_gems_affixes.empty.regular";
        return def.getEmptyTranslationKey();
    }
}