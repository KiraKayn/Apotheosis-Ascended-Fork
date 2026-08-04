package net.kayn.fallen_gems_affixes.client.tooltip;

import com.mojang.datafixers.util.Either;
import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.attachment.augment.AugmentSlotHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.rtxyd.fallen.lib.runtime.forgemod.util.GameLifecycleHelper;

import java.util.List;
import java.util.Optional;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ApothTooltipTweaks {
    @SubscribeEvent(priority = EventPriority.LOW, receiveCanceled = true)
    // JEI go RenderTooltipEvent.Pre but no GatherComponents
    public static void onGatherTooltip(RenderTooltipEvent.GatherComponents event) {
        ItemStack stack = event.getItemStack();
        stack.getOrCreateTag().putBoolean(Fallen.ContextKeys.RENDER_APOTH_TOOLTIP_ITEM.getId(), true);
        GameLifecycleHelper.submitContextCall(Fallen.ContextKeys.RENDER_APOTH_TOOLTIP_ITEM, () -> stack);
    }
}
