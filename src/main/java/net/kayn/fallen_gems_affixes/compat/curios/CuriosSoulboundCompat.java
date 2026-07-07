package net.kayn.fallen_gems_affixes.compat.curios;

import net.kayn.fallen_gems_affixes.event.SoulboundEventHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.event.CurioDropsEvent;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.*;
import java.util.function.Predicate;

import static net.kayn.fallen_gems_affixes.event.SoulboundEventHandler.hasSoulboundAffix;

public class CuriosSoulboundCompat {
    private static final String TAG_SOULBOUND_CURIOS = "fallen_gems_affixes:soulbound_curios";
    private static final Map<UUID, List<CuriosSoulboundCompat.CurioEntry>> tempEquippedCurios = new HashMap<>();

    public static void init() {
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, CuriosSoulboundCompat::onLivingDeath);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, CuriosSoulboundCompat::curiosDropEvent);
        MinecraftForge.EVENT_BUS.addListener(CuriosSoulboundCompat::onPlayerClone);
        MinecraftForge.EVENT_BUS.addListener(CuriosSoulboundCompat::onPlayerRespawn);
    }

    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;
        tempEquippedCurios.put(player.getUUID(), CuriosSoulboundCompat.collectEquippedSoulboundCurios(player, SoulboundEventHandler::hasSoulboundAffix));
    }


    public static void curiosDropEvent(CurioDropsEvent event) {
        if (!(event.getEntity() instanceof Player p)) return;
        List<CuriosSoulboundCompat.CurioEntry> soulboundCurios = new ArrayList<>();
        List<CuriosSoulboundCompat.CurioEntry> equippedCurios = tempEquippedCurios.get(p.getUUID());
        event.getDrops().removeIf(itemEntity -> {
            ItemStack stack = itemEntity.getItem();
            if (hasSoulboundAffix(stack)) {
                for (int i = 0; i < equippedCurios.size(); i++) {
                    CuriosSoulboundCompat.CurioEntry entry = equippedCurios.get(i);
                    if (ItemStack.isSameItemSameTags(stack, entry.stack)) {
                        soulboundCurios.add(entry);
                        equippedCurios.remove(i);
                        break;
                    }
                }
                soulboundCurios.add(new CurioEntry("fga.unknown", 0, stack.copy()));
                return true;
            }
            return false;
        });
        if (!soulboundCurios.isEmpty()) {
            storeSoulboundCurios(p, soulboundCurios);
        }
        tempEquippedCurios.remove(p.getUUID());
    }

    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            CompoundTag originalData = event.getOriginal().getPersistentData();
            CompoundTag newData = event.getEntity().getPersistentData();
            if (originalData.contains(TAG_SOULBOUND_CURIOS)) {
                newData.put(TAG_SOULBOUND_CURIOS, originalData.get(TAG_SOULBOUND_CURIOS));
            }
        }
    }

    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        List<CuriosSoulboundCompat.CurioEntry> soulboundCurios = getSoulboundCurios(player);
        if (!soulboundCurios.isEmpty()) {
            for (CuriosSoulboundCompat.CurioEntry entry : soulboundCurios) {
                boolean placed = CuriosSoulboundCompat.equipCurio(player, entry.identifier(), entry.index(), entry.stack());
                if (!placed) {
                    if (!player.getInventory().add(entry.stack())) {
                        player.drop(entry.stack(), false);
                    }
                }
            }
            player.getPersistentData().remove(TAG_SOULBOUND_CURIOS);
        }
        clearSoulboundCurios(player);
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

    private static void storeSoulboundCurios(Player player, List<CuriosSoulboundCompat.CurioEntry> soulboundCurios) {
        CompoundTag compound = player.getPersistentData();
        ListTag curiosListTag = new ListTag();
        for (CuriosSoulboundCompat.CurioEntry entry : soulboundCurios) {
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

    public static List<CurioEntry> collectEquippedSoulboundCurios(Player player, Predicate<ItemStack> soulboundCheck) {
        List<CurioEntry> result = new ArrayList<>();

        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            for (Map.Entry<String, ICurioStacksHandler> entry : handler.getCurios().entrySet()) {
                IDynamicStackHandler stacks = entry.getValue().getStacks();
                for (int i = 0; i < stacks.getSlots(); i++) {
                    ItemStack stack = stacks.getStackInSlot(i);
                    if (!stack.isEmpty() && soulboundCheck.test(stack)) {
                        result.add(new CurioEntry(entry.getKey(), i, stack.copy()));
                    }
                }
            }
        });

        return result;
    }

    public static boolean equipCurio(Player player, String identifier, int index, ItemStack stack) {
        var handlerOpt = CuriosApi.getCuriosInventory(player);
        var stacksHandler = handlerOpt.map(handler -> handler.getCurios().get(identifier)).orElse(null);

        if (stacksHandler == null) {
            return false;
        }

        IDynamicStackHandler stacks = stacksHandler.getStacks();
        if (index >= stacks.getSlots() || !stacks.getStackInSlot(index).isEmpty()) {
            return false;
        }

        stacks.setStackInSlot(index, stack);
        return true;
    }

    public record CurioEntry(String identifier, int index, ItemStack stack) {
    }

    private static void clearSoulboundCurios(Player player) {
        player.getPersistentData().remove(TAG_SOULBOUND_CURIOS);
    }
}