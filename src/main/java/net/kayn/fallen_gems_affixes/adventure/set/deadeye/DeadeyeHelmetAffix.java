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

public class DeadeyeHelmetAffix extends SetAffix {
    public static final Codec<DeadeyeHelmetAffix> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ResourceLocation.CODEC.fieldOf("set").forGetter(a -> a.setId),
            Codec.INT.fieldOf("focus_per_crit").forGetter(a -> a.focusPerCrit),
            Codec.FLOAT.fieldOf("crit_burst_damage_percent").forGetter(a -> a.critBurstDamagePercent),
            Codec.FLOAT.fieldOf("crit_burst_radius").forGetter(a -> a.critBurstRadius)
    ).apply(inst, DeadeyeHelmetAffix::new));

    private final int focusPerCrit;
    private final float critBurstDamagePercent;
    private final float critBurstRadius;

    public DeadeyeHelmetAffix(ResourceLocation setId, int focusPerCrit, float critBurstDamagePercent, float critBurstRadius) {
        super(setId);
        this.focusPerCrit = focusPerCrit;
        this.critBurstDamagePercent = critBurstDamagePercent;
        this.critBurstRadius = critBurstRadius;
    }

    public int getFocusPerCrit() { return focusPerCrit; }
    public float getCritBurstDamagePercent() { return critBurstDamagePercent; }
    public float getCritBurstRadius() { return critBurstRadius; }

    @Override
    public ResourceLocation getTypeId() { return FallenGemsAffixes.id("deadeye_helmet"); }

    @Override
    public MutableComponent getDescription(ItemStack stack, LootRarity rarity, float level) {
        Component focusVal = Component.literal(String.valueOf(focusPerCrit)).withStyle(ChatFormatting.DARK_RED);
        Component burstVal = Component.literal(SetAffix.fmt(critBurstDamagePercent * 100f) + "%").withStyle(ChatFormatting.DARK_RED);

        MutableComponent desc = Component.translatable("set_affix.fallen_gems_affixes.deadeye_helmet.desc", focusVal, burstVal)
                .withStyle(ChatFormatting.YELLOW);

        if (isShiftDown()) {
            Component radiusVal = Component.literal(SetAffix.fmt(critBurstRadius)).withStyle(ChatFormatting.DARK_RED);
            desc.append(Component.translatable("set_affix.fallen_gems_affixes.deadeye_helmet.desc.shift", radiusVal)
                    .withStyle(ChatFormatting.GRAY));
        }

        return desc;
    }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        return !cat.isNone() && cat == LootCategory.HELMET;
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