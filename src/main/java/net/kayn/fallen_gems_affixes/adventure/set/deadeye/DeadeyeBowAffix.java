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

public class DeadeyeBowAffix extends SetAffix {
    public static final Codec<DeadeyeBowAffix> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ResourceLocation.CODEC.fieldOf("set").forGetter(a -> a.setId),
            Codec.FLOAT.fieldOf("execute_base_threshold").forGetter(a -> a.executeBaseThreshold),
            Codec.FLOAT.fieldOf("execute_scaling_per_focus").forGetter(a -> a.executeScalingPerFocus),
            Codec.FLOAT.fieldOf("execute_mark_radius").forGetter(a -> a.executeMarkRadius),
            Codec.INT.fieldOf("mark_duration_ticks").forGetter(a -> a.markDurationTicks),
            Codec.INT.fieldOf("execute_mark_refund").forGetter(a -> a.executeMarkRefund),
            Codec.FLOAT.fieldOf("three_piece_no_consume_chance").forGetter(a -> a.threePieceNoConsumeChance),
            Codec.INT.fieldOf("three_piece_execute_bonus_focus").forGetter(a -> a.threePieceExecuteBonusFocus),
            Codec.INT.fieldOf("echo_duration_ticks").forGetter(a -> a.echoDurationTicks),
            Codec.FLOAT.fieldOf("echo_damage_percent").forGetter(a -> a.echoDamagePercent),
            Codec.FLOAT.fieldOf("echo_aoe_radius").forGetter(a -> a.echoAoeRadius),
            Codec.INT.fieldOf("echo_focus_grant").forGetter(a -> a.echoFocusGrant)
    ).apply(inst, DeadeyeBowAffix::new));

    private final float executeBaseThreshold;
    private final float executeScalingPerFocus;
    private final float executeMarkRadius;
    private final int markDurationTicks;
    private final int executeMarkRefund;
    private final float threePieceNoConsumeChance;
    private final int threePieceExecuteBonusFocus;
    private final int echoDurationTicks;
    private final float echoDamagePercent;
    private final float echoAoeRadius;
    private final int echoFocusGrant;

    public DeadeyeBowAffix(ResourceLocation setId, float executeBaseThreshold, float executeScalingPerFocus,
                              float executeMarkRadius, int markDurationTicks, int executeMarkRefund,
                              float threePieceNoConsumeChance, int threePieceExecuteBonusFocus,
                              int echoDurationTicks, float echoDamagePercent, float echoAoeRadius, int echoFocusGrant) {
        super(setId);
        this.executeBaseThreshold = executeBaseThreshold;
        this.executeScalingPerFocus = executeScalingPerFocus;
        this.executeMarkRadius = executeMarkRadius;
        this.markDurationTicks = markDurationTicks;
        this.executeMarkRefund = executeMarkRefund;
        this.threePieceNoConsumeChance = threePieceNoConsumeChance;
        this.threePieceExecuteBonusFocus = threePieceExecuteBonusFocus;
        this.echoDurationTicks = echoDurationTicks;
        this.echoDamagePercent = echoDamagePercent;
        this.echoAoeRadius = echoAoeRadius;
        this.echoFocusGrant = echoFocusGrant;
    }

    public float getExecuteBaseThreshold() { return executeBaseThreshold; }
    public float getExecuteScalingPerFocus() { return executeScalingPerFocus; }
    public float getExecuteMarkRadius() { return executeMarkRadius; }
    public int getMarkDurationTicks() { return markDurationTicks; }
    public int getExecuteMarkRefund() { return executeMarkRefund; }
    public float getThreePieceNoConsumeChance() { return threePieceNoConsumeChance; }
    public int getThreePieceExecuteBonusFocus() { return threePieceExecuteBonusFocus; }
    public int getEchoDurationTicks() { return echoDurationTicks; }
    public float getEchoDamagePercent() { return echoDamagePercent; }
    public float getEchoAoeRadius() { return echoAoeRadius; }
    public int getEchoFocusGrant() { return echoFocusGrant; }

    @Override
    public ResourceLocation getTypeId() { return FallenGemsAffixes.id("deadeye_weapon"); }

    @Override
    public MutableComponent getDescription(ItemStack stack, LootRarity rarity, float level) {
        Component baseVal = Component.literal(SetAffix.fmt(executeBaseThreshold * 100f) + "%").withStyle(ChatFormatting.DARK_RED);

        MutableComponent desc = Component.translatable("set_affix.fallen_gems_affixes.deadeye_bow.desc", baseVal)
                .withStyle(ChatFormatting.YELLOW);

        if (isShiftDown()) {
            Component scaleVal  = Component.literal(SetAffix.fmt(executeScalingPerFocus * 100f) + "%").withStyle(ChatFormatting.DARK_RED);
            Component radiusVal = Component.literal(SetAffix.fmt(executeMarkRadius)).withStyle(ChatFormatting.DARK_RED);
            Component durVal    = Component.literal(SetAffix.fmt(markDurationTicks / 20f) + "s").withStyle(ChatFormatting.DARK_RED);
            Component refundVal = Component.literal(String.valueOf(executeMarkRefund)).withStyle(ChatFormatting.DARK_RED);
            desc.append(Component.translatable("set_affix.fallen_gems_affixes.deadeye_bow.desc.shift",
                    scaleVal, radiusVal, durVal, refundVal).withStyle(ChatFormatting.GRAY));
        }

        return desc;
    }

    @Override
    public Component getBonusDescription(int threshold) {
        if (threshold == 3) {
            Component chanceVal = Component.literal(SetAffix.fmt(threePieceNoConsumeChance * 100f) + "%").withStyle(ChatFormatting.DARK_RED);
            Component focusVal  = Component.literal(String.valueOf(threePieceExecuteBonusFocus)).withStyle(ChatFormatting.DARK_RED);
            return Component.translatable("set_bonus.fallen_gems_affixes.deadeye.3", chanceVal, focusVal);
        }
        if (threshold == 5) {
            Component durVal = Component.literal(SetAffix.fmt(echoDurationTicks / 20f) + "s").withStyle(ChatFormatting.DARK_RED);
            MutableComponent desc = Component.translatable("set_bonus.fallen_gems_affixes.deadeye.5", durVal);

            if (isShiftDown()) {
                Component dmgVal   = Component.literal(SetAffix.fmt(echoDamagePercent * 100f) + "%").withStyle(ChatFormatting.DARK_RED);
                Component grantVal = Component.literal(String.valueOf(echoFocusGrant)).withStyle(ChatFormatting.DARK_RED);
                desc.append(Component.translatable("set_bonus.fallen_gems_affixes.deadeye.5.shift", dmgVal, grantVal)
                        .withStyle(ChatFormatting.GRAY));
            }

            return desc;
        }
        return null;
    }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        return !cat.isNone() && cat == LootCategory.BOW;
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