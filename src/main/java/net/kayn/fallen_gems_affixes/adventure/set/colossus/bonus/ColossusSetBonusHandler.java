package net.kayn.fallen_gems_affixes.adventure.set.colossus.bonus;

import net.kayn.fallen_gems_affixes.adventure.set.SetAffix;
import net.kayn.fallen_gems_affixes.adventure.set.SetAffixHelper;
import net.kayn.fallen_gems_affixes.adventure.set.SetBonusHandler;
import net.kayn.fallen_gems_affixes.adventure.set.colossus.*;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.UUID;

public class ColossusSetBonusHandler {

    private static final String PIECE_COUNT_KEY   = "fga.colossus_bonus_pieces";
    private static final String LAST_STAND_KEY    = "fga.colossus_last_stand_expires";
    private static final String ATSPD_EXPIRES_KEY = "fga.colossus_atspd_expires";

    private static final UUID ADAPTIVE_HEALTH_UUID = UUID.fromString("c0105505-b00b-0003-cafe-000000000003");
    private static final UUID ATSPD_UUID           = UUID.fromString("c0105505-b00b-0004-cafe-000000000004");

    private static final ThreadLocal<Boolean> ADDING_BONUS_ORB = ThreadLocal.withInitial(() -> false);

    public static void onPieceCountChanged(Player player, int newCount) {
        int last = player.getPersistentData().getInt(PIECE_COUNT_KEY);
        if (last == newCount) return;
        player.getPersistentData().putInt(PIECE_COUNT_KEY, newCount);
    }

    public static void sendOrbActionBar(Player player) {
        if (player.level().isClientSide) return;
        int orbs = BastionOrbManager.getOrbs(player);
        player.displayClientMessage(Component.translatable("fga.colossus.orb_display", orbs, ColossusSetConstants.MAX_ORBS)
                .withStyle(ChatFormatting.GOLD), true);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;
        if (SetBonusHandler.getSetPieceCount(player, ColossusSetConstants.SET_ID) > 0) return;
        if (BastionOrbManager.getOrbs(player) == 0) return;

        BastionOrbManager.setOrbsDirect(player, 0);
        BastionOrbManager.resetAccumulatedDamage(player);
        BastionOrbManager.setStandingTicks(player, 0);
        BastionOrbManager.setStandingBonusReady(player, false);
        removeAdaptiveVitality(player);

        AttributeInstance speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr != null) speedAttr.removeModifier(UUID.fromString("c0105505-b00b-0001-cafe-000000000001"));

        AttributeInstance atSpd = player.getAttribute(Attributes.ATTACK_SPEED);
        if (atSpd != null) atSpd.removeModifier(ATSPD_UUID);

