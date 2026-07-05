package net.kayn.fallen_gems_affixes.adventure.set.deadeye;

import dev.shadowsoffire.attributeslib.api.ALObjects;
import net.kayn.fallen_gems_affixes.adventure.set.SetAffix;
import net.kayn.fallen_gems_affixes.adventure.set.SetAffixHelper;
import net.kayn.fallen_gems_affixes.adventure.set.SetBonusHandler;
import net.kayn.fallen_gems_affixes.adventure.set.deadeye.bonus.DeadeyeSetBonusHandler;
import net.kayn.fallen_gems_affixes.entity.SpiritEchoEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

public class DeadeyeSetAffixEventHandler {
    private static final ThreadLocal<Boolean> IN_AOE_BURST = ThreadLocal.withInitial(() -> false);
    private static final int ROOT_SLOWNESS_AMPLIFIER = 9;

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRangedHit(LivingHurtEvent event) {
        if (event.isCanceled()) return;
        if (IN_AOE_BURST.get()) return;
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide) return;
        if (target instanceof SpiritEchoEntity) return;
        if (!(event.getSource().getDirectEntity() instanceof AbstractArrow arrow)) return;
        if (!(arrow.getOwner() instanceof Player player)) return;

        DeadeyeHelmetAffix helmet = getAffix(player, EquipmentSlot.HEAD, DeadeyeHelmetAffix.class);
        DeadeyeChestplateAffix chestplate = getAffix(player, EquipmentSlot.CHEST, DeadeyeChestplateAffix.class);
        DeadeyeLeggingsAffix leggings = getAffix(player, EquipmentSlot.LEGS, DeadeyeLeggingsAffix.class);
        DeadeyeBootsAffix boots = getAffix(player, EquipmentSlot.FEET, DeadeyeBootsAffix.class);
        DeadeyeBowAffix bow = getAffix(player, EquipmentSlot.MAINHAND, DeadeyeBowAffix.class);

        if (helmet == null && chestplate == null && leggings == null && boots == null && bow == null) return;

        int pieces = SetBonusHandler.getSetPieceCount(player, DeadeyeSetConstants.SET_ID);
        int maxFocus = DeadeyeSetBonusHandler.getMaxFocus(pieces, chestplate, leggings);
        int focusBefore = DeadeyeStateHelper.getFocus(player);
        float amount = event.getAmount();

        boolean marked = DeadeyeStateHelper.isMarkedBy(target, player);
        amount = DeadeyeSetBonusHandler.applyFourPieceMarkDamage(pieces, leggings, marked, amount);

        if (bow != null) {
            float thresholdFraction = bow.getExecuteBaseThreshold() + focusBefore * bow.getExecuteScalingPerFocus();
            if (target.getHealth() - amount <= target.getMaxHealth() * thresholdFraction) {
                executeTarget(event, target, player, bow, chestplate, leggings, pieces, focusBefore, maxFocus);
                return;
            }
        }

        int gain = 0;
        if (chestplate != null) {
            gain += chestplate.getFocusPerHit();
            if (amount >= target.getMaxHealth() * chestplate.getBigHitHealthThreshold()) gain += chestplate.getBigHitBonusFocus();
        }
        boolean isCrit = isRangedCrit(player, arrow);
        if (helmet != null && isCrit) gain += helmet.getFocusPerCrit();

        int focus = Math.min(maxFocus, focusBefore + gain);

