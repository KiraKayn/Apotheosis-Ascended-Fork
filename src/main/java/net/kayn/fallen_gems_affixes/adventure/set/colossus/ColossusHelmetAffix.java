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

public class ColossusHelmetAffix extends SetAffix {
    public static final Codec<ColossusHelmetAffix> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ResourceLocation.CODEC.fieldOf("set").forGetter(a -> a.setId),
            Codec.FLOAT.fieldOf("shockwave_base_damage").forGetter(a -> a.shockwaveBaseDamage),
            Codec.FLOAT.fieldOf("shockwave_damage_per_orb").forGetter(a -> a.shockwaveDamagePerOrb),
            Codec.FLOAT.fieldOf("shockwave_radius").forGetter(a -> a.shockwaveRadius)
    ).apply(inst, ColossusHelmetAffix::new));

    private final float shockwaveBaseDamage;
    private final float shockwaveDamagePerOrb;
    private final float shockwaveRadius;

    public ColossusHelmetAffix(ResourceLocation setId, float shockwaveBaseDamage,
                               float shockwaveDamagePerOrb, float shockwaveRadius) {
        super(setId);
        this.shockwaveBaseDamage = shockwaveBaseDamage;
        this.shockwaveDamagePerOrb = shockwaveDamagePerOrb;
        this.shockwaveRadius = shockwaveRadius;
    }

    public float getShockwaveBaseDamage() { return shockwaveBaseDamage; }
    public float getShockwaveDamagePerOrb() { return shockwaveDamagePerOrb; }
    public float getShockwaveRadius() { return shockwaveRadius; }

    @Override
    public ResourceLocation getTypeId() { return FallenGemsAffixes.id("colossus_helmet"); }

    @Override
    public MutableComponent getDescription(ItemStack stack, LootRarity rarity, float level) {
        MutableComponent desc = Component.translatable("set_affix.fallen_gems_affixes.colossus_helmet.desc")
                .withStyle(ChatFormatting.YELLOW);

        if (isShiftDown()) {
            Component base = Component.literal(SetAffix.fmt(shockwaveBaseDamage)).withStyle(ChatFormatting.DARK_RED);
            Component perOrb = Component.literal(SetAffix.fmt(shockwaveDamagePerOrb)).withStyle(ChatFormatting.DARK_RED);
            Component radius = Component.literal(SetAffix.fmt(shockwaveRadius)).withStyle(ChatFormatting.DARK_RED);
            desc.append(Component.translatable("set_affix.fallen_gems_affixes.colossus_helmet.desc.shift", base, perOrb, radius)
                    .withStyle(ChatFormatting.GRAY));
        }

        return desc;
    }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        return !cat.isNone() && cat == LootCategory.HELMET;
    }

    @Override
    public Component getBonusDescription(int threshold) { return null; }

    @Override
    public void applySetBonus(Player player, int pieceCount) { ColossusSetBonusHandler.onPieceCountChanged(player, pieceCount); }

    @Override
    public void removeSetBonus(Player player) { ColossusSetBonusHandler.onPieceCountChanged(player, 0); }

    @Override
    public int[] getBonusThresholds() { return ColossusSetConstants.BONUS_THRESHOLDS; }

    @Override
    public Codec<? extends SetAffix> getCodec() { return CODEC; }
}