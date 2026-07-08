package net.kayn.fallen_gems_affixes.client.tooltip;

import com.mojang.datafixers.util.Either;
import dev.shadowsoffire.attributeslib.api.client.AddAttributeTooltipsEvent;
import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.attachment.augment.AugmentSlotHelper;
import net.kayn.fallen_gems_affixes.types.augment.IAugment;
import net.kayn.fallen_gems_affixes.types.augment.IAugmentInnerData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.ListIterator;
import java.util.Optional;

import static net.kayn.fallen_gems_affixes.Fallen.AugmentMisc.*;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class AugmentTooltipHandler {

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void tooltips(AddAttributeTooltipsEvent e) {
        ItemStack stack = e.getStack();
        ListIterator<Component> it = e.getAttributeTooltipIterator();
        int augmentSlots = AugmentSlotHelper.getAugmentSlots(stack);
        if (augmentSlots > 0) it.add(Component.literal("FALLEN_AUGMENT_MARKER"));
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    @SuppressWarnings("ConstantConditions")
    public static void onGatherTooltip(RenderTooltipEvent.GatherComponents event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;

        int augmentSlots = AugmentSlotHelper.getAugmentSlots(stack);
        if (augmentSlots <= 0) return;
        // apoth tooltip handle
        List<Either<FormattedText, TooltipComponent>> list = event.getTooltipElements();
        int remove = -1;
        for (int i = 0; i < list.size(); i++) {
            Optional<FormattedText> o = list.get(i).left();
            if (o.isPresent() && o.get() instanceof Component comp && comp.getContents() instanceof LiteralContents tc) {
                if ("FALLEN_AUGMENT_MARKER".equals(tc.text())) {
                    remove = i;
                    list.remove(i);
                    break;
                }
            }
        }
        if (remove == -1) return;

        ListTag listTag = new ListTag();
        if (stack.hasTag() && stack.getTag().contains(Fallen.AugmentMisc.AUGMENT_DATA)) {
                CompoundTag augmentData = stack.getTag().getCompound(Fallen.AugmentMisc.AUGMENT_DATA);
                listTag = augmentData.getList(AUGMENTS, Tag.TAG_COMPOUND);
        }

        int filled = listTag.size();
        int empty = Math.max(0, augmentSlots - filled);

        for (int i = 0; i < empty; i++) {
            event.getTooltipElements().add(remove, Either.right(new AugmentTooltipComponent(null, null)));
        }

        for (int i = 0; i < filled; i++) {
            CompoundTag tag = listTag.getCompound(i);

            ResourceLocation typeId = ResourceLocation.tryParse(tag.getString(TYPE));
            IAugment augment = Fallen.Registries.AUGMENT_REGISTRY.getAug(typeId);

            if (augment != null) {
                IAugmentInnerData inner = augment.deserializeInnerData(tag.getCompound(INNER_DATA));
                inner.stackContext(stack);
                event.getTooltipElements().add(remove, Either.right(new AugmentTooltipComponent(augment, inner)));
            } else {
                event.getTooltipElements().add(remove, Either.right(new AugmentTooltipComponent(null, null)));
            }
        }
    }
}