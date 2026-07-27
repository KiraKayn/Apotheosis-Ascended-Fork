package net.kayn.fallen_gems_affixes.augment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AttributeAffix;
import dev.shadowsoffire.apotheosis.adventure.affix.effect.DurableAffix;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.kayn.fallen_gems_affixes.attachment.augment.*;
import net.kayn.fallen_gems_affixes.item.augments.AugmentItem;
import net.kayn.fallen_gems_affixes.mixin.AttributeAffixAccessor;
import net.kayn.fallen_gems_affixes.types.augment.IAugment;
import net.kayn.fallen_gems_affixes.types.augment.IAugmentInnerData;
import net.kayn.fallen_gems_affixes.util.MiscUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.common.Mod;
import net.rtxyd.fallen.lib.runtime.forgemod.util.GameLifecycleHelper;
import net.rtxyd.fallen.lib.runtime.forgemod.util.eventkey.EventKeys;
import net.rtxyd.fallen.lib.util.IEither;
import net.rtxyd.fallen.lib.util.ins_attr.InsAttributeModifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class MaliceAugment implements IAugment {

    public static final ResourceLocation MALICE_ID =
            ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "malice");

    private static final String AFFIX_DATA  = "affix_data";
    private static final String AFFIXES_KEY = "affixes";

    private static final Codec<AugmentMeta> META_CODEC = AugmentMeta.codecCreate(MaliceData.CODEC);

    @Override public ResourceLocation getId()   { return MALICE_ID; }
    @Override public boolean isUnique()          { return true; }
    @Override public boolean shouldAttachToEntity()     { return false; }


    @Override
    public String toString() {
        return IAugment.string(this);
    }

    @Override
    public Codec<AugmentMeta> getMetaDataCodec() {
        return META_CODEC;
    }

    @Override
    public IAugmentInnerData fallbackInnerData() {
        MaliceData data = new MaliceData();
        data.affixDominant = true;
        data.powerBoost = 1.0f;
        data.powerNerf = -0.5f;
        data.revealed = false;
        return data;
    }

    @Override
    public boolean onAssemble(ItemStack result, ItemStack augmentItem, IAugment aug, Container cont, Level level) {
        result.getOrCreateTagElement("fga:malice_unrevealed");
        GameLifecycleHelper.submitCallback(EventKeys.SLOT_ON_TAKE, cont, (slot, player, stack) -> {
            if (stack.getTagElement("fga:malice_unrevealed") != null) {
                stack.removeTagKey("fga:malice_unrevealed");
                AugmentMeta meta = Fallen.Registries.AUGMENT_REGISTRY.getMetaData(Fallen.Augments.MALICE);
                MaliceData data = (MaliceData) meta.newDefaultData();
                data.revealed = true;
                data.affixDominant = player.level().getRandom().nextDouble() < 0.5;
                AugmentHelper.applyAugment(stack, new AugmentInstance(meta.getAugment(), data));
                return true;
            }
            return false;
        });
        return true;
    }

    @Override
    public boolean onApply(ItemStack item, Map<IAugment, AugmentInstance> mutableAugMap, AugmentInstance inst) {
        return true;
    }

    @Override
    public IAugmentInnerData deserializeInnerData(CompoundTag tag) {
        MaliceData data = new MaliceData();
        data.deserializeNBT(tag);
        return data;
    }

    @Override
    public void renderImage(@NotNull Font font, int x, int y, GuiGraphics gui,
                            IAugmentInnerData innerData) {
        gui.blit(IAugment.AUGMENT_ICON, x, y, 0, 0, 0, 9, 9, 9, 9);
        ItemStack displayStack = AugmentItem.createAugment(Fallen.Augments.MALICE);
        var pose = gui.pose();
        pose.pushPose();
        pose.translate(x, y, 0);
        pose.scale(0.5F, 0.5F, 1);
        gui.renderFakeItem(displayStack, 0, 0);
        pose.popPose();
    }


    @Override
    public MutableComponent organizeTooltipText(IAugmentInnerData innerData) {
        if (innerData instanceof MaliceData data && data.revealed) {
                return Component.translatable(
                        "fallen_gems_affixes.augment.malice.socket_desc",
                        MiscUtil.formatPercentage(data.getAffixPower()),
                        MiscUtil.formatPercentage(data.getGemPower())
                ).withStyle(ChatFormatting.YELLOW);
        }
        return Component.translatable("fallen_gems_affixes.augment.malice.socket_desc.hidden")
                .withStyle(ChatFormatting.GOLD);
    }

    @Override
    public void appendItemTooltip(ItemStack stack, @Nullable Level level,
                                  List<Component> tooltip, TooltipFlag flag) {
        AugmentMeta meta = AugmentItem.getAugmentData(stack);
        if (meta == null) return;
        MaliceData data = (MaliceData) meta.newDefaultData();
        tooltip.add(Component.translatable("fallen_gems_affixes.augment.malice.type")
                .withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("• ")
                .withStyle(ChatFormatting.YELLOW)
                .append(Component.translatable("fallen_gems_affixes.augment.malice.desc.chance")
                        .withStyle(ChatFormatting.YELLOW)));
        tooltip.add(Component.literal("  → ")
                .withStyle(ChatFormatting.YELLOW)
                .append(Component.translatable("fallen_gems_affixes.augment.malice.desc.option_affix",
                                MiscUtil.formatPercentage(data.getAffixPower()),
                                MiscUtil.formatPercentage(data.getGemPower()))
                        .withStyle(ChatFormatting.YELLOW)));
        tooltip.add(Component.literal("  → ")
                .withStyle(ChatFormatting.YELLOW)
                .append(Component.translatable("fallen_gems_affixes.augment.malice.desc.option_gem",
                                MiscUtil.formatPercentage(data.getAffixPower()),
                                MiscUtil.formatPercentage(data.getGemPower()))
                        .withStyle(ChatFormatting.YELLOW)));
    }

    public static class MaliceData implements IAugmentInnerData, IAffixPowerProvider, IGemPowerProvider {
        public static final Codec<MaliceData> CODEC = RecordCodecBuilder.create(inst ->
                inst.group(
                        Codec.FLOAT.fieldOf("power_boost").forGetter(d -> d.powerBoost),
                        Codec.FLOAT.fieldOf("power_nerf").forGetter(d -> d.powerNerf)
                ).apply(inst, (powerBoost, powerNerf) -> {
                    MaliceData data = (MaliceData) Fallen.Augments.MALICE.fallbackInnerData();
                    data.powerBoost = powerBoost;
                    data.powerNerf = powerNerf;
                    data.affixDominant = true;
                    data.revealed = false;
                    return data;
                })
        );

        boolean affixDominant = true;
        float   powerBoost    = 1.0f;
        float   powerNerf     = -0.5f;
        boolean revealed      = false;


        public static final String MODIFIER_NAME = "fallen_gems_affixes:malic_affix_power";
        @Override public void enable()              {}
        @Override public void disable()             {}
        @Override public boolean isFunctional()     { return true; }

        @Override
        public IAugmentInnerData copy() {
            var data = new MaliceData();
            data.affixDominant = affixDominant;
            data.powerBoost = powerBoost;
            data.powerNerf = powerNerf;
            data.revealed = revealed;
            return data;
        }

        public boolean isRevealed() {
            return revealed;
        }

        public boolean isAffixDominant() {
            return affixDominant;
        }

        @Override
        public float getAffixPower() {
            return affixDominant ? powerBoost : powerNerf;
        }

        @Override
        public boolean test(IEither<DynamicHolder<? extends Affix>, GemBonus> either) {
            return IAffixPowerProvider.super.test(either);
        }

        @Override
        public float getGemPower() {
            return affixDominant ? powerNerf : powerBoost;
        }

        @Override
        public InsAttributeModifier getModifier() {
            if (!revealed) {
                return InsAttributeModifier.EMPTY;
            }
            return new InsAttributeModifier(
                    InsAttributeModifier.Type.ADD_MULTIPLIED_FINAL,
                    MODIFIER_NAME,
                    getAffixPower());
        }

        @Override
        public InsAttributeModifier getModifierBy(IEither affix) {
            if (this.test(affix)) {
                return getModifier();
            }
            return InsAttributeModifier.EMPTY;
        }

        @Override
        public MutableComponent combineText() {
            return Component.translatable("fallen_gems_affixes.augment.malice.socket_desc",
                    MiscUtil.formatPercentage(getAffixPower()),
                    MiscUtil.formatPercentage(getGemPower()));
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putBoolean("affixDominant", affixDominant);
            tag.putFloat("affixPower",      getAffixPower());
            tag.putFloat("gemPower",        getGemPower());
            tag.putBoolean("revealed",      revealed);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            affixDominant = tag.getBoolean("affixDominant");
            float affixPower = tag.getFloat("affixPower");
            float gemPower = tag.getFloat("gemPower");
            powerBoost = affixDominant ? affixPower : gemPower;
            powerNerf = affixDominant ? gemPower : affixPower;
            revealed = tag.getBoolean("revealed");
        }

        @Override
        public Codec<? extends IAugmentInnerData> getCodec() {
            return CODEC;
        }
    }
}