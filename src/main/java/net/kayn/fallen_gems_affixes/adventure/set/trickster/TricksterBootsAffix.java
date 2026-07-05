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

public class TricksterBootsAffix extends SetAffix {
    public static final Codec<TricksterBootsAffix> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ResourceLocation.CODEC.fieldOf("set").forGetter(a -> a.setId),
            Codec.INT.fieldOf("cooldown_ticks").forGetter(a -> a.cooldownTicks)
    ).apply(inst, TricksterBootsAffix::new));

    private final int cooldownTicks;

    public TricksterBootsAffix(ResourceLocation setId, int cooldownTicks) {
        super(setId);
        this.cooldownTicks = cooldownTicks;
    }

    public int getCooldownTicks() { return cooldownTicks; }

    @Override
    public ResourceLocation getTypeId() { return FallenGemsAffixes.id("trickster_boots"); }

    @Override
    public MutableComponent getDescription(ItemStack stack, LootRarity rarity, float level) {
        MutableComponent desc = Component.translatable("set_affix.fallen_gems_affixes.trickster_boots.desc")
                .withStyle(ChatFormatting.YELLOW);

        if (isShiftDown()) {
            Component value = Component.literal(SetAffix.fmt(cooldownTicks / 20f) + "s").withStyle(ChatFormatting.DARK_RED);
            desc.append(Component.translatable("set_affix.fallen_gems_affixes.trickster_boots.desc.shift", value)
                    .withStyle(ChatFormatting.GRAY));
        }

        return desc;
    }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        return !cat.isNone() && cat == LootCategory.BOOTS;
    }

    @Override
    public void applySetBonus(Player player, int pieceCount) {
        TricksterSetBonusHandler.onPieceCountChanged(player, pieceCount);
    }

    @Override
    public void removeSetBonus(Player player) {
        TricksterSetBonusHandler.onPieceCountChanged(player, 0);
    }

    @Override
    public int[] getBonusThresholds() { return TricksterSetConstants.BONUS_THRESHOLDS; }

    @Override
    public Component getBonusDescription(int threshold) { return null; }

    @Override
    public Codec<? extends SetAffix> getCodec() { return CODEC; }
}