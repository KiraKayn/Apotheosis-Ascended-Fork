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

public class ColossusChestplateAffix extends SetAffix {
    public static final Codec<ColossusChestplateAffix> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ResourceLocation.CODEC.fieldOf("set").forGetter(a -> a.setId),
            Codec.INT.fieldOf("orbs_per_hit").forGetter(a -> a.orbsPerHit),
            Codec.FLOAT.fieldOf("heavy_hit_threshold").forGetter(a -> a.heavyHitThreshold),
            Codec.INT.fieldOf("heavy_hit_bonus_orbs").forGetter(a -> a.heavyHitBonusOrbs),
            Codec.FLOAT.fieldOf("max_health_per_orb_fraction").forGetter(a -> a.maxHealthPerOrbFraction),
            Codec.FLOAT.fieldOf("adaptive_vitality_fraction").forGetter(a -> a.adaptiveVitalityFraction),
            Codec.INT.fieldOf("last_stand_cooldown_ticks").forGetter(a -> a.lastStandCooldownTicks),
            Codec.INT.fieldOf("last_stand_invuln_ticks").forGetter(a -> a.lastStandInvulnTicks),
            Codec.FLOAT.fieldOf("last_stand_shockwave_damage").forGetter(a -> a.lastStandShockwaveDamage),
            Codec.FLOAT.fieldOf("last_stand_shockwave_radius").forGetter(a -> a.lastStandShockwaveRadius)
    ).apply(inst, ColossusChestplateAffix::new));

    private final int orbsPerHit;
    private final float heavyHitThreshold;
    private final int heavyHitBonusOrbs;
    private final float maxHealthPerOrbFraction;
    private final float adaptiveVitalityFraction;
    private final int lastStandCooldownTicks;
    private final int lastStandInvulnTicks;
    private final float lastStandShockwaveDamage;
    private final float lastStandShockwaveRadius;

    public ColossusChestplateAffix(ResourceLocation setId, int orbsPerHit, float heavyHitThreshold,
                                   int heavyHitBonusOrbs, float maxHealthPerOrbFraction,
                                   float adaptiveVitalityFraction, int lastStandCooldownTicks,
                                   int lastStandInvulnTicks, float lastStandShockwaveDamage,
                                   float lastStandShockwaveRadius) {
        super(setId);
        this.orbsPerHit = orbsPerHit;
        this.heavyHitThreshold = heavyHitThreshold;
        this.heavyHitBonusOrbs = heavyHitBonusOrbs;
        this.maxHealthPerOrbFraction = maxHealthPerOrbFraction;
        this.adaptiveVitalityFraction = adaptiveVitalityFraction;
        this.lastStandCooldownTicks = lastStandCooldownTicks;
        this.lastStandInvulnTicks = lastStandInvulnTicks;
        this.lastStandShockwaveDamage = lastStandShockwaveDamage;
        this.lastStandShockwaveRadius = lastStandShockwaveRadius;
    }

    public int getOrbsPerHit() { return orbsPerHit; }
    public float getHeavyHitThreshold() { return heavyHitThreshold; }
    public int getHeavyHitBonusOrbs() { return heavyHitBonusOrbs; }
    public float getMaxHealthPerOrbFraction() { return maxHealthPerOrbFraction; }
    public float getAdaptiveVitalityFraction() { return adaptiveVitalityFraction; }
    public int getLastStandCooldownTicks() { return lastStandCooldownTicks; }
    public int getLastStandInvulnTicks() { return lastStandInvulnTicks; }
    public float getLastStandShockwaveDamage() { return lastStandShockwaveDamage; }
    public float getLastStandShockwaveRadius() { return lastStandShockwaveRadius; }

    @Override
    public ResourceLocation getTypeId() { return FallenGemsAffixes.id("colossus_chestplate"); }

    @Override
    public MutableComponent getDescription(ItemStack stack, LootRarity rarity, float level) {
        Component orbs = Component.literal(String.valueOf(orbsPerHit)).withStyle(ChatFormatting.DARK_RED);
        Component perOrb = Component.literal(SetAffix.fmt(maxHealthPerOrbFraction * 100f) + "%").withStyle(ChatFormatting.DARK_RED);

        MutableComponent desc = Component.translatable("set_affix.fallen_gems_affixes.colossus_chestplate.desc",
                orbs, perOrb).withStyle(ChatFormatting.YELLOW);

        if (isShiftDown()) {
            Component thresh = Component.literal(SetAffix.fmt(heavyHitThreshold * 100f) + "%").withStyle(ChatFormatting.DARK_RED);
            Component bonus = Component.literal("+" + heavyHitBonusOrbs).withStyle(ChatFormatting.DARK_RED);
            desc.append(Component.translatable("set_affix.fallen_gems_affixes.colossus_chestplate.desc.shift", thresh, bonus)
                    .withStyle(ChatFormatting.GRAY));
        }

        return desc;
    }

    @Override
    public Component getBonusDescription(int threshold) {
        if (threshold == 2) {
            Component val = Component.literal(SetAffix.fmt(adaptiveVitalityFraction * 100f) + "%")
                    .withStyle(ChatFormatting.DARK_RED);
            return Component.translatable("set_bonus.fallen_gems_affixes.colossus.2", val);
        }
        if (threshold == 3) {
            Component cooldown = Component.literal(SetAffix.fmt(lastStandCooldownTicks / 20f) + "s")
                    .withStyle(ChatFormatting.DARK_RED);
            Component invuln   = Component.literal(SetAffix.fmt(lastStandInvulnTicks / 20f) + "s")
                    .withStyle(ChatFormatting.DARK_RED);
            return Component.translatable("set_bonus.fallen_gems_affixes.colossus.3", invuln, cooldown);
        }
        return null;
    }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        return !cat.isNone() && cat == LootCategory.CHESTPLATE;
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