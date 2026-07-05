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

public class DeadeyeLeggingsAffix extends SetAffix {
    public static final Codec<DeadeyeLeggingsAffix> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ResourceLocation.CODEC.fieldOf("set").forGetter(a -> a.setId),
            Codec.FLOAT.fieldOf("crit_damage_per_stack").forGetter(a -> a.critDamagePerStack),
            Codec.INT.fieldOf("max_stacks").forGetter(a -> a.maxStacks),
            Codec.INT.fieldOf("stack_reset_delay_ticks").forGetter(a -> a.stackResetDelayTicks),
            Codec.FLOAT.fieldOf("consume_bonus_per_stack").forGetter(a -> a.consumeBonusPerStack),
            Codec.INT.fieldOf("four_piece_max_focus_bonus").forGetter(a -> a.fourPieceMaxFocusBonus),
            Codec.INT.fieldOf("four_piece_cooldown_reduction_ticks").forGetter(a -> a.fourPieceCooldownReductionTicks),
            Codec.FLOAT.fieldOf("four_piece_mark_damage_bonus").forGetter(a -> a.fourPieceMarkDamageBonus)
    ).apply(inst, DeadeyeLeggingsAffix::new));

    private final float critDamagePerStack;
    private final int maxStacks;
    private final int stackResetDelayTicks;
    private final float consumeBonusPerStack;
    private final int fourPieceMaxFocusBonus;
    private final int fourPieceCooldownReductionTicks;
    private final float fourPieceMarkDamageBonus;

    public DeadeyeLeggingsAffix(ResourceLocation setId, float critDamagePerStack, int maxStacks, int stackResetDelayTicks,
                                float consumeBonusPerStack, int fourPieceMaxFocusBonus,
                                int fourPieceCooldownReductionTicks, float fourPieceMarkDamageBonus) {
        super(setId);
        this.critDamagePerStack = critDamagePerStack;
        this.maxStacks = maxStacks;
        this.stackResetDelayTicks = stackResetDelayTicks;
        this.consumeBonusPerStack = consumeBonusPerStack;
        this.fourPieceMaxFocusBonus = fourPieceMaxFocusBonus;
        this.fourPieceCooldownReductionTicks = fourPieceCooldownReductionTicks;
        this.fourPieceMarkDamageBonus = fourPieceMarkDamageBonus;
    }

    public float getCritDamagePerStack() { return critDamagePerStack; }
    public int getMaxStacks() { return maxStacks; }
    public int getStackResetDelayTicks() { return stackResetDelayTicks; }
    public float getConsumeBonusPerStack() { return consumeBonusPerStack; }
    public int getFourPieceMaxFocusBonus() { return fourPieceMaxFocusBonus; }
    public int getFourPieceCooldownReductionTicks() { return fourPieceCooldownReductionTicks; }
    public float getFourPieceMarkDamageBonus() { return fourPieceMarkDamageBonus; }

    @Override
    public ResourceLocation getTypeId() { return FallenGemsAffixes.id("deadeye_leggings"); }

    @Override
    public MutableComponent getDescription(ItemStack stack, LootRarity rarity, float level) {
        Component critVal = Component.literal(SetAffix.fmt(critDamagePerStack * 100f) + "%").withStyle(ChatFormatting.DARK_RED);

        MutableComponent desc = Component.translatable("set_affix.fallen_gems_affixes.deadeye_leggings.desc", critVal)
                .withStyle(ChatFormatting.YELLOW);

        if (isShiftDown()) {
            Component stacksVal = Component.literal(String.valueOf(maxStacks)).withStyle(ChatFormatting.DARK_RED);
            Component resetVal  = Component.literal(SetAffix.fmt(stackResetDelayTicks / 20f) + "s").withStyle(ChatFormatting.DARK_RED);
            desc.append(Component.translatable("set_affix.fallen_gems_affixes.deadeye_leggings.desc.shift",
                    stacksVal, resetVal).withStyle(ChatFormatting.GRAY));
        }

        return desc;
    }

    @Override
    public Component getBonusDescription(int threshold) {
        if (threshold == 4) {
            Component maxVal = Component.literal(String.valueOf(fourPieceMaxFocusBonus)).withStyle(ChatFormatting.DARK_RED);
            Component cdVal  = Component.literal(SetAffix.fmt(fourPieceCooldownReductionTicks / 20f) + "s").withStyle(ChatFormatting.DARK_RED);
            MutableComponent desc = Component.translatable("set_bonus.fallen_gems_affixes.deadeye.4", maxVal, cdVal);

            if (isShiftDown()) {
                Component markVal = Component.literal(SetAffix.fmt(fourPieceMarkDamageBonus * 100f) + "%").withStyle(ChatFormatting.DARK_RED);
                desc.append(Component.translatable("set_bonus.fallen_gems_affixes.deadeye.4.shift", markVal)
                        .withStyle(ChatFormatting.GRAY));
            }

            return desc;
        }
        return null;
    }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        return !cat.isNone() && cat == LootCategory.LEGGINGS;
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