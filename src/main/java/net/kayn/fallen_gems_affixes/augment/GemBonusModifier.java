package net.kayn.fallen_gems_affixes.augment;

import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.placebo.util.StepFunction;
import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.adventure.socket.CatalystSocketHelper;
import net.kayn.fallen_gems_affixes.attachment.augment.AugmentHelper;
import net.kayn.fallen_gems_affixes.attachment.augment.AugmentInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.rtxyd.fallen.lib.api.annotation.FallenInserter;
import net.rtxyd.fallen.lib.api.annotation.Options;
import net.rtxyd.fallen.lib.type.util.patch.IInserterContext;
import net.rtxyd.fallen.lib.util.ObjectModifierFactory;
import net.rtxyd.fallen.lib.util.patch.InserterType;
import net.rtxyd.fallen.lib.util.patch.PatchOption;

import java.util.Map;

/**
 * This class should manage the logic on both client and server, must be thread-safe.
 *
 * <p>Genesis priority: if the item carries a Genesis augment, its {@code gemPower} value
 * is used exclusively. No hasGems guard — Genesis always controls gem scaling once applied.
 * Only if Genesis is absent does it fall through to the GemPower augment.
 */
public class GemBonusModifier {
    private static ObjectModifierFactory FACTORY;
    private static final ThreadLocal<Boolean> isKeyValid = ThreadLocal.withInitial(() -> false);
    public static final ThreadLocal<ItemStack> currentSuspendedItemStack = ThreadLocal.withInitial(() -> ItemStack.EMPTY);
    public static final ThreadLocal<Boolean> clientMarker = ThreadLocal.withInitial(() -> false);

    @FallenInserter(value = InserterType.STANDARD, options = @Options(PatchOption.REPLACE_RETURN))
    public static Object modifier(IInserterContext<Object, Object> ctx, Object... args) {
        if (args.length > 0 && args[0] instanceof LootRarity) {
            Object ret = ctx.ret();
            if (ret != null) {
                return modifyLPre(ctx.receiver(), ctx.ret());
            }
        }
        return ctx.ret();
    }

    public static void bootstrap(IEventBus eventBus) {
        if (FACTORY == null) {
            FACTORY = new ObjectModifierFactory(s -> {
                String name = s.toLowerCase();
                if (name.startsWith("cool") || name.endsWith("cool")) return true;
                return name.startsWith("cost") || name.endsWith("cost");
            }, 1);
        }
        eventBus.addListener(EventPriority.HIGHEST, GemBonusModifier::onTooltipEvent);
    }

    // since gem stats are mostly computed on serverside.
    // if there are some gem stats computed on clientside (visual things),
    // we need to improve this implementation at that time.
    public static void onTooltipEvent(ItemTooltipEvent event) {
        if (event.getEntity() != null) {
            clientMarker.set(event.getEntity().level().isClientSide);
        }
        suspendItemStack(event.getItemStack());
    }

    public static void suspendItemStack(ItemStack stack) {
        currentSuspendedItemStack.set(stack);
    }

    @Deprecated
    public static void keyCheck(Object obj) {
        isKeyValid.set(obj instanceof LootRarity);
    }

    public static boolean shouldNotModify() {
        return currentSuspendedItemStack.get() == ItemStack.EMPTY;
    }

    public static boolean clazzCheck(Class<?> clazz) {
        if (Number.class.isAssignableFrom(clazz)) {
            return true;
        }
        Class<?> host = clazz.getNestHost();
        return GemBonus.class.isAssignableFrom(host) && !FACTORY.isInBlackList(clazz);
    }

    public static Object modifyL(Object obj, float multiplied) {
        return FACTORY.copyAndModifyNumbers(obj, multiplied);
    }

    public static Object modifyLPre(Object rec, Object ret) {
        if (shouldNotModify()) return ret;
        ItemStack stack = currentSuspendedItemStack.get();
        float multiplied;
        if (ret instanceof StepFunction sf) {
            multiplied = getGemPower(stack);
            if (rec instanceof Map m) {
                switch(checkReverse(m)) {
                    case 1 -> {
                        return new StepFunction(sf.min() / multiplied, sf.steps(), sf.step());
                    }
                    case -1 -> {
                        return sf;
                    }
                }
            }
            return new StepFunction(sf.min() * multiplied, sf.steps(), sf.step() * multiplied);
        } else if (ret instanceof Number) {
            multiplied = getGemPower(stack);
            return ObjectModifierFactory.modifyNumber((Number) ret, multiplied);
        } else if (clazzCheck(ret.getClass())) {
            multiplied = getGemPower(stack);
            return modifyL(ret, multiplied);
        }
        return ret;
    }


    private static int checkReverse(Map<LootRarity, StepFunction> m) {
        Float last = null;
        LootRarity lastR = null;
        for (Map.Entry<LootRarity, StepFunction> e : m.entrySet()) {
            if (last == null) {
                last = e.getValue().min();
                lastR = e.getKey();
            } else {
                boolean check = e.getKey().ordinal() > lastR.ordinal();
                float current = e.getValue().min();
                if (current == last) return -1;
                if (current > last) {
                    if (check) {
                        return 0;
                    } else {
                        return 1;
                    }
                } else {
                    if (!check) {
                        return 0;
                    } else {
                        return 1;
                    }
                }
            }
        }
        return 0;
    }
    /**
     * Returns the gem-power multiplier for {@code stack}.
     *
     * <p>Priority:
     * <ol>
     *   <li><b>Genesis</b> — if present, its {@code gemPower} is returned unconditionally.</li>
     *   <li><b>GemPower augment</b> — classic single-value power.</li>
     *   <li><b>Default</b> — 1.0 (no change).</li>
     * </ol>
     */
    private static float getGemPower(ItemStack stack) {
        float currentGemPower = 1F;

        for (AugmentInstance instance : AugmentHelper.getAugments(stack).instances()) {
            if (instance.getData() instanceof IGemPowerProvider data) {
                currentGemPower += data.getGemPower();
            }
        }
//        if (Fallen.Common.GEM_POWER_CAPPED) {
//            return Math.min(currentGemPower * CatalystSocketHelper.getGemPowerMultiplier(stack), Fallen.Common.GEM_POWER_CAP);
//        }

        return currentGemPower * CatalystSocketHelper.getGemPowerMultiplier(stack);
    }
}