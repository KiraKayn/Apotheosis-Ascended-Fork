
package net.kayn.fallen_gems_affixes.client;

import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.adventure.affix.AttributeAffix;
import dev.shadowsoffire.attributeslib.AttributesLib;
import dev.shadowsoffire.attributeslib.api.IFormattableAttribute;
import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.kayn.fallen_gems_affixes.attachment.augment.AugmentHelper;
import net.kayn.fallen_gems_affixes.augment.SupremacyAugment;
import net.kayn.fallen_gems_affixes.mixin.AttributeAffixAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

@Mod.EventBusSubscriber(modid = FallenGemsAffixes.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class SupremacyStarTooltip {

    /**
     * Pre-compute the exact tooltip of an {@link AttributeAffix} to compare.
     * @param stack the item stack where the inst is
     * @param inst the inst get from the stack
     * @return the exact tooltip component in rendering tooltips
     */
    public static Component attributeToComponent(ItemStack stack, AffixInstance inst) {
        if (!(inst.affix().get() instanceof AttributeAffix affix)) {
            return null;
        }
        AttributeAffix.ModifierInst mi = ((AttributeAffixAccessor) affix).getModifiers().get(inst.rarity().get());
        AttributeModifier modifier = mi.build(stack, affix.getId(), inst.level());
        Component comp = IFormattableAttribute.toComponent(mi.attr(), modifier, AttributesLib.getTooltipFlag());
        return comp;
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (!AugmentHelper.hasAugment(stack, Fallen.Augments.SUPREMACY)) return;

        List<Component> tooltips = event.getToolTip();
        if (tooltips == null || tooltips.isEmpty()) return;
        // affixes stars
        List<Component> components = new ArrayList<>();
        if (AffixHelper.hasAffixes(stack)) {
            AffixHelper.streamAffixes(stack)
                    .sorted(Comparator.comparingInt(a -> a.affix().get().getType().ordinal()))
                    .forEach(inst -> {
                        Component desc = inst.getDescription();
                        if (desc.getContents() != ComponentContents.EMPTY) {
                            if (inst.level() > Fallen.Common.STANDARD_MAX_LEVEL) {
                                components.add(Component.empty());
                                components.add(Component.translatable("text.apotheosis.dot_prefix", desc).withStyle(ChatFormatting.YELLOW));
                                components.add(starPrefix(desc).withStyle(ChatFormatting.YELLOW));
                            }
                        }
                    });
        }

        if (!components.isEmpty()) {
            int size = components.size();
            int sizeTp = tooltips.size();
            for (int i = 0, j = 0; i < size && j < sizeTp; j++) {
                if (components.get(i).getContents() == ComponentContents.EMPTY) {
                    if (tooltips.get(j).equals(components.get(i + 1))) {
                        tooltips.remove(j);
                        tooltips.add(j, components.get(i + 2));
                        i += 3;
                    }
                }
            }
        }
        Set<Component> special = new HashSet<>();
        // following code is referenced from Apotheosis 8.4.1 by Shadows, with its original comments.

        // We want attribute modifiers that are being supplied by over-max affixes to reflect that in the tooltip.
        // However, there's not really any way to know which attribute modifiers are from affixes.
        // So to fix that, we have to ask all over-max affixes for their modifier tooltips, and search for them in the tooltip.
        // If we find them, we add a star prefix to them.
        AffixHelper.streamAffixes(stack)
                .filter(inst -> inst.level() > Fallen.Common.STANDARD_MAX_LEVEL)
                .filter(inst -> inst.affix().get() instanceof AttributeAffix)
                .forEach(inst -> {
                    Component comp = attributeToComponent(stack, inst);
                    if (comp != null) {
                        special.add(comp);
                    }
                });

        Component listHeader = Component.literal(" \u2507 ").withStyle(ChatFormatting.GRAY);

        if (!special.isEmpty()) {
            for (int i = 0; i < tooltips.size(); i++) {
                Component comp = tooltips.get(i);
                if (special.contains(comp)) {
                    tooltips.remove(i);
                    tooltips.add(i, starPrefix(comp).withStyle(comp.getStyle()));
                }
                // Try to find tooltips nested in a list header to apply the star to support merged tooltips.
                else if (comp.getContents().equals(listHeader.getContents()) && comp.getSiblings().size() == 1) {
                    Component child = comp.getSiblings().get(0);
                    if (special.contains(child)) {
                        tooltips.remove(i);
                        MutableComponent replacement = listHeader.copy();
                        replacement.append(starPrefix(child).withStyle(child.getStyle()));
                        for (int j = 1; j < comp.getSiblings().size(); j++) {
                            replacement.append(comp.getSiblings().get(j));
                        }
                        tooltips.add(i, replacement);
                    }
                }
            }
        }
    }

    private static MutableComponent starPrefix(Component desc) {
        return Component.translatable("text.fallen_gems_affixes.star_prefix", desc);
    }
}