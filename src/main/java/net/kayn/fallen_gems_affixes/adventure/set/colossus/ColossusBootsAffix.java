package net.kayn.fallen_gems_affixes.adventure.set.colossus;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.kayn.fallen_gems_affixes.adventure.set.SetAffix;
import net.kayn.fallen_gems_affixes.adventure.set.colossus.bonus.ColossusSetBonusHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ColossusBootsAffix extends SetAffix {
    public static final Codec<ColossusBootsAffix> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ResourceLocation.CODEC.fieldOf("set").forGetter(a -> a.setId),
            Codec.FLOAT.fieldOf("speed_per_orb").forGetter(a -> a.speedPerOrb),
            Codec.INT.fieldOf("root_duration_ticks").forGetter(a -> a.rootDurationTicks),
            Codec.INT.fieldOf("standing_still_threshold_ticks").forGetter(a -> a.standingStillThresholdTicks)
    ).apply(inst, ColossusBootsAffix::new));

    private final float speedPerOrb;
    private final int rootDurationTicks;
    private final int standingStillThresholdTicks;

    public ColossusBootsAffix(ResourceLocation setId, float speedPerOrb,
                              int rootDurationTicks, int standingStillThresholdTicks) {
        super(setId);
        this.speedPerOrb = speedPerOrb;
        this.rootDurationTicks = rootDurationTicks;
        this.standingStillThresholdTicks = standingStillThresholdTicks;
    }

    public float getSpeedPerOrb() { return speedPerOrb; }
    public int getRootDurationTicks() { return rootDurationTicks; }
    public int getStandingStillThresholdTicks() { return standingStillThresholdTicks; }

    @Override
    public ResourceLocation getTypeId() { return FallenGemsAffixes.id("colossus_boots"); }

    @Override
    public MutableComponent getDescription(ItemStack stack, LootRarity rarity, float level) {
        Component speed = Component.literal(SetAffix.fmt(speedPerOrb * 100f) + "%").withStyle(ChatFormatting.DARK_RED);

        MutableComponent desc = Component.translatable("set_affix.fallen_gems_affixes.colossus_boots.desc", speed)
                .withStyle(ChatFormatting.YELLOW);

        if (isShiftDown()) {
            Component rootSec = Component.literal(SetAffix.fmt(rootDurationTicks / 20f) + "s").withStyle(ChatFormatting.DARK_RED);
            Component standSec = Component.literal(SetAffix.fmt(standingStillThresholdTicks / 20f) + "s").withStyle(ChatFormatting.DARK_RED);
            desc.append(Component.translatable("set_affix.fallen_gems_affixes.colossus_boots.desc.shift", rootSec, standSec)
                    .withStyle(ChatFormatting.GRAY));
        }

        return desc;
    }

    @Override
    public Component getBonusDescription(int threshold) { return null; }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        return !cat.isNone() && cat == LootCategory.BOOTS;
    }

    @Override
    public void applySetBonus(Player player, int pieceCount) { ColossusSetBonusHandler.onPieceCountChanged(player, pieceCount); }

    @Override
    public void removeSetBonus(Player player) { ColossusSetBonusHandler.onPieceCountChanged(player, 0); }

    @Override
    public int[] getBonusThresholds() { return ColossusSetConstants.BONUS_THRESHOLDS; }

    @Override
    public Codec<? extends SetAffix> getCodec() { return CODEC; }
}