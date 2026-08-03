package net.kayn.fallen_gems_affixes.adventure.affix;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixType;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.placebo.util.StepFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;

import java.util.Map;
import java.util.Set;

public class HomingAffix extends Affix {

    public static final float SEARCH_RANGE = 20f;

    public static final String KEY_TURN_RATE = "fga:homing_turn_rate";
    public static final String KEY_DISABLE = "fag:homing_disable";

    public static final Codec<HomingAffix> CODEC = RecordCodecBuilder.create(inst ->
            inst.group(
                    LootRarity.mapCodec(StepFunction.CODEC).fieldOf("values").forGetter(a -> a.values),
                    LootCategory.SET_CODEC.fieldOf("types").forGetter(a -> a.types)
            ).apply(inst, HomingAffix::new));

    protected final Map<LootRarity, StepFunction> values;
    protected final Set<LootCategory> types;

    public HomingAffix(Map<LootRarity, StepFunction> values, Set<LootCategory> types) {
        super(AffixType.ABILITY);
        this.values = values;
        this.types  = types;
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
        float turnRate = getTurnRate(rarity, level);
        return Component.translatable(
                "affix.fallen_gems_affixes.homing.desc",
                fmt(turnRate * 100));
    }

    @Override
    public Component getAugmentingText(ItemStack stack, LootRarity rarity, float level) {
        MutableComponent comp = this.getDescription(stack, rarity, level);
        float min = getTurnRate(rarity, 0);
        float max = getTurnRate(rarity, 1);
        return comp.append(valueBounds(
                Component.literal(fmt(min * 100) + "%"),
                Component.literal(fmt(max * 100) + "%")));
    }

    public float getTurnRate(LootRarity rarity, float level) {
        StepFunction f = this.values.get(rarity);
        return f != null ? f.get(level) : 0f;
    }

    @Override
    public void onArrowFired(ItemStack stack, LootRarity rarity, float level, LivingEntity user, AbstractArrow arrow) {
        arrow.getPersistentData().putBoolean(KEY_DISABLE, false);
    }

    @Override
    public void onArrowImpact(AbstractArrow arrow, LootRarity rarity, float level, HitResult res, HitResult.Type type) {
        arrow.getPersistentData().putBoolean(KEY_DISABLE, true);
    }
}