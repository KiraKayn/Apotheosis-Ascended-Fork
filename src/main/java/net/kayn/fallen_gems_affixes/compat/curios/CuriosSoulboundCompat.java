package net.kayn.fallen_gems_affixes.compat.curios;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class CuriosSoulboundCompat {

    public static List<CurioEntry> removeSoulboundCurios(Player player, Predicate<ItemStack> soulboundCheck) {
        List<CurioEntry> result = new ArrayList<>();

        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            for (Map.Entry<String, ICurioStacksHandler> entry : handler.getCurios().entrySet()) {
                IDynamicStackHandler stacks = entry.getValue().getStacks();
                for (int i = 0; i < stacks.getSlots(); i++) {
                    ItemStack stack = stacks.getStackInSlot(i);
                    if (!stack.isEmpty() && soulboundCheck.test(stack)) {
                        result.add(new CurioEntry(entry.getKey(), i, stack.copy()));
                        stacks.setStackInSlot(i, ItemStack.EMPTY);
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
}