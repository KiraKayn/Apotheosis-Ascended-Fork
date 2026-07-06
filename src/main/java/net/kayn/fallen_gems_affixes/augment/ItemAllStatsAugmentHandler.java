package net.kayn.fallen_gems_affixes.augment;

import dev.shadowsoffire.attributeslib.impl.BooleanAttribute;
import net.kayn.fallen_gems_affixes.attachment.augment.AugmentInstance;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.registries.ForgeRegistries;
import net.rtxyd.fallen.lib.runtime.forgemod.util.ItemAttributeModifierFactory;

import java.util.*;

public class ItemAllStatsAugmentHandler {
    private static final ItemAttributeModifierFactory FACTORY = new ItemAttributeModifierFactory("fga.multiply_existed");
    private static Set<Attribute> ignore = new HashSet<>(Set.of(Attributes.ATTACK_SPEED, ForgeMod.ENTITY_REACH.get()));
    public static void init() {
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOW, ItemAllStatsAugmentHandler::multiplyExisted);
        for (Attribute attribute : ForgeRegistries.ATTRIBUTES) {
            if (attribute instanceof BooleanAttribute) {
                ignore.add(attribute);
            }
        }
    }

    public static void multiplyExisted(ItemAttributeModifierEvent event) {
        var values = FACTORY.computeModifierValues(event);
        AugmentInstance inst = getAugmentInstance(event.getItemStack());
        if (inst == null) return;
        double factor = computeFactor(inst);
        for (ItemAttributeModifierFactory.ItemModifierValues value : values) {
            var attr = value.attr();
            if (ignore.contains(attr)) continue;
            var uuids = FACTORY.getGlobalUUIDs(attr);
            var names = FACTORY.getAttrNames(attr);
            if (value.addition() != 0) {
                addModifier(event, attr, uuids.addition(), names.addition(), AttributeModifier.Operation.ADDITION, value.addition(), factor);
            }
            if (value.multipliedBase() != 0) {
                addModifier(event, attr, uuids.multipliedBase(), names.multipliedBase(), AttributeModifier.Operation.MULTIPLY_BASE, value.multipliedBase(), factor);
            }
            if (value.multipliedTotal() != 0) {
                addModifier(event, attr, uuids.multipliedTotal(), names.multipliedTotal(), AttributeModifier.Operation.MULTIPLY_TOTAL, value.multipliedTotal(), factor);
            }
        }
    }

    public static AugmentInstance getAugmentInstance(ItemStack stack) {
        return null;
    }

    public static double computeFactor(AugmentInstance inst) {
        return 1;
    }

    private static void addModifier(ItemAttributeModifierEvent event, Attribute attribute, UUID uuid, String name, AttributeModifier.Operation op, double amount, double multipliedFinal) {
        event.addModifier(attribute, new AttributeModifier(uuid, name, amount * multipliedFinal, op));
    }
}
