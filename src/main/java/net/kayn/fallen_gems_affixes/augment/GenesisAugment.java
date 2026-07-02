package net.kayn.fallen_gems_affixes.augment;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.affix.effect.DurableAffix;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.kayn.fallen_gems_affixes.attachment.augment.AugmentInstance;
import net.kayn.fallen_gems_affixes.attachment.augment.AugmentMeta;
import net.kayn.fallen_gems_affixes.attachment.augment.AugmentRecipe;
import net.kayn.fallen_gems_affixes.item.augments.AugmentItem;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.rtxyd.fallen.lib.util.IEither;
import net.rtxyd.fallen.lib.util.ins_attr.InsAttributeModifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Augment of Genesis.
 *
 * <p>On application: snapshots the raw affix levels from {@code affix_data.affixes} NBT,
 * then multiplies every non-durable affix by {@code defaultPower}
 * (e.g. 0.5 makes a 0.9 affix become 0.45).
 *
 * <p>On each unique boss kill: {@code affixPower} and {@code gemPower} grow by their
 * respective boost values. Affixes are re-applied as {@code originalLevel * affixPower}.
 * Gem power is read passively by {@link GemBonusModifier}.
 *
 * <p>Affix NBT is manipulated directly to avoid key-format mismatches that occur when
 * going through {@link AffixHelper#getAffixes} during recipe assembly.
 */
public class GenesisAugment implements IAugment {

    private static final ResourceLocation GENESIS_ID =
            ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "genesis");
    private static final Codec<AugmentMeta> META_CODEC = AugmentMeta.codecCreate(GenesisData.CODEC);

    /** NBT key inside affix_data where affix levels live. */
    private static final String AFFIX_DATA   = "affix_data";
    public static final String AFFIXES_KEY  = "affixes";

    public static final float MAX_AFFIX_LEVEL = SupremacyAugment.MAX_AFFIX_LEVEL;


    @Override public ResourceLocation getId()   { return GENESIS_ID; }
    @Override public boolean isUnique()          { return true; }
    @Override public boolean shouldAttachToEntity()     { return false; }

    @Override
    public Codec<AugmentMeta> getMetaDataCodec() {
        return META_CODEC;
    }

    @Override
    public IAugmentInnerData fallbackInnerData() {
        GenesisData data = new GenesisData();
        data.defaultPower    = -0.5f;
        data.affixPowerBoost = 0.1f;
        data.gemPowerBoost   = 0.1f;
        data.affixPower      = -0.5f;
        data.gemPower        = -0.5f;
        data.bossKillCount   = 0;
        return data;
    }

    @Override
    public String toString() {
        return IAugment.string(this);
    }

    @Override
    public IAugmentInnerData deserializeInnerData(CompoundTag tag) {
        GenesisData data = new GenesisData();
        data.deserializeNBT(tag);
        return data;
    }

    @Override
    public void renderImage(@NotNull Font font, int x, int y, GuiGraphics gui,
                            IAugmentInnerData innerData) {
        gui.blit(IAugment.AUGMENT_ICON, x, y, 0, 0, 0, 9, 9, 9, 9);
        ItemStack displayStack = AugmentItem.createAugment(Fallen.Augments.GENESIS);
        PoseStack pose = gui.pose();
        pose.pushPose();
        pose.translate(x, y, 0);
        pose.scale(0.5F, 0.5F, 1);
        gui.renderFakeItem(displayStack, 0, 0);
        pose.popPose();
    }

    @Override
    public MutableComponent organizeTooltipText(IAugmentInnerData innerData) {
        if (innerData instanceof GenesisData data) {
            return data.combineText().withStyle(ChatFormatting.YELLOW);
        }
        return Component.empty();
    }

    @Override
    public void appendItemTooltip(ItemStack stack, @Nullable Level level,
                                  java.util.List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("fallen_gems_affixes.augment.genesis.type")
                .withStyle(ChatFormatting.GOLD));

        AugmentMeta meta = AugmentItem.getAugmentData(stack);
        float defaultPower;
        float affixBoost;
        float gemBoost;
        GenesisData data;
        if (meta == null) {
            data = (GenesisData) Fallen.Augments.GENESIS.fallbackInnerData();
        } else {
            data = (GenesisData) meta.newDefaultData();
        }
        defaultPower = data.defaultPower;
        affixBoost = data.affixPowerBoost;
        gemBoost = data.gemPowerBoost;

        tooltip.add(Component.literal("• ")
                .withStyle(ChatFormatting.YELLOW)
                .append(Component.translatable(
                                "fallen_gems_affixes.augment.genesis.desc.default",
                                MiscUtil.formatPercentage(defaultPower))
                        .withStyle(ChatFormatting.YELLOW)));
        tooltip.add(Component.literal("• ")
                .withStyle(ChatFormatting.YELLOW)
                .append(Component.translatable(
                                "fallen_gems_affixes.augment.genesis.desc.affix_boost",
                                MiscUtil.formatPercentage(affixBoost))
                        .withStyle(ChatFormatting.YELLOW)));
        tooltip.add(Component.literal("• ")
                .withStyle(ChatFormatting.YELLOW)
                .append(Component.translatable(
                                "fallen_gems_affixes.augment.genesis.desc.gem_boost",
                                MiscUtil.formatPercentage(gemBoost))
                        .withStyle(ChatFormatting.YELLOW)));
        tooltip.add(Component.literal("• ")
                .withStyle(ChatFormatting.YELLOW)
                .append(Component.translatable(
                                "fallen_gems_affixes.augment.genesis.desc.augmentation_block")
                        .withStyle(ChatFormatting.YELLOW)));
    }


    /**
     * Called from {@link AugmentRecipe} after genesis
     * NBT has been committed to {@code result}.
     *
     * <p>Reads affix levels directly from {@code affix_data.affixes} NBT (bypassing
     * AffixHelper which is unreliable during recipe assembly), stores them as the permanent
     * baseline, then immediately applies {@code defaultPower} as the starting multiplier.
     */
    @Override
    public boolean onApply(ItemStack stack, Map<IAugment, AugmentInstance> mutableAugMap, AugmentInstance inst) {
        return true;
    }

    public static class GenesisData implements IAugmentInnerData, IGemPowerProvider, IAffixPowerProvider {
        public static final Codec<GenesisData> CODEC = RecordCodecBuilder.create(inst ->
                inst.group(
                        Codec.FLOAT.fieldOf("default_power").forGetter(d -> d.defaultPower),
                        Codec.FLOAT.fieldOf("affix_power_boost").forGetter(d -> d.affixPowerBoost),
                        Codec.FLOAT.fieldOf("gem_power_boost").forGetter(d -> d.gemPowerBoost)
                ).apply(inst, (defaultPower, affixPowerBoost, gemPowerBoost) -> {
                    GenesisData data = (GenesisData) Fallen.Augments.GENESIS.fallbackInnerData();
                    data.defaultPower = defaultPower;
                    data.affixPowerBoost = affixPowerBoost;
                    data.gemPowerBoost = gemPowerBoost;
                    return data;
                })
        );

        float defaultPower    = -0.5f;
        float affixPowerBoost = 0.1f;
        float gemPowerBoost   = 0.1f;
        float affixPower      = -0.5f;
        float gemPower        = -0.5f;
        int   bossKillCount   = 0;
        final Set<String> killedBossIds     = new HashSet<>();

        public static final String MODIFIER_NAME = "fallen_gems_affixes:genesis_affix_power";
        @Override
        public float getAffixPower()    { return affixPower; }

        @Override
        public boolean test(IEither<DynamicHolder<? extends Affix>, GemBonus> either) {
            return IAffixPowerProvider.super.test(either);
        }

        @Override
        public InsAttributeModifier getModifier() {
            return new InsAttributeModifier(
                    InsAttributeModifier.Type.ADD_MULTIPLIED_BASE,
                    MODIFIER_NAME,
                    getAffixPower());
        }

        @Override
        public InsAttributeModifier getModifierBy(IEither<DynamicHolder<? extends Affix>, GemBonus> either) {
            if (this.test(either)) {
                return getModifier();
            }
            return InsAttributeModifier.EMPTY;
        }

        @Override
        public float getGemPower()      { return gemPower; }
        public int   getBossKillCount() { return bossKillCount; }

        @Override public void enable()  {}
        @Override public void disable() {}
        @Override public boolean isFunctional() { return true; }

        @Override
        public IAugmentInnerData copy() {
            var data = new GenesisData();
            data.bossKillCount = bossKillCount;
//            data.killedBossIds = killedBossIds;
            data.affixPowerBoost = affixPowerBoost;
            data.gemPower = gemPower;
            data.gemPowerBoost = gemPowerBoost;
            data.affixPowerBoost = affixPowerBoost;
            data.affixPower = affixPower;
            return data;
        }

        @Override
        public MutableComponent combineText() {
            return Component.translatable(
                    "fallen_gems_affixes.augment.genesis.socket_desc",
                    killedBossIds.size(),
                    MiscUtil.formatPercentage(affixPower),
                    MiscUtil.formatPercentage(gemPower));
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putFloat("defaultPower",    defaultPower);
            tag.putFloat("affixPowerBoost", affixPowerBoost);
            tag.putFloat("gemPowerBoost",   gemPowerBoost);
            tag.putFloat("affixPower",      affixPower);
            tag.putFloat("gemPower",        gemPower);
            tag.putInt("bossKillCount",     bossKillCount);
            //killedBossIds now stores dedup keys: "apoth:epic", "universal:mythic", etc
            net.minecraft.nbt.ListTag bosslist = new net.minecraft.nbt.ListTag();
            for (String id : killedBossIds) {
                bosslist.add(net.minecraft.nbt.StringTag.valueOf(id));
            }
            tag.put("killedBosses", bosslist);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            defaultPower    = tag.getFloat("defaultPower");
            affixPowerBoost = tag.getFloat("affixPowerBoost");
            gemPowerBoost   = tag.getFloat("gemPowerBoost");
            affixPower      = tag.getFloat("affixPower");
            gemPower        = tag.getFloat("gemPower");
            bossKillCount   = tag.getInt("bossKillCount");
            killedBossIds.clear();
            for (net.minecraft.nbt.Tag t : tag.getList("killedBosses", net.minecraft.nbt.Tag.TAG_STRING)) {
                killedBossIds.add(t.getAsString());
            }
        }

        @Override
        public Codec<GenesisData> getCodec() {
            return CODEC;
        }
    }
}