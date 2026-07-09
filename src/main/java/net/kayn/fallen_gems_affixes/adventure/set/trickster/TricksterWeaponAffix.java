package net.kayn.fallen_gems_affixes.adventure.set.trickster;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.kayn.fallen_gems_affixes.adventure.set.SetAffix;
import net.kayn.fallen_gems_affixes.adventure.set.SetAffixRegistry;
import net.kayn.fallen_gems_affixes.adventure.set.trickster.bonus.TricksterSetBonusHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

public class TricksterWeaponAffix extends SetAffix {
    public static final Codec<TricksterWeaponAffix> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ResourceLocation.CODEC.fieldOf("set").forGetter(a -> a.setId),
            Codec.FLOAT.fieldOf("damage_multiplier").forGetter(a -> a.damageMultiplier),
            Codec.INT.fieldOf("cooldown_ticks").forGetter(a -> a.cooldownTicks),
            Codec.FLOAT.fieldOf("three_piece_speed_per_clone").forGetter(a -> a.threePieceSpeedPerClone),
            Codec.FLOAT.fieldOf("three_piece_dodge_per_clone").forGetter(a -> a.threePieceDodgePerClone),
            Codec.FLOAT.fieldOf("five_piece_kill_refresh_chance").forGetter(a -> a.fivePieceKillRefreshChance)
    ).apply(inst, TricksterWeaponAffix::new));

    private final float damageMultiplier;
    private final int cooldownTicks;
    private final float threePieceSpeedPerClone;
    private final float threePieceDodgePerClone;
    private final float fivePieceKillRefreshChance;

    public TricksterWeaponAffix(ResourceLocation setId, float damageMultiplier, int cooldownTicks,
                                float threePieceSpeedPerClone, float threePieceDodgePerClone,
                                float fivePieceKillRefreshChance) {
        super(setId);
        this.damageMultiplier = damageMultiplier;
        this.cooldownTicks = cooldownTicks;
        this.threePieceSpeedPerClone = threePieceSpeedPerClone;
        this.threePieceDodgePerClone = threePieceDodgePerClone;
        this.fivePieceKillRefreshChance = fivePieceKillRefreshChance;
    }

    public float getDamageMultiplier() { return damageMultiplier; }
    public int getCooldownTicks() { return cooldownTicks; }
    public float getThreePieceSpeedPerClone() { return threePieceSpeedPerClone; }
    public float getThreePieceDodgePerClone() { return threePieceDodgePerClone; }
    public float getFivePieceKillRefreshChance() { return fivePieceKillRefreshChance; }

    @Override
    public ResourceLocation getTypeId() { return FallenGemsAffixes.id("trickster_weapon"); }

    @Override
    public MutableComponent getDescription(ItemStack stack, LootRarity rarity, float level) {
        Component dmgVal = Component.literal(SetAffix.fmt(damageMultiplier * 100f) + "%").withStyle(ChatFormatting.DARK_RED);

        MutableComponent desc = Component.translatable("set_affix.fallen_gems_affixes.trickster_weapon.desc", dmgVal)
                .withStyle(ChatFormatting.YELLOW);

        if (isShiftDown()) {
            Component cooldownVal = Component.literal(SetAffix.fmt(cooldownTicks / 20f) + "s").withStyle(ChatFormatting.DARK_RED);
            desc.append(Component.translatable("set_affix.fallen_gems_affixes.trickster_weapon.desc.shift", cooldownVal)
                    .withStyle(ChatFormatting.GRAY));
        }

        return desc;
    }

    @Override
    public Component getBonusDescription(int threshold) {
        if (threshold == 3) {
            Component speedVal = Component.literal(SetAffix.fmt(threePieceSpeedPerClone * 100f) + "%").withStyle(ChatFormatting.DARK_RED);
            Component dodgeVal = Component.literal(SetAffix.fmt(threePieceDodgePerClone * 100f) + "%").withStyle(ChatFormatting.DARK_RED);
            return Component.translatable("set_bonus.fallen_gems_affixes.trickster.3", speedVal, dodgeVal);
        }
        if (threshold == 5) {
            Component chanceVal = Component.literal(SetAffix.fmt(fivePieceKillRefreshChance * 100f) + "%").withStyle(ChatFormatting.DARK_RED);
            MutableComponent desc = Component.translatable("set_bonus.fallen_gems_affixes.trickster.5", chanceVal);

            if (isShiftDown()) {
                int bonusClones = Fallen.Registries.SET_AFFIX_REGISTRY.getValues().stream()
                        .filter(a -> a instanceof TricksterChestplateAffix && this.setId.equals(a.getSetId()))
                        .map(a -> ((TricksterChestplateAffix) a).getFivePieceBonusClones())
                        .findFirst().orElse(2);
                Component clonesVal = Component.literal(String.valueOf(bonusClones)).withStyle(ChatFormatting.DARK_RED);
                desc.append(Component.translatable("set_bonus.fallen_gems_affixes.trickster.5.shift", clonesVal)
                        .withStyle(ChatFormatting.GRAY));
            }

            return desc;
        }
        return null;
    }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        if (cat.isNone()) return false;
        if (cat == LootCategory.SWORD || cat == LootCategory.HEAVY_WEAPON) return true;
        if (ModList.get().isLoaded("celestisynth")) {
            LootCategory celestialMelee = LootCategory.byId("celestial_melee");
            return celestialMelee != null && cat == celestialMelee;
        }
        return false;
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