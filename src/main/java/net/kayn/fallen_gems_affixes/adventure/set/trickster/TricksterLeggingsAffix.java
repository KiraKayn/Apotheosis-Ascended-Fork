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

public class TricksterLeggingsAffix extends SetAffix {
    public static final Codec<TricksterLeggingsAffix> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ResourceLocation.CODEC.fieldOf("set").forGetter(a -> a.setId),
            Codec.FLOAT.fieldOf("explosion_multiplier").forGetter(a -> a.explosionMultiplier),
            Codec.FLOAT.fieldOf("radius").forGetter(a -> a.radius),
            Codec.INT.fieldOf("four_piece_cooldown_reduction_ticks").forGetter(a -> a.fourPieceCooldownReductionTicks)
    ).apply(inst, TricksterLeggingsAffix::new));

    private final float explosionMultiplier;
    private final float radius;
    private final int fourPieceCooldownReductionTicks;

    public TricksterLeggingsAffix(ResourceLocation setId, float explosionMultiplier, float radius, int fourPieceCooldownReductionTicks) {
        super(setId);
        this.explosionMultiplier = explosionMultiplier;
        this.radius = radius;
        this.fourPieceCooldownReductionTicks = fourPieceCooldownReductionTicks;
    }

    public float getExplosionMultiplier() { return explosionMultiplier; }
    public float getRadius() { return radius; }
    public int getFourPieceCooldownReductionTicks() { return fourPieceCooldownReductionTicks; }

    @Override
    public ResourceLocation getTypeId() { return FallenGemsAffixes.id("trickster_leggings"); }

    @Override
    public MutableComponent getDescription(ItemStack stack, LootRarity rarity, float level) {
        Component dmgVal = Component.literal(SetAffix.fmt(explosionMultiplier * 100f) + "%").withStyle(ChatFormatting.DARK_RED);

        MutableComponent desc = Component.translatable("set_affix.fallen_gems_affixes.trickster_leggings.desc", dmgVal)
                .withStyle(ChatFormatting.YELLOW);

        if (isShiftDown()) {
            Component radiusVal = Component.literal(SetAffix.fmt(radius)).withStyle(ChatFormatting.DARK_RED);
            desc.append(Component.translatable("set_affix.fallen_gems_affixes.trickster_leggings.desc.shift", radiusVal)
                    .withStyle(ChatFormatting.GRAY));
        }

        return desc;
    }

    @Override
    public Component getBonusDescription(int threshold) {
        if (threshold == 4) {
            Component val = Component.literal(SetAffix.fmt(fourPieceCooldownReductionTicks / 20f) + "s")
                    .withStyle(ChatFormatting.DARK_RED);
            return Component.translatable("set_bonus.fallen_gems_affixes.trickster.4", val);
        }
        return null;
    }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        return !cat.isNone() && cat == LootCategory.LEGGINGS;
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