        if (boots != null && focusBefore >= maxFocus && !DeadeyeSetBonusHandler.shouldSkipConsumption(player, pieces, focusBefore, maxFocus, bow)) {
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, boots.getRootDurationTicks(), ROOT_SLOWNESS_AMPLIFIER, false, false));
            focus = Math.max(0, focus - boots.getRootFocusCost());
            amount *= consumeLeggingsBonus(player, leggings);
            DeadeyeSetBonusHandler.applyFourPieceCooldownReduction(player, pieces);
        }

        if (helmet != null && isCrit && focus >= maxFocus && !DeadeyeSetBonusHandler.shouldSkipConsumption(player, pieces, focus, maxFocus, bow)) {
            float legBonus = consumeLeggingsBonus(player, leggings);
            doAoeBurst(target, player, amount * helmet.getCritBurstDamagePercent() * legBonus, helmet.getCritBurstRadius());
            DeadeyeSetBonusHandler.applyFourPieceCooldownReduction(player, pieces);
            focus = 0;
        }

        DeadeyeSetBonusHandler.updateFocus(player, focus, pieces, chestplate, maxFocus);
        DeadeyeStateHelper.markAction(player);

        if (leggings != null) {
            int stacks = Math.min(leggings.getMaxStacks(), DeadeyeStateHelper.getStacks(player) + 1);
            DeadeyeStateHelper.setStacks(player, stacks);
            DeadeyeStateHelper.markStackHit(player);
            DeadeyeSetBonusHandler.updateCritDamageAttribute(player, stacks, leggings.getCritDamagePerStack());
        }

        event.setAmount(amount);
    }

    private static void executeTarget(LivingHurtEvent event, LivingEntity target, Player player, DeadeyeBowAffix bow, DeadeyeChestplateAffix chestplate,
                                      DeadeyeLeggingsAffix leggings, int pieces, int focusBefore, int maxFocus) {
        event.setCanceled(true);
        boolean wasMarked = DeadeyeStateHelper.isMarkedBy(target, player);
        boolean skip = DeadeyeSetBonusHandler.shouldSkipConsumption(player, pieces, focusBefore, maxFocus, bow);

        target.setHealth(0.0F);
        target.die(player.level().damageSources().playerAttack(player));

        markNearby(target, player, bow.getExecuteMarkRadius(), bow.getMarkDurationTicks());

        int newFocus = skip ? focusBefore : 0;
        if (!skip && wasMarked) newFocus = Math.min(maxFocus, newFocus + bow.getExecuteMarkRefund());
        newFocus = Math.min(maxFocus, newFocus + DeadeyeSetBonusHandler.getThreePieceExecuteBonusFocus(pieces, bow));

        DeadeyeSetBonusHandler.updateFocus(player, newFocus, pieces, chestplate, maxFocus);
        DeadeyeStateHelper.markAction(player);

        if (!skip) {
            DeadeyeSetBonusHandler.applyFourPieceCooldownReduction(player, pieces);
            consumeLeggingsBonus(player, leggings);
        }
    }

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        if (!(event.getProjectile() instanceof AbstractArrow arrow)) return;
        if (!(arrow.getOwner() instanceof Player player)) return;
        if (player.level().isClientSide) return;

        HitResult res = event.getRayTraceResult();

        if (res.getType() == HitResult.Type.ENTITY) {
            if (((EntityHitResult) res).getEntity() instanceof SpiritEchoEntity echo) {
                SetAffix affix = SetAffixHelper.getSetAffix(player.getMainHandItem());
                if (affix instanceof DeadeyeBowAffix bow) {
                    event.setCanceled(true);
                    triggerEchoShot(player, echo, arrow, bow);
                }
            }
            return;
        }

        if (res.getType() != HitResult.Type.BLOCK) return;

        SetAffix legAffix = SetAffixHelper.getSetAffix(player.getItemBySlot(EquipmentSlot.LEGS));
        if (legAffix instanceof DeadeyeLeggingsAffix leg) {
            DeadeyeStateHelper.setStacks(player, 0);
            DeadeyeSetBonusHandler.updateCritDamageAttribute(player, 0, leg.getCritDamagePerStack());
        }
    }

    private static void triggerEchoShot(Player player, SpiritEchoEntity echo, AbstractArrow arrow, DeadeyeBowAffix bow) {
        int pieces = SetBonusHandler.getSetPieceCount(player, DeadeyeSetConstants.SET_ID);
        DeadeyeChestplateAffix chestplate = getAffix(player, EquipmentSlot.CHEST, DeadeyeChestplateAffix.class);
        DeadeyeLeggingsAffix leggings = getAffix(player, EquipmentSlot.LEGS, DeadeyeLeggingsAffix.class);
        int maxFocus = DeadeyeSetBonusHandler.getMaxFocus(pieces, chestplate, leggings);

        float damage = (float) (arrow.getBaseDamage() * bow.getEchoDamagePercent());
        AABB area = echo.getBoundingBox().inflate(bow.getEchoAoeRadius());
        List<LivingEntity> targets = echo.level().getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e.isAlive());

        try {
            IN_AOE_BURST.set(true);
            for (LivingEntity t : targets) {
                t.hurt(player.level().damageSources().indirectMagic(player, player), damage);
            }
        } finally {
            IN_AOE_BURST.set(false);
        }

        player.level().playSound(null, echo.getX(), echo.getY(), echo.getZ(),
                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.0F);

        int focus = DeadeyeStateHelper.getFocus(player);
        DeadeyeSetBonusHandler.updateFocus(player, focus + bow.getEchoFocusGrant(), pieces, chestplate, maxFocus);
        DeadeyeStateHelper.markAction(player);

        LivingEntity closest = findClosest(echo, targets);
        if (closest != null) DeadeyeStateHelper.applyMark(closest, player, bow.getMarkDurationTicks());

        if (echo.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SOUL, echo.getX(), echo.getY() + 1, echo.getZ(), 20, 0.5, 0.5, 0.5, 0.05);
        }

        echo.discard();
    }

    private static LivingEntity findClosest(SpiritEchoEntity echo, List<LivingEntity> candidates) {
        LivingEntity closest = null;
        double closestDist = Double.MAX_VALUE;
        for (LivingEntity e : candidates) {
            double d = e.distanceToSqr(echo);
            if (d < closestDist) {
                closestDist = d;
                closest = e;
            }
        }
        return closest;
    }

    @SubscribeEvent
    public static void onKnockback(LivingKnockBackEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;

        DeadeyeBootsAffix boots = getAffix(player, EquipmentSlot.FEET, DeadeyeBootsAffix.class);
        if (boots == null) return;

        int pieces = SetBonusHandler.getSetPieceCount(player, DeadeyeSetConstants.SET_ID);
        DeadeyeChestplateAffix chestplate = getAffix(player, EquipmentSlot.CHEST, DeadeyeChestplateAffix.class);
        DeadeyeLeggingsAffix leggings = getAffix(player, EquipmentSlot.LEGS, DeadeyeLeggingsAffix.class);
        int maxFocus = DeadeyeSetBonusHandler.getMaxFocus(pieces, chestplate, leggings);

        if (DeadeyeStateHelper.getFocus(player) >= maxFocus) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        if (player.level().isClientSide) return;
        if (player.tickCount % 20 != 0) return;

        int pieces = SetBonusHandler.getSetPieceCount(player, DeadeyeSetConstants.SET_ID);
        if (pieces <= 0) return;

        DeadeyeChestplateAffix chestplate = getAffix(player, EquipmentSlot.CHEST, DeadeyeChestplateAffix.class);
        DeadeyeLeggingsAffix leggings = getAffix(player, EquipmentSlot.LEGS, DeadeyeLeggingsAffix.class);
        long now = player.level().getGameTime();

        if (chestplate != null) {
            int focus = DeadeyeStateHelper.getFocus(player);
            if (focus > 0 && now - DeadeyeStateHelper.getLastActionTime(player) >= chestplate.getFocusDecayDelayTicks()) {
                int maxFocus = DeadeyeSetBonusHandler.getMaxFocus(pieces, chestplate, leggings);
                DeadeyeSetBonusHandler.updateFocus(player, focus - 1, pieces, chestplate, maxFocus);
            }
        }

        if (leggings != null) {
            int stacks = DeadeyeStateHelper.getStacks(player);
            if (stacks > 0 && now - DeadeyeStateHelper.getStacksLastHit(player) >= leggings.getStackResetDelayTicks()) {
                DeadeyeStateHelper.setStacks(player, 0);
                DeadeyeSetBonusHandler.updateCritDamageAttribute(player, 0, leggings.getCritDamagePerStack());
            }
        }
    }

    private static boolean isRangedCrit(Player player, AbstractArrow arrow) {
        if (arrow.isCritArrow()) return true;
        double critChance = player.getAttributeValue(ALObjects.Attributes.CRIT_CHANCE.get());
        return critChance > 0 && player.getRandom().nextFloat() < critChance;
    }

    private static void doAoeBurst(LivingEntity center, Player player, float damage, double radius) {
        AABB area = center.getBoundingBox().inflate(radius);
        List<LivingEntity> targets = center.level().getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e.isAlive() && !(e instanceof SpiritEchoEntity));
        try {
            IN_AOE_BURST.set(true);
            for (LivingEntity t : targets) {
                t.hurt(player.level().damageSources().indirectMagic(player, player), damage);
            }
        } finally {
            IN_AOE_BURST.set(false);
        }
        if (center.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SONIC_BOOM, center.getX(), center.getY() + 1, center.getZ(), 1, 0, 0, 0, 0);
        }
    }

    private static void markNearby(LivingEntity origin, Player player, double radius, int durationTicks) {
        AABB area = origin.getBoundingBox().inflate(radius);
        List<LivingEntity> nearby = origin.level().getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e.isAlive() && !(e instanceof SpiritEchoEntity));
        for (LivingEntity e : nearby) {
            DeadeyeStateHelper.applyMark(e, player, durationTicks);
        }
        if (origin.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CRIT, origin.getX(), origin.getY() + 1, origin.getZ(), 15, 0.5, 0.5, 0.5, 0.1);
        }
    }

    private static float consumeLeggingsBonus(Player player, DeadeyeLeggingsAffix leggings) {
        if (leggings == null) return 1.0F;
        int stacks = DeadeyeStateHelper.getStacks(player);
        if (stacks <= 0) return 1.0F;
        DeadeyeStateHelper.setStacks(player, 0);
        DeadeyeSetBonusHandler.updateCritDamageAttribute(player, 0, leggings.getCritDamagePerStack());
        return 1.0F + stacks * leggings.getConsumeBonusPerStack();
    }

    private static <T extends SetAffix> T getAffix(Player player, EquipmentSlot slot, Class<T> type) {
        SetAffix affix = SetAffixHelper.getSetAffix(player.getItemBySlot(slot));
        return type.isInstance(affix) ? type.cast(affix) : null;
    }
}