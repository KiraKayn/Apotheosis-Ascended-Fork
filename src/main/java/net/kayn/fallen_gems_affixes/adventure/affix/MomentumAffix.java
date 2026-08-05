package net.kayn.fallen_gems_affixes.adventure.affix;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixType;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.placebo.util.StepFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;

import java.util.Map;
import java.util.Set;

public class MomentumAffix extends Affix {

    public static final String KEY_ORIGIN_X = "fga:momentum_ox";
    public static final String KEY_ORIGIN_Y = "fga:momentum_oy";
    public static final String KEY_ORIGIN_Z = "fga:momentum_oz";

    public static final Codec<MomentumAffix> CODEC = RecordCodecBuilder.create(inst ->
            inst.group(
                    Codec.FLOAT.fieldOf("max_distance").forGetter(a -> a.maxDistance),
                    LootRarity.mapCodec(StepFunction.CODEC).fieldOf("values").forGetter(a -> a.values),
                    LootCategory.SET_CODEC.fieldOf("types").forGetter(a -> a.types)
            ).apply(inst, MomentumAffix::new));

    protected final float maxDistance;
    protected final Map<LootRarity, StepFunction> values;
    protected final Set<LootCategory> types;

    public MomentumAffix(float maxDistance, Map<LootRarity, StepFunction> values, Set<LootCategory> types) {
        super(AffixType.ABILITY);
        this.maxDistance = maxDistance;
        this.values      = values;
        this.types       = types;
    }

    @Override
    public Codec<? extends Affix> getCodec() { return CODEC; }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        if (!this.values.containsKey(rarity)) return false;
        return this.types.isEmpty() || this.types.contains(cat);
    }

    @Override
    public MutableComponent getDescription(ItemStack stack, LootRarity rarity, float level) {
        float maxBonus = this.getMaxBonus(rarity, level);
        return Component.translatable(
                "affix.fallen_gems_affixes.momentum.desc",
                fmt(maxBonus * 100),
                fmt(this.maxDistance));
    }

    @Override
    public Component getAugmentingText(ItemStack stack, LootRarity rarity, float level) {
        MutableComponent comp = this.getDescription(stack, rarity, level);
        float min = this.getMaxBonus(rarity, 0);
        float max = this.getMaxBonus(rarity, 1);
        return comp.append(valueBounds(
                Component.literal(fmt(min * 100) + "%"),
                Component.literal(fmt(max * 100) + "%")));
    }

    public float getMaxBonus(LootRarity rarity, float level) {
        StepFunction f = this.values.get(rarity);
        return f != null ? f.get(level) : 0f;
    }

    public float getDamageMultiplier(AbstractArrow arrow, LootRarity rarity, float level) {
        CompoundTag data = arrow.getPersistentData();
        if (!data.contains(KEY_ORIGIN_X)) return 1f;

        double ox = data.getDouble(KEY_ORIGIN_X);
        double oy = data.getDouble(KEY_ORIGIN_Y);
        double oz = data.getDouble(KEY_ORIGIN_Z);

        double dx = arrow.getX() - ox;
        double dy = arrow.getY() - oy;
        double dz = arrow.getZ() - oz;
        float  dist = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);

        float ratio    = Mth.clamp(dist / this.maxDistance, 0f, 1f);
        float maxBonus = this.getMaxBonus(rarity, level);

        return 1f + maxBonus * ratio;
    }

    @Override
    public void onArrowFired(ItemStack stack, LootRarity rarity, float level, LivingEntity user, AbstractArrow arrow) {
        arrow.getPersistentData().putDouble(MomentumAffix.KEY_ORIGIN_X, arrow.getX());
        arrow.getPersistentData().putDouble(MomentumAffix.KEY_ORIGIN_Y, arrow.getY());
        arrow.getPersistentData().putDouble(MomentumAffix.KEY_ORIGIN_Z, arrow.getZ());
    }

    @Override
    public void onArrowImpact(AbstractArrow arrow, LootRarity rarity, float level, HitResult res, HitResult.Type type) {
        float mult = getDamageMultiplier(arrow, rarity, level);
        if (mult > 1f) arrow.setBaseDamage(arrow.getBaseDamage() * mult);
    }
}