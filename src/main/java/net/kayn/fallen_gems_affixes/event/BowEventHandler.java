package net.kayn.fallen_gems_affixes.event;

import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixInstance;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.adventure.affix.*;
import net.kayn.fallen_gems_affixes.attachment.augment.SpecialAffixEventHandler;
import net.kayn.fallen_gems_affixes.util.ArrowFireCache;
import net.kayn.fallen_gems_affixes.util.DelayedTaskScheduler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.rtxyd.fallen.lib.runtime.forgemod.util.GameLifecycleHelper;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class BowEventHandler {

    private static boolean firingExtraArrows = false;


    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onArrowLoose(ArrowLooseEvent event) {
        if (firingExtraArrows) return;
        if (event.getLevel().isClientSide()) return;
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;

        Player player = event.getEntity();
        ItemStack bow = event.getBow();
        float power   = BowItem.getPowerForTime(event.getCharge());
        if (power < 0.1f) return;

        int totalExtraShots = 0;
        boolean bypass      = false;

        for (var inst : AffixHelper.getAffixes(bow).values()) {
            if (!inst.isValid()) continue;
            if (inst.affix().get() instanceof MultiShotAffix affix) {
                totalExtraShots += affix.getExtraShots(inst.rarity().get(), inst.level());
                if (affix.isBypassIframes(inst.rarity().get())) bypass = true;
            }
        }
        if (totalExtraShots <= 0) return;

        final boolean bypassIframes = bypass;

        firingExtraArrows = true;
        try {
            for (int i = 1; i <= totalExtraShots; i++) {
                int   side      = (i % 2 == 1) ? 1 : -1;
                int   step      = (i + 1) / 2;
                float yawOffset = side * step * MultiShotAffix.SPREAD_DEGREES;

                Arrow extraArrow = new Arrow(serverLevel, player);
                extraArrow.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
                extraArrow.shootFromRotation(
                        player,
                        player.getXRot(),
                        player.getYRot() + yawOffset,
                        0.0f,
                        power * 3.0f,
                        1.0f);

                AffixHelper.copyFrom(bow, extraArrow);
                if (power == 1.0f) extraArrow.setCritArrow(true);
                extraArrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;

                if (bypassIframes) {
                    extraArrow.getPersistentData()
                            .putBoolean(MultiShotAffix.KEY_BYPASS_IFRAMES, true);
                }

                serverLevel.addFreshEntity(extraArrow);
            }
        } finally {
            firingExtraArrows = false;
        }

        serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS,
                1.0f, 1.0f / (serverLevel.getRandom().nextFloat() * 0.4f + 1.2f) + power * 0.5f);
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onArrowSpawn(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof AbstractArrow arrow)) return;

        arrow.getPersistentData().putDouble(MomentumAffix.KEY_ORIGIN_X, arrow.getX());
        arrow.getPersistentData().putDouble(MomentumAffix.KEY_ORIGIN_Y, arrow.getY());
        arrow.getPersistentData().putDouble(MomentumAffix.KEY_ORIGIN_Z, arrow.getZ());
        // clear and get the data record by AdventureEvents mixin
        ArrowFireCache cache = GameLifecycleHelper.callAndRemoveIfPresent(Fallen.ContextKeys.ARROW_FIRE_CACHE, GameLifecycleHelper.EMPTY_EX_CONSUMER);
        ItemStack bow = null;
        if (cache != null) {
            // directly trigger the bow affixes, so overwrite won't influence it.
            if (arrow == cache.getArrow() || arrow.getPersistentData().getBoolean(Fallen.Common.KEY_ARROW_FIRE_CACHE)) {
                arrow.getPersistentData().remove(Fallen.Common.KEY_ARROW_FIRE_CACHE);
                bow = cache.getBow();
            }
        }
        Map<DynamicHolder<? extends Affix>, AffixInstance> affixes = AffixHelper.getAffixes(arrow);
        if (bow != null) {
            affixes.putAll(SpecialAffixEventHandler.getToModifyAffixes(bow).getOutput());
        }
        for (AffixInstance inst : affixes.values()) {
            triggerBowAffixes(inst, arrow, event);
        }
    }

    private static void triggerBowAffixes(AffixInstance inst, AbstractArrow arrow, EntityJoinLevelEvent event) {
        if (!inst.isValid()) return;
        if (inst.affix().get() instanceof PiercingArrowAffix affix) {
            int pierceLevel = affix.getPierceLevel(inst.rarity().get(), inst.level());
            if (pierceLevel > 0) {
                arrow.setPierceLevel((byte) Math.max(arrow.getPierceLevel(), pierceLevel));
            }
        }

        if (inst.affix().get() instanceof ChainShotAffix affix) {
            arrow.getPersistentData().putFloat(ChainShotAffix.KEY_CACHED_RANGE, affix.getMaxRange());
        }

        if (inst.affix().get() instanceof TrueShotAffix) {
            arrow.getPersistentData().putBoolean(TrueShotAffix.KEY_TRUE_SHOT, true);
        }

        if (inst.affix().get() instanceof HomingAffix affix) {
            float turnRate = affix.getTurnRate(inst.rarity().get(), inst.level());
            if (turnRate > 0f) {
                arrow.getPersistentData().putFloat(HomingAffix.KEY_TURN_RATE, turnRate);
            }
            ServerLevel level = (ServerLevel) event.getLevel();
            repeatTickUntilDisable(level, arrow, 1);
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


    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onArrowHit(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getSource().getDirectEntity() instanceof AbstractArrow arrow)) return;

        LivingEntity target = event.getEntity();

        AffixHelper.streamAffixes(arrow).forEach(inst -> {
            if (!inst.isValid()) return;
            if (inst.affix().get() instanceof MomentumAffix affix) {
                float mult = affix.getDamageMultiplier(arrow, inst.rarity().get(), inst.level());
                if (mult > 1f) event.setAmount(event.getAmount() * mult);
            }
        });
        if (arrow.getPersistentData().getBoolean(MultiShotAffix.KEY_BYPASS_IFRAMES)) {
            int savedHurtTime   = target.hurtTime;
            int savedInvulnTime = target.invulnerableTime;
            target.hurtTime        = 0;
            target.invulnerableTime = 0;
            if (event.isCanceled()) {
                target.hurtTime        = savedHurtTime;
                target.invulnerableTime = savedInvulnTime;
            }
        }
        if (!arrow.getPersistentData().getBoolean(ChainShotAffix.KEY_CHAIN_ARROW)
                && arrow.getOwner() instanceof Player player) {
            player.getPersistentData().putFloat(
                    ChainShotAffix.KEY_LAST_ARROW_DAMAGE, event.getAmount());
        }

        if (arrow.getPersistentData().getBoolean(ChainShotAffix.KEY_CHAIN_ARROW)
                && arrow.getOwner() instanceof Player player) {
            float stored = player.getPersistentData()
                    .getFloat(ChainShotAffix.KEY_LAST_ARROW_DAMAGE);
            if (stored > 0f) event.setAmount(stored);
        }
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onEntityDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity().level() instanceof ServerLevel serverLevel)) return;
        if (!(event.getSource().getDirectEntity() instanceof AbstractArrow killingArrow)) return;

        if (killingArrow.getPersistentData().getBoolean(ChainShotAffix.KEY_CHAIN_ARROW)) return;

        if (!(killingArrow.getOwner() instanceof Player player)) return;

        if (!killingArrow.getPersistentData().contains(ChainShotAffix.KEY_CACHED_RANGE)) return;
        float maxRange = killingArrow.getPersistentData().getFloat(ChainShotAffix.KEY_CACHED_RANGE);
        if (maxRange <= 0f) return;

        LivingEntity deadEntity = event.getEntity();
        final float searchRange = maxRange;
        AABB searchBox = deadEntity.getBoundingBox().inflate(searchRange);
        List<LivingEntity> candidates = serverLevel.getEntitiesOfClass(
                LivingEntity.class,
                searchBox,
                e -> e != deadEntity
                        && e != player
                        && e.isAlive()
                        && !(e instanceof Player));

        if (candidates.isEmpty()) return;

        LivingEntity chainTarget = candidates.stream()
                .min(Comparator.comparingDouble(e -> e.distanceToSqr(deadEntity)))
                .orElse(null);
        if (chainTarget == null) return;

        Arrow chainArrow = new Arrow(serverLevel, player);
        if (killingArrow instanceof Arrow killingTyped) {
            CompoundTag killingNbt = killingTyped.serializeNBT();
            CompoundTag chainNbt   = chainArrow.serializeNBT();
            for (String key : List.of("Potion", "CustomPotionEffects", "CustomPotionColor")) {
                if (killingNbt.contains(key)) chainNbt.put(key, killingNbt.get(key).copy());
            }
            chainArrow.deserializeNBT(chainNbt);
        }
        chainArrow.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());

        double dx = chainTarget.getX() - player.getX();
        double dy = chainTarget.getY() + chainTarget.getBbHeight() * 0.5 - chainArrow.getY();
        double dz = chainTarget.getZ() - player.getZ();
        chainArrow.shoot(dx, dy, dz, 3.0f, 0f);

        chainArrow.setBaseDamage(1.0);

        CompoundTag killingData = killingArrow.getPersistentData();
        if (killingData.contains("affix_data")) {
            chainArrow.getPersistentData().put("affix_data", killingData.getCompound("affix_data").copy());
        }
        chainArrow.getPersistentData().putBoolean(ChainShotAffix.KEY_CHAIN_ARROW, true);
        chainArrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;

        serverLevel.addFreshEntity(chainArrow);

        serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 0.6f, 1.4f);
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