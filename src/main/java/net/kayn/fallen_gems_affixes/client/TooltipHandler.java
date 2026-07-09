package net.kayn.fallen_gems_affixes.client;

import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.adventure.set.SetAffix;
import net.kayn.fallen_gems_affixes.adventure.set.SetAffixHelper;
import net.kayn.fallen_gems_affixes.adventure.set.SetAffixInstance;
import net.kayn.fallen_gems_affixes.adventure.set.SetAffixRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = "fallen_gems_affixes", value = Dist.CLIENT)
public class TooltipHandler {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (!SetAffixHelper.hasSetAffix(stack)) return;

        SetAffixInstance inst = SetAffixHelper.getSetAffixInstance(stack).orElse(null);
        if (inst == null || !inst.isValid()) return;

        List<Component> tooltip = event.getToolTip();

        MutableComponent setAffixLine = Component.translatable("text.apotheosis.dot_prefix", inst.getDescription())
                .withStyle(ChatFormatting.YELLOW);

        tooltip.removeIf(line -> line.getString().equals(setAffixLine.getString()));
        int insertPos = Math.min(1, tooltip.size());
        tooltip.add(insertPos, setAffixLine);

        ResourceLocation setId = inst.afx().getSetId();
        String setKey = setId.toString().replace(":", ".");
        int[] thresholds = inst.afx().getBonusThresholds();
        int maxPieces = thresholds.length > 0 ? thresholds[thresholds.length - 1] : 5;

        int pieceCount = 0;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ResourceLocation equippedSetId = SetAffixHelper.getSetId(player.getItemBySlot(slot));
                if (setId.equals(equippedSetId)) pieceCount++;
            }
        }
        final int finalPieceCount = pieceCount;

        tooltip.add(Component.empty());

        MutableComponent setHeader = Component.translatable("set." + setKey)
                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD)
                .append(Component.literal(" (" + finalPieceCount + "/" + maxPieces + ")")
                        .withStyle(ChatFormatting.GRAY));
        tooltip.add(setHeader);

        for (int threshold : thresholds) {
            boolean unlocked = finalPieceCount >= threshold;
            Component bonusDesc = resolveBonusDescription(setId, threshold, setKey);

            Component formattedDesc = unlocked
                    ? Component.empty().append(bonusDesc).withStyle(ChatFormatting.GRAY)
                    : forceStyle(bonusDesc, ChatFormatting.DARK_GRAY);

            MutableComponent icon = Component.literal(unlocked ? " ✔ " : " ✘ ")
                    .withStyle(unlocked ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY);
            MutableComponent tierNum = Component.literal("(" + threshold + ") ")
                    .withStyle(ChatFormatting.DARK_GRAY);
            MutableComponent tierLine = icon.append(tierNum).append(formattedDesc);
            tooltip.add(tierLine);
        }
    }

    private static Component resolveBonusDescription(ResourceLocation setId, int threshold, String setKey) {
        for (SetAffix affix : Fallen.Registries.SET_AFFIX_REGISTRY.getValues()) {
            if (!setId.equals(affix.getSetId())) continue;
            Component desc = affix.getBonusDescription(threshold);
            if (desc != null) return desc;
        }
        return Component.translatable("set_bonus." + setKey + "." + threshold);
    }

    private static Component forceStyle(Component comp, ChatFormatting format) {
        MutableComponent copy = comp.copy().withStyle(format);
        List<Component> siblings = copy.getSiblings();
        for (int i = 0; i < siblings.size(); i++) {
            siblings.set(i, forceStyle(siblings.get(i), format));
        }
        return copy;
    }
}