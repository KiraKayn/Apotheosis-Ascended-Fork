package net.kayn.fallen_gems_affixes;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Predicate;

public class AALootCategories {

    // Hold references reflectively to avoid a hard compile-time dependency on Apotheosis
    private static final Class<?> LOOT_CATEGORY_CLASS;
    private static final Method LOOT_CATEGORY_FOR_ITEM;
    private static final Object GUN;

    static {
        Class<?> lootClass = null;
        Method forItem = null;
        Object gun = null;

        try {
            lootClass = Class.forName("dev.shadowsoffire.apotheosis.adventure.loot.LootCategory");

            // find forItem(ItemStack) method
            for (Method m : lootClass.getMethods()) {
                if (m.getName().equals("forItem") && m.getParameterCount() == 1) {
                    forItem = m;
                    break;
                }
            }

            if (ModList.get().isLoaded("scguns") && lootClass != null) {
                // get SWORD constant
                Field swordField = lootClass.getField("SWORD");
                Object SWORD = swordField.get(null);

                // find a suitable register(...) method and invoke it reflectively
                Method registerMethod = null;
                for (Method m : lootClass.getMethods()) {
                    if (m.getName().equals("register")) {
                        // choose the first register method that accepts at least 2 params
                        if (m.getParameterCount() >= 2) {
                            registerMethod = m;
                            break;
                        }
                    }
                }

                if (registerMethod != null) {
                    // Build a Predicate via a lambda that uses reflection to check for the GunItem class
                    Predicate<Object> predicate = (obj) -> {
                        try {
                            if (!(obj instanceof ItemStack)) return false;
                            ItemStack stack = (ItemStack) obj;
                            Object item = stack.getItem();
                            Class<?> gunCls = Class.forName("top.ribs.scguns.item.GunItem");
                            return gunCls.isInstance(item);
                        } catch (Throwable t) {
                            return false;
                        }
                    };

                    // prepare EquipmentSlot[] argument
                    EquipmentSlot[] slots = arr(EquipmentSlot.MAINHAND);

                    // Attempt to invoke register. The exact signature varies between versions; try calling with 4 args if possible
                    Object registered = null;
                    try {
                        // try (baseCategory, name, predicate, slots)
                        registered = registerMethod.invoke(null, SWORD, "gun", predicate, slots);
                    } catch (IllegalArgumentException iae) {
                        // try (name, predicate, slots)
                        try {
                            registered = registerMethod.invoke(null, "gun", predicate, slots);
                        } catch (IllegalArgumentException iae2) {
                            // ignore, leave registered null
                        }
                    }

                    gun = registered;
                }
            }
        } catch (Throwable t) {
            // Apotheosis or scguns not available; leave reflective refs null
        }

        LOOT_CATEGORY_CLASS = lootClass;
        LOOT_CATEGORY_FOR_ITEM = forItem;
        GUN = gun;
    }

    private static EquipmentSlot[] arr(EquipmentSlot... s) {
        return s;
    }

    public static boolean isGun(ItemStack stack) {
        if (GUN == null || LOOT_CATEGORY_CLASS == null || LOOT_CATEGORY_FOR_ITEM == null) return false;
        try {
            Object category = LOOT_CATEGORY_FOR_ITEM.invoke(null, stack);
            return category != null && category.equals(GUN);
        } catch (Throwable t) {
            return false;
        }
    }

    // Force class loading/init
    public static void init() {}
}
