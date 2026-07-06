package net.kayn.fallen_gems_affixes.event;

import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.kayn.fallen_gems_affixes.compat.curios.CuriosSoulboundCompat;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;

import java.util.*;

public class SoulboundEventHandler {

    private static final String TAG_SOULBOUND = "fallen_gems_affixes:soulbound_items";
    private static final String TAG_EQUIPPED_ITEMS = "fallen_gems_affixes:equipped_items";
    private static final String TAG_SOULBOUND_CURIOS = "fallen_gems_affixes:soulbound_curios";
    private static final ResourceLocation SOULBOUND_ID = ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "soulbound");
    private static final String CURIOS_MODID = "curios";

    // Temporary storage for equipped items (only exists during death process)
    private static final Map<UUID, List<ItemStack>> tempEquippedItems = new HashMap<>();
    private static final Map<UUID, List<CuriosSoulboundCompat.CurioEntry>> tempEquippedCurios = new HashMap<>();

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        List<ItemStack> equippedSoulbound = new ArrayList<>();

        // Check armor and offhand slots for soulbound items
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot == EquipmentSlot.MAINHAND) continue; // Skip mainhand


            ItemStack stack = player.getItemBySlot(slot);
            if (!stack.isEmpty() && hasSoulboundAffix(stack)) {
                equippedSoulbound.add(stack.copy());
            }
        }

        // Store equipped soulbound items temporarily
        tempEquippedItems.put(player.getUUID(), equippedSoulbound);

        if (ModList.get().isLoaded(CURIOS_MODID)) {
            tempEquippedCurios.put(player.getUUID(), CuriosSoulboundCompat.removeSoulboundCurios(player, SoulboundEventHandler::hasSoulboundAffix));
        } else {
            tempEquippedCurios.put(player.getUUID(), new ArrayList<>());
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        List<ItemStack> soulboundItems = new ArrayList<>();
        List<ItemStack> equippedItems = tempEquippedItems.getOrDefault(player.getUUID(), new ArrayList<>());
        List<CuriosSoulboundCompat.CurioEntry> soulboundCurios = tempEquippedCurios.getOrDefault(player.getUUID(), new ArrayList<>());

        event.getDrops().removeIf(itemEntity -> {
            ItemStack stack = itemEntity.getItem();
            if (hasSoulboundAffix(stack)) {
                soulboundItems.add(stack.copy());
                return true;
            }
            return false;
        });

        if (!soulboundItems.isEmpty() || !soulboundCurios.isEmpty()) {
            storeSoulboundItems(player, soulboundItems, equippedItems, soulboundCurios);
        }

        tempEquippedItems.remove(player.getUUID());
        tempEquippedCurios.remove(player.getUUID());
    }

    private static CuriosSoulboundCompat.CurioEntry findMatchingCurio(ItemStack stack, List<CuriosSoulboundCompat.CurioEntry> equippedCurios) {
        Iterator<CuriosSoulboundCompat.CurioEntry> iterator = equippedCurios.iterator();
        while (iterator.hasNext()) {
            CuriosSoulboundCompat.CurioEntry entry = iterator.next();
            if (ItemStack.isSameItemSameTags(stack, entry.stack())) {
                iterator.remove();
                return new CuriosSoulboundCompat.CurioEntry(entry.identifier(), entry.index(), stack.copy());
            }
        }
        return null;
    }

    /**
     * Check if an ItemStack has the Soulbound affix applied to it.
     */
    private static boolean hasSoulboundAffix(ItemStack stack) {
        var affixes = AffixHelper.getAffixes(stack);
        // Check if any affix has the soulbound ID
        for (var affixHolder : affixes.keySet()) {
            if (SOULBOUND_ID.equals(affixHolder.getId())) {
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            CompoundTag originalData = event.getOriginal().getPersistentData();
            CompoundTag newData = event.getEntity().getPersistentData();

            if (originalData.contains(TAG_SOULBOUND)) {
                newData.put(TAG_SOULBOUND, originalData.get(TAG_SOULBOUND));
            }
            if (originalData.contains(TAG_EQUIPPED_ITEMS)) {
                newData.put(TAG_EQUIPPED_ITEMS, originalData.get(TAG_EQUIPPED_ITEMS));
            }
            if (originalData.contains(TAG_SOULBOUND_CURIOS)) {
                newData.put(TAG_SOULBOUND_CURIOS, originalData.get(TAG_SOULBOUND_CURIOS));
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        List<CuriosSoulboundCompat.CurioEntry> soulboundCurios = getSoulboundCurios(player);
        if (!soulboundCurios.isEmpty()) {
            boolean curiosLoaded = ModList.get().isLoaded(CURIOS_MODID);
            for (CuriosSoulboundCompat.CurioEntry entry : soulboundCurios) {
                boolean placed = curiosLoaded && CuriosSoulboundCompat.equipCurio(player, entry.identifier(), entry.index(), entry.stack());
                if (!placed) {
                    if (!player.getInventory().add(entry.stack())) {
                        player.drop(entry.stack(), false);
                    }
                }
            }
            player.getPersistentData().remove(TAG_SOULBOUND_CURIOS);
        }

        List<ItemStack> soulboundItems = getSoulboundItems(player);
        List<ItemStack> equippedItems = getEquippedItems(player);

        if (!soulboundItems.isEmpty()) {
            Inventory inv = player.getInventory();

            // don't delete this, inv.getItem(slotIndex) could be an exception and crash.
            if (inv.getContainerSize() < 41 || !(player instanceof ServerPlayer)) {
                for (ItemStack stack01 : soulboundItems) {
                    if (!inv.add(stack01)) {
                        player.drop(stack01, false);
                    }
                }
                clearSoulboundItems(player);
                return;
            }

            // Reverse iterate through soulbound items
            for (int t = soulboundItems.size() - 1; t >= 0; t--) {
                ItemStack stack = soulboundItems.get(t);
                boolean wasEquipped = false;

                // Check if this item was equipped
                for (ItemStack equippedItem : equippedItems) {
                    if (ItemStack.isSameItemSameTags(stack, equippedItem)) {
                        wasEquipped = true;
                        break;
                    }
                }

                boolean placed = false;

                // If it was equipped, try to equip it again
                if (wasEquipped) {
                    EquipmentSlot equipmentSlot = LivingEntity.getEquipmentSlotForItem(stack);
                    if (equipmentSlot != EquipmentSlot.MAINHAND) {
                        int slotIndex = getInventorySlotForEquipmentSlot(equipmentSlot);
                        if (slotIndex != -1 && inv.getItem(slotIndex).isEmpty()) {
                            inv.setItem(slotIndex, stack);
                            placed = true;
                        }
                    }
                }

                // If not placed in equipment slot, add to general inventory
                if (!placed) {
                    if (!inv.add(stack)) {
                        player.drop(stack, false);
                    }
                }
            }

            clearSoulboundItems(player);
        }
    }

    private static int getInventorySlotForEquipmentSlot(EquipmentSlot equipmentSlot) {
        return switch (equipmentSlot) {
            case HEAD -> 39;      // Helmet slot
            case CHEST -> 38;     // Chestplate slot
            case LEGS -> 37;      // Leggings slot
            case FEET -> 36;      // Boots slot
            case OFFHAND -> 40;   // Offhand slot
            case MAINHAND -> -1;  // Don't auto-place in mainhand
        };
    }

    private static void storeSoulboundItems(Player player, List<ItemStack> items, List<ItemStack> equippedItems, List<CuriosSoulboundCompat.CurioEntry> curios) {
        CompoundTag compound = player.getPersistentData();

        // Store soulbound items
        ListTag itemListTag = new ListTag();
        for (ItemStack stack : items) {
            CompoundTag itemTag = new CompoundTag();
            stack.save(itemTag);
            itemListTag.add(itemTag);
        }
        compound.put(TAG_SOULBOUND, itemListTag);

        // Store equipped items
        ListTag equippedListTag = new ListTag();
        for (ItemStack stack : equippedItems) {
            CompoundTag itemTag = new CompoundTag();
            stack.save(itemTag);
            equippedListTag.add(itemTag);
        }
        compound.put(TAG_EQUIPPED_ITEMS, equippedListTag);

        ListTag curiosListTag = new ListTag();
        for (CuriosSoulboundCompat.CurioEntry entry : curios) {
            CompoundTag curioTag = new CompoundTag();
            curioTag.putString("Identifier", entry.identifier());
            curioTag.putInt("Index", entry.index());
            CompoundTag itemTag = new CompoundTag();
            entry.stack().save(itemTag);
            curioTag.put("Item", itemTag);
            curiosListTag.add(curioTag);
        }
        compound.put(TAG_SOULBOUND_CURIOS, curiosListTag);
    }

    private static List<ItemStack> getSoulboundItems(Player player) {
        List<ItemStack> items = new ArrayList<>();
        CompoundTag compound = player.getPersistentData();

        if (compound.contains(TAG_SOULBOUND)) {
            ListTag listTag = compound.getList(TAG_SOULBOUND, 10);
            for (int i = 0; i < listTag.size(); i++) {
                CompoundTag itemTag = listTag.getCompound(i);
                ItemStack stack = ItemStack.of(itemTag);
                if (!stack.isEmpty()) {
                    items.add(stack);
                }
            }
        }

        return items;
    }

    private static List<ItemStack> getEquippedItems(Player player) {
        List<ItemStack> items = new ArrayList<>();
        CompoundTag compound = player.getPersistentData();

        if (compound.contains(TAG_EQUIPPED_ITEMS)) {
            ListTag listTag = compound.getList(TAG_EQUIPPED_ITEMS, 10);
            for (int i = 0; i < listTag.size(); i++) {
                CompoundTag itemTag = listTag.getCompound(i);
                ItemStack stack = ItemStack.of(itemTag);
                if (!stack.isEmpty()) {
                    items.add(stack);
                }
            }
        }

        return items;
    }

    private static List<CuriosSoulboundCompat.CurioEntry> getSoulboundCurios(Player player) {
        List<CuriosSoulboundCompat.CurioEntry> result = new ArrayList<>();
        CompoundTag compound = player.getPersistentData();

        if (compound.contains(TAG_SOULBOUND_CURIOS)) {
            ListTag listTag = compound.getList(TAG_SOULBOUND_CURIOS, 10);
            for (int i = 0; i < listTag.size(); i++) {
                CompoundTag curioTag = listTag.getCompound(i);
                String identifier = curioTag.getString("Identifier");
                int index = curioTag.getInt("Index");
                ItemStack stack = ItemStack.of(curioTag.getCompound("Item"));
                if (!stack.isEmpty()) {
                    result.add(new CuriosSoulboundCompat.CurioEntry(identifier, index, stack));
                }
            }
        }

        return result;
    }

    private static void clearSoulboundItems(Player player) {
        player.getPersistentData().remove(TAG_SOULBOUND);
        player.getPersistentData().remove(TAG_EQUIPPED_ITEMS);
        player.getPersistentData().remove(TAG_SOULBOUND_CURIOS);
    }
}