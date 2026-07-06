package net.kayn.fallen_gems_affixes.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class SigilOfTransmutationItem extends Item {

    public SigilOfTransmutationItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.fallen_gems_affixes.sigil_of_transmutation.desc")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.fallen_gems_affixes.sigil_of_transmutation.desc.base")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.fallen_gems_affixes.sigil_of_transmutation.desc.source")
                .withStyle(ChatFormatting.GRAY));
    }
}