        sendOrbActionBar(player);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        try {
            if (event.phase != TickEvent.Phase.END) return;
            Player player = event.player;
            if (player.level().isClientSide) return;

            int playerOffset = Math.abs(player.getUUID().hashCode()) % 20;
            if (player.tickCount % 20 != playerOffset) return;

            int pieces = player.getPersistentData().getInt(PIECE_COUNT_KEY);
            if (pieces < 2) {
                removeAdaptiveVitality(player);
                return;
            }

            pieces = SetBonusHandler.getSetPieceCount(player, ColossusSetConstants.SET_ID);

            if (pieces >= 2) {
                updateAdaptiveVitality(player);
            } else {
                removeAdaptiveVitality(player);
            }

            long atspdExpires = player.getPersistentData().getLong(ATSPD_EXPIRES_KEY);
            if (player.level().getGameTime() >= atspdExpires) {
                AttributeInstance atSpd = player.getAttribute(Attributes.ATTACK_SPEED);
                if (atSpd != null) {
                    atSpd.removeModifier(ATSPD_UUID);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @SubscribeEvent
    public static void onEffectApplicable(MobEffectEvent.Applicable event) {
        try {
            if (!(event.getEntity() instanceof Player player)) return;
            if (player.level().isClientSide) return;

            int pieces = SetBonusHandler.getSetPieceCount(player, ColossusSetConstants.SET_ID);

            if (pieces >= 5 && BastionOrbManager.isUnstoppable(player)) {
                if (!event.getEffectInstance().getEffect().isBeneficial()) {
                    event.setResult(net.minecraftforge.eventbus.api.Event.Result.DENY);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onDeath(LivingDeathEvent event) {
        try {
            if (!(event.getEntity() instanceof Player player)) return;
            if (player.level().isClientSide) return;

            int pieces = SetBonusHandler.getSetPieceCount(player, ColossusSetConstants.SET_ID);
            if (pieces < 3) return;
            if (!BastionOrbManager.isUnstoppable(player)) return;

            long expires = player.getPersistentData().getLong(LAST_STAND_KEY);
            if (player.level().getGameTime() < expires) return;

            SetAffix chestAffix = SetAffixHelper.getSetAffix(player.getItemBySlot(EquipmentSlot.CHEST));
            if (!(chestAffix instanceof ColossusChestplateAffix ca)) return;

            event.setCanceled(true);
            player.setHealth(1.0f);
            BastionOrbManager.consumeOrbs(player, ColossusSetConstants.MAX_ORBS);
            BastionOrbManager.resetAccumulatedDamage(player);

            ColossusShockwaveHelper.releaseMega(player, ca.getLastStandShockwaveDamage(),
                    ca.getLastStandShockwaveRadius(), player.level());

            player.invulnerableTime = ca.getLastStandInvulnTicks();
            player.getPersistentData().putLong(LAST_STAND_KEY,
                    player.level().getGameTime() + ca.getLastStandCooldownTicks());

            sendOrbActionBar(player);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @SubscribeEvent
    public static void onOrbGained(BastionOrbGainedEvent event) {
        try {
            Player player = event.getPlayer();
            if (player.level().isClientSide) return;

            int pieces = SetBonusHandler.getSetPieceCount(player, ColossusSetConstants.SET_ID);

            if (pieces >= 4) applyResonance(player);

            if (pieces >= 5 && !ADDING_BONUS_ORB.get()) {
                SetAffix shieldAffix = SetAffixHelper.getSetAffix(player.getItemBySlot(EquipmentSlot.OFFHAND));
                if (shieldAffix instanceof ColossusShieldAffix sa) {
                    if (player.getRandom().nextFloat() < sa.getFivePieceOrbGenBonus()) {
                        ADDING_BONUS_ORB.set(true);
                        try {
                            BastionOrbManager.addOrbs(player, 1);
                        } finally {
                            ADDING_BONUS_ORB.set(false);
                        }
                    }
                }
            }

            sendOrbActionBar(player);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @SubscribeEvent
    public static void onOrbConsumed(BastionOrbConsumedEvent event) {
        try {
            Player player = event.getPlayer();
            if (player.level().isClientSide) return;

            int pieces = SetBonusHandler.getSetPieceCount(player, ColossusSetConstants.SET_ID);
            if (pieces >= 4) applyResonance(player);

            sendOrbActionBar(player);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static void applyResonance(Player player) {
        SetAffix legsAffix = SetAffixHelper.getSetAffix(player.getItemBySlot(EquipmentSlot.LEGS));
        if (!(legsAffix instanceof ColossusLeggingsAffix la)) return;

        player.heal(la.getResonanceHealFraction() * (float) player.getAttributeValue(Attributes.MAX_HEALTH));

        if (player.level() instanceof ServerLevel sl) {
            sl.sendParticles(net.minecraft.core.particles.ParticleTypes.HEART,
                    player.getX(), player.getY() + 1.5, player.getZ(), 3, 0.3, 0.3, 0.3, 0.05);
        }

        AttributeInstance atSpd = player.getAttribute(Attributes.ATTACK_SPEED);
        if (atSpd != null) {
            atSpd.removeModifier(ATSPD_UUID);
            atSpd.addTransientModifier(new AttributeModifier(ATSPD_UUID,
                    "colossus_resonance", la.getResonanceAttackSpeedBoost(), AttributeModifier.Operation.MULTIPLY_BASE));
            player.getPersistentData().putLong(ATSPD_EXPIRES_KEY,
                    player.level().getGameTime() + la.getResonanceAttackSpeedDurationTicks());
        }

        int reduction = la.getResonanceCooldownReductionTicks();

        CompoundTag data = player.getPersistentData();
        String prefix = "apoth.affix_cooldown.";
        for (String key : data.getAllKeys()) {
            if (key.startsWith(prefix)) {
                long startTime = data.getLong(key);
                if (startTime > 0) {
                    data.putLong(key, Math.max(0, startTime - reduction));
                }
            }
        }

        ColossusCooldownHelper.reduceAllCooldowns(reduction, player);
    }

    private static void updateAdaptiveVitality(Player player) {
        SetAffix chestAffix = SetAffixHelper.getSetAffix(player.getItemBySlot(EquipmentSlot.CHEST));
        if (!(chestAffix instanceof ColossusChestplateAffix ca)) {
            removeAdaptiveVitality(player);
            return;
        }

        AttributeInstance maxHP = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHP == null) return;

        maxHP.removeModifier(ADAPTIVE_HEALTH_UUID);

        int orbs = BastionOrbManager.getOrbs(player);
        if (orbs == 0) return;

        float baseMax = (float) player.getAttributeValue(Attributes.MAX_HEALTH);
        float currentHP = player.getHealth();
        float missingHP = Math.max(0, baseMax - currentHP);

        float bonus = ca.getAdaptiveVitalityFraction() * orbs * missingHP;
        if (bonus > 0.1f) {
            maxHP.addTransientModifier(new AttributeModifier(ADAPTIVE_HEALTH_UUID,
                    "colossus_adaptive", bonus, AttributeModifier.Operation.ADDITION));
        }
    }

    private static void removeAdaptiveVitality(Player player) {
        AttributeInstance maxHP = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHP != null) maxHP.removeModifier(ADAPTIVE_HEALTH_UUID);
    }
}