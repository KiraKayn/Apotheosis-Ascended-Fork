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

import java.util.Map;
import java.util.Set;

public class PiercingArrowAffix extends Affix {

    public static final Codec<PiercingArrowAffix> CODEC = RecordCodecBuilder.create(inst ->
            inst.group(
                    LootRarity.mapCodec(StepFunction.CODEC).fieldOf("values").forGetter(a -> a.values),
                    LootCategory.SET_CODEC.fieldOf("types").forGetter(a -> a.types)
            ).apply(inst, PiercingArrowAffix::new));

    protected final Map<LootRarity, StepFunction> values;
    protected final Set<LootCategory> types;

    public PiercingArrowAffix(Map<LootRarity, StepFunction> values, Set<LootCategory> types) {
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
        return Component.translatable(
                "affix.fallen_gems_affixes.piercing_arrow.desc",
                getPierceLevel(rarity, level));
    }

    @Override
    public Component getAugmentingText(ItemStack stack, LootRarity rarity, float level) {
        MutableComponent comp = this.getDescription(stack, rarity, level);
        int min = getPierceLevel(rarity, 0);
        int max = getPierceLevel(rarity, 1);
        return comp.append(valueBounds(
                Component.literal(String.valueOf(min)),
                Component.literal(String.valueOf(max))));
    }

    /**
     * Returns the pierce level as an int. The StepFunction stores it as a float
     * but we always round down — pierce levels are discrete whole numbers.
     */
    public int getPierceLevel(LootRarity rarity, float level) {
        StepFunction f = this.values.get(rarity);
        return f != null ? (int) f.get(level) : 0;
    }

    @Override
    public void onArrowFired(ItemStack stack, LootRarity rarity, float level, LivingEntity user, AbstractArrow arrow) {
        int pierceLevel = getPierceLevel(rarity, level);
        if (pierceLevel > 0) {
            arrow.setPierceLevel((byte) Math.max(arrow.getPierceLevel(), pierceLevel));
        }
    }
}