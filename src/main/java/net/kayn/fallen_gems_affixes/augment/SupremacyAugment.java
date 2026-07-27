package net.kayn.fallen_gems_affixes.augment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.kayn.fallen_gems_affixes.attachment.augment.*;
import net.kayn.fallen_gems_affixes.item.augments.AugmentItem;
import net.kayn.fallen_gems_affixes.types.augment.IAugment;
import net.kayn.fallen_gems_affixes.types.augment.IAugmentInnerData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.rtxyd.fallen.lib.util.IEither;
import net.rtxyd.fallen.lib.util.ins_attr.InsAttributeModifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SupremacyAugment implements IAugment {
    private static final ResourceLocation SUPREMACY_ID = ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "supremacy");
    private static final Codec<AugmentMeta> META_CODEC = AugmentMeta.codecCreate(SupremacyData.CODEC);

    @Override
    public ResourceLocation getId() {
        return SUPREMACY_ID;
    }

    @Override
    public Codec<AugmentMeta> getMetaDataCodec() {
        return META_CODEC;
    }

    @Override
    public IAugmentInnerData fallbackInnerData() {
        SupremacyData data = new SupremacyData();
        data.power = 1.5f;
        return data;
    }

    @Override
    public boolean isUnique() {
        return true;
    }

    @Override
    public boolean shouldAttachToEntity() {
        return false;
    }

    @Override
    public void renderImage(@NotNull Font font, int x, int y, GuiGraphics gui, IAugmentInnerData innerData) {
        gui.blit(IAugment.AUGMENT_ICON, x, y, 0, 0, 0, 9, 9, 9, 9);

        AugmentMeta data = AugmentItem.getAugmentData(SUPREMACY_ID);
        if (data == null) return;

        ItemStack stack = AugmentItem.createAugment(Fallen.Augments.SUPREMACY);

        var pose = gui.pose();
        pose.pushPose();
        pose.translate(x, y, 0);
        pose.scale(0.5F, 0.5F, 1);
        gui.renderFakeItem(stack, 0, 0);
        pose.popPose();
    }

    @Override
    public IAugmentInnerData deserializeInnerData(CompoundTag tag) {
        SupremacyData data = new SupremacyData();
        data.deserializeNBT(tag);

        return data;
    }

    @Override
    public MutableComponent organizeTooltipText(IAugmentInnerData innerData) {
        if (innerData instanceof SupremacyData data) {
            return Component.translatable(
                    "fallen_gems_affixes.augment.supremacy.desc",
                    data.getAffixPower()
            ).withStyle(ChatFormatting.YELLOW);
        }
        return Component.translatable("fallen_gems_affixes.augment.supremacy.desc")
                .withStyle(ChatFormatting.YELLOW);
    }

    @Override
    public void appendItemTooltip(ItemStack stack, @Nullable Level level, java.util.List<Component> tooltip, TooltipFlag flag) {
        float power = getAugmentPower(stack);

        tooltip.add(Component.translatable("fallen_gems_affixes.augment.supremacy.type")
                .withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("• ")
                .withStyle(ChatFormatting.YELLOW)
                .append(Component.translatable("fallen_gems_affixes.augment.supremacy.desc", power)
                        .withStyle(ChatFormatting.YELLOW)));
    }

    private static float getAugmentPower(ItemStack stack) {
        AugmentMeta data = AugmentItem.getAugmentData(stack);
        if (data != null) {
            return ((SupremacyData)data.newDefaultData()).power;
        }
        return 1.5f;
    }

    public static class SupremacyData implements IAugmentInnerData, IAffixPowerProvider {
        public static final Codec<SupremacyData> CODEC = RecordCodecBuilder.create(inst ->
                inst.group(
                        Codec.FLOAT.fieldOf("power").forGetter(d -> d.power)
                ).apply(inst, (power) -> {
                    SupremacyData data = (SupremacyData) Fallen.Augments.SUPREMACY.fallbackInnerData();
                    data.power = power;
                    return data;
                })
        );
        float power;

        public static final String MODIFIER_NAME = "fallen_gems_affixes:supremacy_affix_power";

        @Override
        public boolean test(IEither<DynamicHolder<? extends Affix>, GemBonus> either) {
            return IAffixPowerProvider.super.test(either);
        }

        @Override
        public InsAttributeModifier getModifier() {
            return new InsAttributeModifier(InsAttributeModifier.Type.SET_BASE, MODIFIER_NAME, power);
        }

        @Override
        public void enable() {
        }

        @Override
        public void disable() {
        }

        @Override
        public boolean isFunctional() {
            return true;
        }

        @Override
        public IAugmentInnerData copy() {
            var data = new SupremacyData();
            data.power = power;
            return data;
        }

        @Override
        public MutableComponent combineText() {
            return Component.translatable("fallen_gems_affixes.augment.supremacy.desc", power);
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putFloat("power", power);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            power = tag.getFloat("power");
        }

        @Override
        public float getAffixPower() {
            return power;
        }

        @Override
        public Codec<SupremacyData> getCodec() {
            return CODEC;
        }
    }
}