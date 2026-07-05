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

public class ColossusShieldAffix extends SetAffix {
    public static final Codec<ColossusShieldAffix> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ResourceLocation.CODEC.fieldOf("set").forGetter(a -> a.setId),
            Codec.FLOAT.fieldOf("reflect_fraction").forGetter(a -> a.reflectFraction),
            Codec.INT.fieldOf("stun_duration_ticks").forGetter(a -> a.stunDurationTicks),
            Codec.FLOAT.fieldOf("stun_radius").forGetter(a -> a.stunRadius),
            Codec.FLOAT.fieldOf("five_piece_orb_gen_bonus").forGetter(a -> a.fivePieceOrbGenBonus)
    ).apply(inst, ColossusShieldAffix::new));

    private final float reflectFraction;
    private final int stunDurationTicks;
    private final float stunRadius;
    private final float fivePieceOrbGenBonus;

    public ColossusShieldAffix(ResourceLocation setId, float reflectFraction, int stunDurationTicks,
                               float stunRadius, float fivePieceOrbGenBonus) {
        super(setId);
        this.reflectFraction = reflectFraction;
        this.stunDurationTicks = stunDurationTicks;
        this.stunRadius = stunRadius;
        this.fivePieceOrbGenBonus = fivePieceOrbGenBonus;
    }

    public float getReflectFraction() { return reflectFraction; }
    public int getStunDurationTicks() { return stunDurationTicks; }
    public float getStunRadius() { return stunRadius; }
    public float getFivePieceOrbGenBonus() { return fivePieceOrbGenBonus; }

    @Override
    public ResourceLocation getTypeId() { return FallenGemsAffixes.id("colossus_shield"); }

    @Override
    public MutableComponent getDescription(ItemStack stack, LootRarity rarity, float level) {
        MutableComponent desc = Component.translatable("set_affix.fallen_gems_affixes.colossus_shield.desc")
                .withStyle(ChatFormatting.YELLOW);

        if (isShiftDown()) {
            Component reflect = Component.literal(SetAffix.fmt(reflectFraction * 100f) + "%").withStyle(ChatFormatting.DARK_RED);
            Component radius = Component.literal(SetAffix.fmt(stunRadius)).withStyle(ChatFormatting.DARK_RED);
            Component stunSec = Component.literal(SetAffix.fmt(stunDurationTicks / 20f) + "s").withStyle(ChatFormatting.DARK_RED);
            desc.append(Component.translatable("set_affix.fallen_gems_affixes.colossus_shield.desc.shift", reflect, radius, stunSec)
                    .withStyle(ChatFormatting.GRAY));
        }

        return desc;
    }

    @Override
    public Component getBonusDescription(int threshold) {
        if (threshold == 5) {
            MutableComponent desc = Component.translatable("set_bonus.fallen_gems_affixes.colossus.5");

            if (isShiftDown()) {
                Component bonus = Component.literal(SetAffix.fmt(fivePieceOrbGenBonus * 100f) + "%").withStyle(ChatFormatting.DARK_RED);
                desc.append(Component.translatable("set_bonus.fallen_gems_affixes.colossus.5.shift", bonus)
                        .withStyle(ChatFormatting.GRAY));
            }

            return desc;
        }
        return null;
    }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        return !cat.isNone() && cat == LootCategory.SHIELD;
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