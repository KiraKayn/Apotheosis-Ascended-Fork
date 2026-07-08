package net.kayn.fallen_gems_affixes.item.augments;

import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import net.kayn.fallen_gems_affixes.attachment.augment.AugmentMeta;
import net.kayn.fallen_gems_affixes.registry.ModItems;
import net.kayn.fallen_gems_affixes.types.augment.IAugment;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static net.kayn.fallen_gems_affixes.Fallen.AugmentMisc.AUGMENT_ID_TAG;
import static net.kayn.fallen_gems_affixes.Fallen.Registries.AUGMENT_REGISTRY;

public class AugmentItem extends Item {

    public AugmentItem(Properties properties) {
        super(properties);
    }

    public static boolean is(ItemStack stack) {
        return stack.is(ModItems.AUGMENT_ITEM.get());
    }

    public static ItemStack createAugment(IAugment augment) {
        ItemStack stack = new ItemStack(ModItems.AUGMENT_ITEM.get());
        if (AUGMENT_REGISTRY.getMetaData(augment) != null) {
            CompoundTag tag = stack.getOrCreateTag();
            tag.putString(AUGMENT_ID_TAG, augment.getId().toString());
        }
        return stack;
    }

    public static ItemStack createAugment(ResourceLocation id) {
        IAugment aug = AUGMENT_REGISTRY.getAug(id);
        if (aug != null) {
            return createAugment(aug);
        }
        return new ItemStack(ModItems.AUGMENT_ITEM.get());
    }

    @SuppressWarnings("ConstantConditions")
    public static String getAugmentId(ItemStack stack) {
        if (stack.hasTag()) {
            return stack.getTag().getString(AUGMENT_ID_TAG);
        }
        return "";
    }

    public static AugmentMeta getAugmentData(ItemStack stack) {
        String augmentId = getAugmentId(stack);
        IAugment augment = AUGMENT_REGISTRY.getAug(augmentId);
        if (augment != null) {
            return AUGMENT_REGISTRY.getMetaData(augment);
        }
        return null;
    }

    public static AugmentMeta getAugmentData(ResourceLocation augmentId) {
        return AUGMENT_REGISTRY.getMetaData(augmentId);
    }

    public static boolean canApplyTo(ItemStack augmentStack, LootCategory category) {
        AugmentMeta data = getAugmentData(augmentStack);
        if (data == null) return false;
        return data.canApplyTo(category);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        AugmentMeta data = getAugmentData(stack);
        if (data != null) {
            Set<LootCategory> categories = data.getCategories();

            // When categories is empty it means "applies to all items".
            // Show every known LootCategory
            List<LootCategory> displayCategories;
            if (categories.isEmpty()) {
                displayCategories = new ArrayList<>(LootCategory.BY_ID.values());
                displayCategories.sort(Comparator.comparing(LootCategory::getName));
            } else {
                displayCategories = new ArrayList<>(categories);
            }

            tooltip.add(Component.literal(""));
            tooltip.add(Component.translatable("tooltip.fallen_gems_affixes.augment.categories")
                    .withStyle(ChatFormatting.GREEN));

            for (int i = 0; i < displayCategories.size(); i += 3) {
                int endIndex = Math.min(i + 3, displayCategories.size());
                List<String> lineCategories = new ArrayList<>();

                for (int j = i; j < endIndex; j++) {
                    lineCategories.add(Component.translatable(
                            displayCategories.get(j).getDescIdPlural()).getString());
                }

                tooltip.add(Component.literal("  • " + String.join(", ", lineCategories))
                        .withStyle(ChatFormatting.GREEN));
            }

            tooltip.add(Component.literal(""));
        }

        String id = getAugmentId(stack);
        IAugment augment = AUGMENT_REGISTRY.getAug(id);
        if (augment != null) {
            augment.appendItemTooltip(stack, level, tooltip, flag);

            tooltip.add(Component.literal(""));
            tooltip.add(Component.literal("Fabled").withStyle(ChatFormatting.DARK_RED));
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        ResourceLocation augmentId = ResourceLocation.tryParse(getAugmentId(stack));
        if (augmentId != null) {
            return Component
                    .translatable("item.fallen_gems_affixes.augment." + augmentId.getPath())
                    .withStyle(ChatFormatting.DARK_RED);
        }
        return super.getName(stack);
    }
}