package net.kayn.fallen_gems_affixes.adventure.set.trickster;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.kayn.fallen_gems_affixes.adventure.set.SetAffix;
import net.kayn.fallen_gems_affixes.adventure.set.trickster.bonus.TricksterSetBonusHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class TricksterChestplateAffix extends SetAffix {
    public static final Codec<TricksterChestplateAffix> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ResourceLocation.CODEC.fieldOf("set").forGetter(a -> a.setId),
            Codec.FLOAT.fieldOf("damage_reduction").forGetter(a -> a.damageReduction),
            Codec.INT.fieldOf("max_clones").forGetter(a -> a.maxClones),
            Codec.INT.fieldOf("cooldown_ticks").forGetter(a -> a.cooldownTicks),
            Codec.FLOAT.fieldOf("two_piece_clone_health_fraction").forGetter(a -> a.twoPieceCloneHealthFraction),
            Codec.INT.fieldOf("five_piece_bonus_clones").forGetter(a -> a.fivePieceBonusClones)
    ).apply(inst, TricksterChestplateAffix::new));

    private final float damageReduction;
    private final int maxClones;
    private final int cooldownTicks;
    private final float twoPieceCloneHealthFraction;
    private final int fivePieceBonusClones;

    public TricksterChestplateAffix(ResourceLocation setId, float damageReduction, int maxClones,
                                    int cooldownTicks, float twoPieceCloneHealthFraction, int fivePieceBonusClones) {
        super(setId);
        this.damageReduction = damageReduction;
        this.maxClones = maxClones;
        this.cooldownTicks = cooldownTicks;
        this.twoPieceCloneHealthFraction = twoPieceCloneHealthFraction;
        this.fivePieceBonusClones = fivePieceBonusClones;
    }

    public float getDamageReduction() { return damageReduction; }
    public int getMaxClones() { return maxClones; }
    public int getCooldownTicks() { return cooldownTicks; }
    public float getTwoPieceCloneHealthFraction() { return twoPieceCloneHealthFraction; }
    public int getFivePieceBonusClones() { return fivePieceBonusClones; }

    @Override
    public ResourceLocation getTypeId() { return FallenGemsAffixes.id("trickster_chestplate"); }

    @Override
    public MutableComponent getDescription(ItemStack stack, LootRarity rarity, float level) {
        Component clonesVal = Component.literal(String.valueOf(maxClones)).withStyle(ChatFormatting.DARK_RED);
        Component reductionVal = Component.literal(SetAffix.fmt(damageReduction * 100f) + "%").withStyle(ChatFormatting.DARK_RED);

        MutableComponent desc = Component.translatable("set_affix.fallen_gems_affixes.trickster_chestplate.desc",
                clonesVal, reductionVal).withStyle(ChatFormatting.YELLOW);

        if (isShiftDown()) {
            Component cooldownVal = Component.literal(SetAffix.fmt(cooldownTicks / 20f) + "s").withStyle(ChatFormatting.DARK_RED);
            desc.append(Component.translatable("set_affix.fallen_gems_affixes.trickster_chestplate.desc.shift", cooldownVal)
                    .withStyle(ChatFormatting.GRAY));
        }

        return desc;
    }

    @Override
    public Component getBonusDescription(int threshold) {
        if (threshold == 2) {
            Component val = Component.literal(SetAffix.fmt(twoPieceCloneHealthFraction * 100f) + "%")
                    .withStyle(ChatFormatting.DARK_RED);
            return Component.translatable("set_bonus.fallen_gems_affixes.trickster.2", val);
        }
        return null;
    }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        return !cat.isNone() && cat == LootCategory.CHESTPLATE;
    }

    @Override
    public void applySetBonus(Player player, int pieceCount) { TricksterSetBonusHandler.onPieceCountChanged(player, pieceCount); }

    @Override
    public void removeSetBonus(Player player) { TricksterSetBonusHandler.onPieceCountChanged(player, 0); }

    @Override
    public int[] getBonusThresholds() { return TricksterSetConstants.BONUS_THRESHOLDS; }

    @Override
    public Codec<? extends SetAffix> getCodec() { return CODEC; }
}