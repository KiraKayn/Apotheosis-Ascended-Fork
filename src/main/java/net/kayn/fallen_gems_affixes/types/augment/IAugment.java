package net.kayn.fallen_gems_affixes.types.augment;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.attachment.augment.AugmentInstance;
import net.kayn.fallen_gems_affixes.attachment.augment.AugmentMeta;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public interface IAugment {
    static String string(IAugment augment) {
        return "Augment{id=" + augment.getId() + "}";
    }

    static final Codec<Supplier<IAugment>> CODEC = new Codec<Supplier<IAugment>>() {
        @Override
        public <T> DataResult<Pair<Supplier<IAugment>, T>> decode(DynamicOps<T> ops, T input) {
            ResourceLocation id = ResourceLocation.CODEC.decode(ops, input).getOrThrow(false, t -> {}).getFirst();
            Supplier<IAugment> augment = () -> Fallen.Registries.AUGMENT_REGISTRY.getAug(id);
            return DataResult.success(new Pair<>(augment, input));
        }

        @Override
        public <T> DataResult<T> encode(Supplier<IAugment> input, DynamicOps<T> ops, T prefix) {
            return ResourceLocation.CODEC.encode(input.get().getId(), ops, prefix);
        }
    };

    ResourceLocation AUGMENT_ICON = ResourceLocation.fromNamespaceAndPath("fallen_gems_affixes", "textures/gui/augment_socket.png");

    Component TEXT = Component.translatable("fallen_gems_affixes.augment_slot");

    // Every augment must have a unique ID.
    // Used for serialization
    ResourceLocation getId();

    default IAugmentInnerData parseEntityAugment(CompoundTag augmentData) {
        if (!shouldAttachToEntity()) {
            return null;
        }
        return IAugmentInnerData.EMPTY;
    }

    boolean isUnique();

    boolean shouldAttachToEntity();

    void renderImage(@NotNull Font font, int x, int y, GuiGraphics gui, IAugmentInnerData innerData);

    default MutableComponent organizeTooltipText(IAugmentInnerData innerData) {
        return Component.translatable(this.getId().toString());
    }

    default void appendItemTooltip(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
    }

    default String getDescString() {
        return this.getId().getNamespace() + ".augment." + this.getId().getPath() + ".desc";
    }

    IAugmentInnerData deserializeInnerData(CompoundTag tag);

    Codec<AugmentMeta> getMetaDataCodec();

    IAugmentInnerData fallbackInnerData();

    default boolean onApply(ItemStack item, Map<IAugment, AugmentInstance> mutableAugMap, AugmentInstance inst) {
        return true;
    }

    default boolean onAssemble(ItemStack result, ItemStack augmentItem, IAugment aug, Container cont, Level level) {
        return true;
    }
}