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

public class DeadeyeBootsAffix extends SetAffix {
    public static final Codec<DeadeyeBootsAffix> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ResourceLocation.CODEC.fieldOf("set").forGetter(a -> a.setId),
            Codec.INT.fieldOf("root_focus_cost").forGetter(a -> a.rootFocusCost),
            Codec.INT.fieldOf("root_duration_ticks").forGetter(a -> a.rootDurationTicks)
    ).apply(inst, DeadeyeBootsAffix::new));

    private final int rootFocusCost;
    private final int rootDurationTicks;

    public DeadeyeBootsAffix(ResourceLocation setId, int rootFocusCost, int rootDurationTicks) {
        super(setId);
        this.rootFocusCost = rootFocusCost;
        this.rootDurationTicks = rootDurationTicks;
    }

    public int getRootFocusCost() { return rootFocusCost; }
    public int getRootDurationTicks() { return rootDurationTicks; }

    @Override
    public ResourceLocation getTypeId() { return FallenGemsAffixes.id("deadeye_boots"); }

    @Override
    public MutableComponent getDescription(ItemStack stack, LootRarity rarity, float level) {
        MutableComponent desc = Component.translatable("set_affix.fallen_gems_affixes.deadeye_boots.desc")
                .withStyle(ChatFormatting.YELLOW);

        if (isShiftDown()) {
            Component costVal = Component.literal(String.valueOf(rootFocusCost)).withStyle(ChatFormatting.DARK_RED);
            Component durVal  = Component.literal(SetAffix.fmt(rootDurationTicks / 20f) + "s").withStyle(ChatFormatting.DARK_RED);
            desc.append(Component.translatable("set_affix.fallen_gems_affixes.deadeye_boots.desc.shift", costVal, durVal)
                    .withStyle(ChatFormatting.GRAY));
        }

        return desc;
    }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        return !cat.isNone() && cat == LootCategory.BOOTS;
    }

    @Override
    public void applySetBonus(Player player, int pieceCount) { DeadeyeSetBonusHandler.onPieceCountChanged(player, pieceCount); }

    @Override
    public void removeSetBonus(Player player) { DeadeyeSetBonusHandler.onPieceCountChanged(player, 0); }

    @Override
    public int[] getBonusThresholds() { return DeadeyeSetConstants.BONUS_THRESHOLDS; }

    @Override
    public Component getBonusDescription(int threshold) { return null; }

    @Override
    public Codec<? extends SetAffix> getCodec() { return CODEC; }
}