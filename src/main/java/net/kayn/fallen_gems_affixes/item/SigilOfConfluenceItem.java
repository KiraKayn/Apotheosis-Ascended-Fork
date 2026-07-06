package net.kayn.fallen_gems_affixes.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class SigilOfConfluenceItem extends Item {

    public SigilOfConfluenceItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.fallen_gems_affixes.sigil_of_confluence.desc")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.fallen_gems_affixes.sigil_of_confluence.desc2")
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("item.fallen_gems_affixes.sigil_of_confluence.desc_special")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}