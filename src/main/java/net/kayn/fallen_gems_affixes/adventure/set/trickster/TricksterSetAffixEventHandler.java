package net.kayn.fallen_gems_affixes.adventure.set.trickster;

import net.kayn.fallen_gems_affixes.adventure.set.SetAffix;
import net.kayn.fallen_gems_affixes.adventure.set.SetAffixHelper;
import net.kayn.fallen_gems_affixes.adventure.set.trickster.bonus.TricksterSetBonusHandler;
import net.kayn.fallen_gems_affixes.entity.ShadowCloneEntity;
import net.kayn.fallen_gems_affixes.event.PlayerCriticalHitEvent;
import net.kayn.fallen_gems_affixes.event.ShadowCloneDeathEvent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

public class TricksterSetAffixEventHandler {
    private static final ThreadLocal<Boolean> CLONE_STRIKING = ThreadLocal.withInitial(() -> false);

    @SubscribeEvent
    public static void onCriticalHit(PlayerCriticalHitEvent event) {
        Player player = event.getPlayer();
        if (player.level().isClientSide) return;

        SetAffix affix = SetAffixHelper.getSetAffix(player.getItemBySlot(EquipmentSlot.HEAD));
        if (!(affix instanceof TricksterHelmetAffix helmetAffix)) return;

        LivingEntity target = event.getTarget();
        List<ShadowCloneEntity> clones = ShadowCloneManager.getClones(player);

        if (!clones.isEmpty()) {
            float strikeDamage = event.getDamage() * helmetAffix.getCloneStrikeDamageFraction();
            for (ShadowCloneEntity clone : clones) {
                clone.strikeTarget(target, strikeDamage);
            }
        } else {
            if (player.getRandom().nextFloat() < helmetAffix.getSpawnChance()) {
                ShadowCloneManager.spawnClonesAtCircle(player, 1);
                TricksterSetBonusHandler.sendCloneActionBar(player);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onPlayerTakeDamage(LivingHurtEvent event) {
        if (event.isCanceled()) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;

        SetAffix affix = SetAffixHelper.getSetAffix(player.getItemBySlot(EquipmentSlot.CHEST));
        if (!(affix instanceof TricksterChestplateAffix chestplateAffix)) return;

        int cloneCount = ShadowCloneManager.getCloneCount(player);

        if (cloneCount > 0) {
            float reduced = event.getAmount() * (1.0f - chestplateAffix.getDamageReduction());
            event.setAmount(reduced);
        }

        if (!TricksterCooldownHelper.isOnCooldown(player, TricksterCooldownHelper.CHESTPLATE_CD)) {
            int pieces = net.kayn.fallen_gems_affixes.adventure.set.SetBonusHandler.getSetPieceCount(player, TricksterSetConstants.SET_ID);
            int maxClones = chestplateAffix.getMaxClones() + (pieces >= 5 ? chestplateAffix.getFivePieceBonusClones() : 0);
            int toSpawn = Math.max(0, maxClones - cloneCount);
            if (toSpawn > 0) {
                ShadowCloneManager.spawnClonesAtCircle(player, toSpawn);
                TricksterCooldownHelper.setCooldown(player, TricksterCooldownHelper.CHESTPLATE_CD, chestplateAffix.getCooldownTicks());
                TricksterSetBonusHandler.sendCloneActionBar(player);
            }
        }
    }

    @SubscribeEvent
    public static void onCloneDeath(ShadowCloneDeathEvent event) {
        Player owner = event.getOwner();
        if (owner == null || owner.level().isClientSide) return;

        SetAffix affix = SetAffixHelper.getSetAffix(owner.getItemBySlot(EquipmentSlot.LEGS));
        if (affix instanceof TricksterLeggingsAffix leggingsAffix) {
            ShadowCloneEntity clone = event.getClone();
            float damage = (float) (owner.getAttributeValue(Attributes.ATTACK_DAMAGE) * leggingsAffix.getExplosionMultiplier());
            double radius = leggingsAffix.getRadius();
            AABB area = clone.getBoundingBox().inflate(radius);

            List<LivingEntity> targets = owner.level().getEntitiesOfClass(LivingEntity.class, area,
                    e -> e != owner && !(e instanceof ShadowCloneEntity) && !e.isDeadOrDying());

            for (LivingEntity target : targets) {
                target.hurt(owner.level().damageSources().magic(), damage);
            }
        }

        TricksterSetBonusHandler.sendCloneActionBar(owner);
    }

    @SubscribeEvent
    public static void onPlayerTick(net.minecraftforge.event.TickEvent.PlayerTickEvent event) {
        if (event.phase != net.minecraftforge.event.TickEvent.Phase.END) return;
        Player player = event.player;
        if (player.level().isClientSide) return;

        if (!player.isCrouching()) return;

        SetAffix affix = SetAffixHelper.getSetAffix(player.getItemBySlot(EquipmentSlot.FEET));
        if (!(affix instanceof TricksterBootsAffix bootsAffix)) return;

        if (TricksterCooldownHelper.isOnCooldown(player, TricksterCooldownHelper.BOOTS_CD)) return;

        checkBootsSwap(player, bootsAffix);
    }

    private static void checkBootsSwap(Player player, TricksterBootsAffix bootsAffix) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle();
        Vec3 endPos = eyePos.add(lookVec.scale(8.0));
        AABB searchBox = new AABB(eyePos, endPos).inflate(1.0);

        List<ShadowCloneEntity> clones = player.level().getEntitiesOfClass(ShadowCloneEntity.class, searchBox,
                c -> !c.isRemoved() && player.getUUID().equals(c.getOwnerUUID().orElse(null)));

        ShadowCloneEntity target = null;
        double closest = Double.MAX_VALUE;
        for (ShadowCloneEntity clone : clones) {
            var hit = clone.getBoundingBox().clip(eyePos, endPos);
            if (hit.isPresent()) {
                double d = hit.get().distanceTo(eyePos);
                if (d < closest) {
                    closest = d;
                    target = clone;
                }
            }
        }
        if (target == null) return;

        Vec3 clonePos = target.position();
        target.fireDeathEvent();
        target.discard();
        player.teleportTo(clonePos.x, clonePos.y, clonePos.z);
        TricksterCooldownHelper.setCooldown(player, TricksterCooldownHelper.BOOTS_CD, bootsAffix.getCooldownTicks());
        TricksterSetBonusHandler.sendCloneActionBar(player);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerHitEnemy(LivingHurtEvent event) {
        if (event.isCanceled()) return;
        if (CLONE_STRIKING.get()) return;
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;

        SetAffix affix = SetAffixHelper.getSetAffix(player.getMainHandItem());
        if (!(affix instanceof TricksterWeaponAffix weaponAffix)) return;

        if (TricksterCooldownHelper.isOnCooldown(player, TricksterCooldownHelper.WEAPON_CD)) return;

        List<ShadowCloneEntity> clones = ShadowCloneManager.getClones(player);
        if (clones.isEmpty()) return;

        LivingEntity victim = event.getEntity();
        float cloneDamage = (float) (player.getAttributeValue(Attributes.ATTACK_DAMAGE) * weaponAffix.getDamageMultiplier());

        try {
            CLONE_STRIKING.set(true);
            for (ShadowCloneEntity clone : clones) {
                victim.hurt(victim.level().damageSources().mobAttack(clone), cloneDamage);
            }
        } finally {
            CLONE_STRIKING.set(false);
        }

        TricksterCooldownHelper.setCooldown(player, TricksterCooldownHelper.WEAPON_CD, weaponAffix.getCooldownTicks());
    }
}