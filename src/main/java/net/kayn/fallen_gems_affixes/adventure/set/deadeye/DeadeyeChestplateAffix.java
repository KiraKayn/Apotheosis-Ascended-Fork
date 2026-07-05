package net.kayn.fallen_gems_affixes.adventure.set.deadeye;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.kayn.fallen_gems_affixes.adventure.set.SetAffix;
import net.kayn.fallen_gems_affixes.adventure.set.deadeye.bonus.DeadeyeSetBonusHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class DeadeyeChestplateAffix extends SetAffix {
    public static final Codec<DeadeyeChestplateAffix> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ResourceLocation.CODEC.fieldOf("set").forGetter(a -> a.setId),
            Codec.INT.fieldOf("max_focus").forGetter(a -> a.maxFocus),
            Codec.INT.fieldOf("focus_per_hit").forGetter(a -> a.focusPerHit),
            Codec.FLOAT.fieldOf("big_hit_health_threshold").forGetter(a -> a.bigHitHealthThreshold),
            Codec.INT.fieldOf("big_hit_bonus_focus").forGetter(a -> a.bigHitBonusFocus),
            Codec.FLOAT.fieldOf("arrow_damage_per_focus").forGetter(a -> a.arrowDamagePerFocus),
            Codec.INT.fieldOf("focus_decay_delay_ticks").forGetter(a -> a.focusDecayDelayTicks),
            Codec.FLOAT.fieldOf("two_piece_arrow_damage_per_focus").forGetter(a -> a.twoPieceArrowDamagePerFocus),
            Codec.FLOAT.fieldOf("two_piece_arrow_velocity_per_focus").forGetter(a -> a.twoPieceArrowVelocityPerFocus)
    ).apply(inst, DeadeyeChestplateAffix::new));

    private final int maxFocus;
    private final int focusPerHit;
    private final float bigHitHealthThreshold;
    private final int bigHitBonusFocus;
    private final float arrowDamagePerFocus;
    private final int focusDecayDelayTicks;
    private final float twoPieceArrowDamagePerFocus;
    private final float twoPieceArrowVelocityPerFocus;

    public DeadeyeChestplateAffix(ResourceLocation setId, int maxFocus, int focusPerHit, float bigHitHealthThreshold, int bigHitBonusFocus,
                                  float arrowDamagePerFocus, int focusDecayDelayTicks,
                                  float twoPieceArrowDamagePerFocus, float twoPieceArrowVelocityPerFocus) {
        super(setId);
        this.maxFocus = maxFocus;
        this.focusPerHit = focusPerHit;
        this.bigHitHealthThreshold = bigHitHealthThreshold;
        this.bigHitBonusFocus = bigHitBonusFocus;
        this.arrowDamagePerFocus = arrowDamagePerFocus;
        this.focusDecayDelayTicks = focusDecayDelayTicks;
        this.twoPieceArrowDamagePerFocus = twoPieceArrowDamagePerFocus;
        this.twoPieceArrowVelocityPerFocus = twoPieceArrowVelocityPerFocus;
    }

    public int getMaxFocus() { return maxFocus; }
    public int getFocusPerHit() { return focusPerHit; }
    public float getBigHitHealthThreshold() { return bigHitHealthThreshold; }
    public int getBigHitBonusFocus() { return bigHitBonusFocus; }
    public float getArrowDamagePerFocus() { return arrowDamagePerFocus; }
    public int getFocusDecayDelayTicks() { return focusDecayDelayTicks; }
    public float getTwoPieceArrowDamagePerFocus() { return twoPieceArrowDamagePerFocus; }
    public float getTwoPieceArrowVelocityPerFocus() { return twoPieceArrowVelocityPerFocus; }

    @Override
    public ResourceLocation getTypeId() { return FallenGemsAffixes.id("deadeye_chestplate"); }

    @Override
    public MutableComponent getDescription(ItemStack stack, LootRarity rarity, float level) {
        Component focusVal = Component.literal(String.valueOf(focusPerHit)).withStyle(ChatFormatting.DARK_RED);
        Component dmgVal   = Component.literal(SetAffix.fmt(arrowDamagePerFocus * 100f) + "%").withStyle(ChatFormatting.DARK_RED);
        Component maxVal   = Component.literal(String.valueOf(maxFocus)).withStyle(ChatFormatting.DARK_RED);

        MutableComponent desc = Component.translatable("set_affix.fallen_gems_affixes.deadeye_chestplate.desc",
                focusVal, dmgVal, maxVal).withStyle(ChatFormatting.YELLOW);

        if (isShiftDown()) {
            Component thresholdVal = Component.literal(SetAffix.fmt(bigHitHealthThreshold * 100f) + "%").withStyle(ChatFormatting.DARK_RED);
            Component bonusVal     = Component.literal(String.valueOf(bigHitBonusFocus)).withStyle(ChatFormatting.DARK_RED);
            Component decayVal     = Component.literal(SetAffix.fmt(focusDecayDelayTicks / 20f) + "s").withStyle(ChatFormatting.DARK_RED);
            desc.append(Component.translatable("set_affix.fallen_gems_affixes.deadeye_chestplate.desc.shift",
                    thresholdVal, bonusVal, decayVal).withStyle(ChatFormatting.GRAY));
        }

        return desc;
    }

    @Override
    public Component getBonusDescription(int threshold) {
        if (threshold == 2) {
            Component dmgVal = Component.literal(SetAffix.fmt(twoPieceArrowDamagePerFocus * 100f) + "%").withStyle(ChatFormatting.DARK_RED);
            Component velVal = Component.literal(SetAffix.fmt(twoPieceArrowVelocityPerFocus * 100f) + "%").withStyle(ChatFormatting.DARK_RED);
            return Component.translatable("set_bonus.fallen_gems_affixes.deadeye.2", dmgVal, velVal);
        }
        return null;
    }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        return !cat.isNone() && cat == LootCategory.CHESTPLATE;
    }

    @Override
    public void applySetBonus(Player player, int pieceCount) { DeadeyeSetBonusHandler.onPieceCountChanged(player, pieceCount); }

    @Override
    public void removeSetBonus(Player player) { DeadeyeSetBonusHandler.onPieceCountChanged(player, 0); }

    @Override
    public int[] getBonusThresholds() { return DeadeyeSetConstants.BONUS_THRESHOLDS; }

    @Override
    public Codec<? extends SetAffix> getCodec() { return CODEC; }
}