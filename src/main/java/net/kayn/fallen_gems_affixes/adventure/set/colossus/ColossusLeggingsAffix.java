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

public class ColossusLeggingsAffix extends SetAffix {
    public static final Codec<ColossusLeggingsAffix> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ResourceLocation.CODEC.fieldOf("set").forGetter(a -> a.setId),
            Codec.FLOAT.fieldOf("base_shockwave_damage").forGetter(a -> a.baseShockwaveDamage),
            Codec.FLOAT.fieldOf("damage_scale_fraction").forGetter(a -> a.damageScaleFraction),
            Codec.FLOAT.fieldOf("shockwave_radius").forGetter(a -> a.shockwaveRadius),
            Codec.FLOAT.fieldOf("max_orb_radius_multiplier").forGetter(a -> a.maxOrbRadiusMultiplier),
            Codec.FLOAT.fieldOf("max_orb_damage_multiplier").forGetter(a -> a.maxOrbDamageMultiplier),
            Codec.FLOAT.fieldOf("resonance_heal_fraction").forGetter(a -> a.resonanceHealFraction),
            Codec.FLOAT.fieldOf("resonance_attack_speed_boost").forGetter(a -> a.resonanceAttackSpeedBoost),
            Codec.INT.fieldOf("resonance_attack_speed_duration_ticks").forGetter(a -> a.resonanceAttackSpeedDurationTicks),
            Codec.INT.fieldOf("resonance_cooldown_reduction_ticks").forGetter(a -> a.resonanceCooldownReductionTicks)
    ).apply(inst, ColossusLeggingsAffix::new));

    private final float baseShockwaveDamage;
    private final float damageScaleFraction;
    private final float shockwaveRadius;
    private final float maxOrbRadiusMultiplier;
    private final float maxOrbDamageMultiplier;
    private final float resonanceHealFraction;
    private final float resonanceAttackSpeedBoost;
    private final int resonanceAttackSpeedDurationTicks;
    private final int resonanceCooldownReductionTicks;

    public ColossusLeggingsAffix(ResourceLocation setId, float baseShockwaveDamage, float damageScaleFraction,
                                 float shockwaveRadius, float maxOrbRadiusMultiplier, float maxOrbDamageMultiplier,
                                 float resonanceHealFraction, float resonanceAttackSpeedBoost,
                                 int resonanceAttackSpeedDurationTicks, int resonanceCooldownReductionTicks) {
        super(setId);
        this.baseShockwaveDamage = baseShockwaveDamage;
        this.damageScaleFraction = damageScaleFraction;
        this.shockwaveRadius = shockwaveRadius;
        this.maxOrbRadiusMultiplier = maxOrbRadiusMultiplier;
        this.maxOrbDamageMultiplier = maxOrbDamageMultiplier;
        this.resonanceHealFraction = resonanceHealFraction;
        this.resonanceAttackSpeedBoost = resonanceAttackSpeedBoost;
        this.resonanceAttackSpeedDurationTicks = resonanceAttackSpeedDurationTicks;
        this.resonanceCooldownReductionTicks = resonanceCooldownReductionTicks;
    }

    public float getBaseShockwaveDamage() { return baseShockwaveDamage; }
    public float getDamageScaleFraction() { return damageScaleFraction; }
    public float getShockwaveRadius() { return shockwaveRadius; }
    public float getMaxOrbRadiusMultiplier() { return maxOrbRadiusMultiplier; }
    public float getMaxOrbDamageMultiplier() { return maxOrbDamageMultiplier; }
    public float getResonanceHealFraction() { return resonanceHealFraction; }
    public float getResonanceAttackSpeedBoost() { return resonanceAttackSpeedBoost; }
    public int getResonanceAttackSpeedDurationTicks() { return resonanceAttackSpeedDurationTicks; }
    public int getResonanceCooldownReductionTicks() { return resonanceCooldownReductionTicks; }

    @Override
    public ResourceLocation getTypeId() { return FallenGemsAffixes.id("colossus_leggings"); }

    @Override
    public MutableComponent getDescription(ItemStack stack, LootRarity rarity, float level) {
        MutableComponent desc = Component.translatable("set_affix.fallen_gems_affixes.colossus_leggings.desc")
                .withStyle(ChatFormatting.YELLOW);

        if (isShiftDown()) {
            Component base = Component.literal(SetAffix.fmt(baseShockwaveDamage)).withStyle(ChatFormatting.DARK_RED);
            Component scale = Component.literal(SetAffix.fmt(damageScaleFraction * 100f) + "%").withStyle(ChatFormatting.DARK_RED);
            Component dmgMult = Component.literal(SetAffix.fmt(maxOrbDamageMultiplier) + "x").withStyle(ChatFormatting.DARK_RED);
            Component radiusMult = Component.literal(SetAffix.fmt(maxOrbRadiusMultiplier) + "x").withStyle(ChatFormatting.DARK_RED);
            desc.append(Component.translatable("set_affix.fallen_gems_affixes.colossus_leggings.desc.shift",
                    base, scale, dmgMult, radiusMult).withStyle(ChatFormatting.GRAY));
        }

        return desc;
    }

    @Override
    public Component getBonusDescription(int threshold) {
        if (threshold == 4) {
            MutableComponent desc = Component.translatable("set_bonus.fallen_gems_affixes.colossus.4");

            if (isShiftDown()) {
                Component heal = Component.literal(SetAffix.fmt(resonanceHealFraction * 100f) + "%").withStyle(ChatFormatting.DARK_RED);
                Component speed = Component.literal(SetAffix.fmt(resonanceAttackSpeedBoost * 100f) + "%").withStyle(ChatFormatting.DARK_RED);
                Component duration = Component.literal(SetAffix.fmt(resonanceAttackSpeedDurationTicks / 20f) + "s").withStyle(ChatFormatting.DARK_RED);
                Component cd = Component.literal(SetAffix.fmt(resonanceCooldownReductionTicks / 20f) + "s").withStyle(ChatFormatting.DARK_RED);
                desc.append(Component.translatable("set_bonus.fallen_gems_affixes.colossus.4.shift", heal, speed, duration, cd)
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
    public void applySetBonus(Player player, int pieceCount) { ColossusSetBonusHandler.onPieceCountChanged(player, pieceCount); }

    @Override
    public void removeSetBonus(Player player) { ColossusSetBonusHandler.onPieceCountChanged(player, 0); }

    @Override
    public int[] getBonusThresholds() { return ColossusSetConstants.BONUS_THRESHOLDS; }

    @Override
    public Codec<? extends SetAffix> getCodec() { return CODEC; }
}