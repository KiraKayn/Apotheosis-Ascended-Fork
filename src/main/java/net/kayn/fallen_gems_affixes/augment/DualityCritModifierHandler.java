package net.kayn.fallen_gems_affixes.augment;

import dev.shadowsoffire.attributeslib.api.ALObjects;
import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.attachment.augment.AugmentHelper;
import net.kayn.fallen_gems_affixes.attachment.augment.AugmentInstance;
import net.kayn.fallen_gems_affixes.registry.ModItems;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.UUID;

public class DualityCritModifierHandler {
    private static final UUID DUALITY_CRIT_BONUS_UUID = UUID.fromString("518354e7-2959-48b3-b809-f7d66f844a21");
    private static final UUID DUALITY_CRIT_REDUCTION_UUID = UUID.fromString("518354e7-2959-48b3-b809-f7d66f844a22");
    @SubscribeEvent(priority = EventPriority.LOW)
    public void attributeModifier(ItemAttributeModifierEvent e) {
        ItemStack stack = e.getItemStack();
        if (e.getSlotType() != EquipmentSlot.MAINHAND) return;
// Fix augment item triggering augment effects when holding it
        if (stack.is(ModItems.AUGMENT_ITEM.get())) return;
        AugmentInstance inst = AugmentHelper.getAugments(stack).get(Fallen.Augments.DUALITY);
        if (inst != null) {
            double value = 0f;
            for (var am : e.getModifiers().get(ALObjects.Attributes.CRIT_CHANCE.get())) {
                if (am.getOperation() == AttributeModifier.Operation.ADDITION) {
                    value += am.getAmount();
                }
            }
            DualityAugment.DualityData data = (DualityAugment.DualityData) inst.getData();
            float critChanceMultiplier = data.critChanceMultiplier;
            float critDamageReduction = data.critDamageReduction;

            e.addModifier(ALObjects.Attributes.CRIT_DAMAGE.get(), new AttributeModifier(DUALITY_CRIT_REDUCTION_UUID, "dualityCritReduction", -critDamageReduction, AttributeModifier.Operation.ADDITION));
            e.addModifier(ALObjects.Attributes.CRIT_CHANCE.get(), new AttributeModifier(DUALITY_CRIT_BONUS_UUID, "dualityCritBonus", value * Math.max(critChanceMultiplier - 1f, 0f), AttributeModifier.Operation.ADDITION));
        }
    }
}
