package net.kayn.fallen_gems_affixes.adventure.affix;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixType;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.placebo.util.StepFunction;
import net.kayn.fallen_gems_affixes.util.DelayedTaskScheduler;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.Comparator;
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
        float turnRate = getTurnRate(rarity, level);
        if (turnRate > 0f) {
            arrow.getPersistentData().putFloat(HomingAffix.KEY_TURN_RATE, turnRate);
        }
        if (user.level() instanceof ServerLevel serverLevel) {
            repeatTickUntilDisable(serverLevel, arrow, 1);
        }
    }

    private static void repeatTickUntilDisable(ServerLevel level, AbstractArrow arrow, int tickCount) {
        DelayedTaskScheduler.schedule(level, 1, () -> {
            if (arrow.isAlive() && !arrow.getPersistentData().getBoolean(HomingAffix.KEY_DISABLE)) {
                if (tickCount < 200 && tickHoming(arrow, level)) {
                    repeatTickUntilDisable(level, arrow, tickCount + 1);
                }
            }
        });
    }

    @Override
    public void onArrowImpact(AbstractArrow arrow, LootRarity rarity, float level, HitResult res, HitResult.Type type) {
        arrow.getPersistentData().putBoolean(KEY_DISABLE, true);
    }

    private static boolean tickHoming(AbstractArrow arrow, ServerLevel level) {
        float turnRate = arrow.getPersistentData().getFloat(HomingAffix.KEY_TURN_RATE);
        if (turnRate <= 0f) return false;

        AABB box = arrow.getBoundingBox().inflate(HomingAffix.SEARCH_RANGE);
        LivingEntity target = level.getEntitiesOfClass(LivingEntity.class, box,
                        e -> e.isAlive()
                                && !(e instanceof Player)
                                && e != arrow.getOwner())
                .stream()
                .min(Comparator.comparingDouble(e -> e.distanceToSqr(arrow)))
                .orElse(null);

        if (target == null) return true;

        Vec3 vel = arrow.getDeltaMovement();
        Vec3 horizontalVel = new Vec3(vel.x, 0, vel.z);
        double horizontalSpeed = horizontalVel.length();
        if (horizontalSpeed < 1e-6) return false;

        Vec3 toTarget = new Vec3(
                target.getX() - arrow.getX(),
                0,
                target.getZ() - arrow.getZ()
        ).normalize();

        Vec3 newHorizontal = horizontalVel.normalize()
                .lerp(toTarget, turnRate)
                .normalize()
                .scale(horizontalSpeed);

        Vec3 newVel = new Vec3(
                newHorizontal.x,
                vel.y,
                newHorizontal.z
        );

        arrow.setDeltaMovement(newVel);
        arrow.setYRot((float) (Math.toDegrees(Math.atan2(-newVel.x, newVel.z))));
        arrow.setXRot((float) (Math.toDegrees(Math.atan2(-newVel.y,
                Math.sqrt(newVel.x * newVel.x + newVel.z * newVel.z)))));
        return true;
    }
}