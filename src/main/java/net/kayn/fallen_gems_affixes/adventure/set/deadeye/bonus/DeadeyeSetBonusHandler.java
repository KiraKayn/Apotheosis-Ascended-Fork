package net.kayn.fallen_gems_affixes.adventure.set.deadeye.bonus;

import dev.shadowsoffire.attributeslib.api.ALObjects;
import net.kayn.fallen_gems_affixes.adventure.set.SetAffix;
import net.kayn.fallen_gems_affixes.adventure.set.SetAffixHelper;
import net.kayn.fallen_gems_affixes.adventure.set.SetBonusHandler;
import net.kayn.fallen_gems_affixes.adventure.set.deadeye.*;
import net.kayn.fallen_gems_affixes.entity.SpiritEchoEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.UUID;

public class DeadeyeSetBonusHandler {
    private static final String PIECE_COUNT_KEY = "fga.deadeye_bonus_pieces";
    private static final UUID ARROW_DAMAGE_MODIFIER_UUID = UUID.fromString("d5f7b9c1-4567-89ab-cdef-012345678901");
    private static final UUID ARROW_VELOCITY_MODIFIER_UUID = UUID.fromString("d5f7b9c1-4567-89ab-cdef-012345678902");
    private static final UUID CRIT_DAMAGE_MODIFIER_UUID = UUID.fromString("c4e6a8b0-3456-789a-bcde-f01234567890");

    public static void onPieceCountChanged(Player player, int newCount) {
        int lastCount = player.getPersistentData().getInt(PIECE_COUNT_KEY);
        if (lastCount == newCount) return;
        player.getPersistentData().putInt(PIECE_COUNT_KEY, newCount);
    }

    public static int getMaxFocus(int pieces, DeadeyeChestplateAffix chestplate, DeadeyeLeggingsAffix leggings) {
        int max = chestplate != null ? chestplate.getMaxFocus() : DeadeyeSetConstants.BASE_MAX_FOCUS;
        if (pieces >= 4 && leggings != null) max += leggings.getFourPieceMaxFocusBonus();
        return max;
    }

    public static void updateFocus(Player player, int newFocus, int pieces, DeadeyeChestplateAffix chestplate, int maxFocus) {
        int clamped = Math.max(0, Math.min(newFocus, maxFocus));
        DeadeyeStateHelper.setFocus(player, clamped, maxFocus);
        refreshArrowAttributes(player, pieces, chestplate, clamped);
        sendFocusActionBar(player, clamped, maxFocus);
    }

    private static void refreshArrowAttributes(Player player, int pieces, DeadeyeChestplateAffix chestplate, int focus) {
        AttributeInstance dmgAttr = player.getAttribute(ALObjects.Attributes.ARROW_DAMAGE.get());
        if (dmgAttr != null) dmgAttr.removeModifier(ARROW_DAMAGE_MODIFIER_UUID);
        AttributeInstance velAttr = player.getAttribute(ALObjects.Attributes.ARROW_VELOCITY.get());
        if (velAttr != null) velAttr.removeModifier(ARROW_VELOCITY_MODIFIER_UUID);

        if (chestplate == null || focus <= 0) return;

        double dmgBonus = focus * chestplate.getArrowDamagePerFocus();
        if (pieces >= 2) dmgBonus += focus * chestplate.getTwoPieceArrowDamagePerFocus();
        if (dmgAttr != null && dmgBonus > 0) {
            dmgAttr.addTransientModifier(new AttributeModifier(ARROW_DAMAGE_MODIFIER_UUID,
                    "deadeye_arrow_damage", dmgBonus, AttributeModifier.Operation.MULTIPLY_BASE));
        }

        if (pieces >= 2) {
            double velBonus = focus * chestplate.getTwoPieceArrowVelocityPerFocus();
            if (velAttr != null && velBonus > 0) {
                velAttr.addTransientModifier(new AttributeModifier(ARROW_VELOCITY_MODIFIER_UUID,
                        "deadeye_arrow_velocity", velBonus, AttributeModifier.Operation.MULTIPLY_BASE));
            }
        }
    }

    public static void updateCritDamageAttribute(Player player, int stacks, float perStack) {
        AttributeInstance attr = player.getAttribute(ALObjects.Attributes.CRIT_DAMAGE.get());
        if (attr == null) return;
        attr.removeModifier(CRIT_DAMAGE_MODIFIER_UUID);
        if (stacks > 0) {
            attr.addTransientModifier(new AttributeModifier(CRIT_DAMAGE_MODIFIER_UUID,
                    "deadeye_crit_stacks", stacks * perStack, AttributeModifier.Operation.MULTIPLY_BASE));
        }
    }

    private static void sendFocusActionBar(Player player, int focus, int maxFocus) {
        if (player.level().isClientSide) return;
        player.displayClientMessage(Component.translatable("fga.deadeye.focus_display", focus, maxFocus)
                .withStyle(ChatFormatting.AQUA), true);
    }

    public static boolean shouldSkipConsumption(Player player, int pieces, int focusLevel, int maxFocus, DeadeyeBowAffix bow) {
        if (pieces < 3 || bow == null) return false;
        if (focusLevel < maxFocus) return false;
        return player.getRandom().nextFloat() < bow.getThreePieceNoConsumeChance();
    }

    public static int getThreePieceExecuteBonusFocus(int pieces, DeadeyeBowAffix bow) {
        if (pieces < 3 || bow == null) return 0;
        return bow.getThreePieceExecuteBonusFocus();
    }

    public static float applyFourPieceMarkDamage(int pieces, DeadeyeLeggingsAffix leggings, boolean marked, float amount) {
        if (pieces < 4 || leggings == null || !marked) return amount;
        return amount * (1.0F + leggings.getFourPieceMarkDamageBonus());
    }

    public static void applyFourPieceCooldownReduction(Player player, int pieces) {
        if (pieces < 4) return;
        SetAffix legAffix = SetAffixHelper.getSetAffix(player.getItemBySlot(EquipmentSlot.LEGS));
        if (!(legAffix instanceof DeadeyeLeggingsAffix leg)) return;

        int reduction = leg.getFourPieceCooldownReductionTicks();
        CompoundTag data = player.getPersistentData();
        String prefix = "apoth.affix_cooldown.";
        for (String key : data.getAllKeys()) {
            if (key.startsWith(prefix)) {
                long startTime = data.getLong(key);
                if (startTime > 0) data.putLong(key, Math.max(0, startTime - reduction));
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;
        if (SetBonusHandler.getSetPieceCount(player, DeadeyeSetConstants.SET_ID) > 0) return;
        if (DeadeyeStateHelper.getFocus(player) == 0 && DeadeyeStateHelper.getStacks(player) == 0) return;

        DeadeyeStateHelper.setFocus(player, 0, DeadeyeSetConstants.BASE_MAX_FOCUS);
        DeadeyeStateHelper.setStacks(player, 0);
        refreshArrowAttributes(player, 0, null, 0);
        updateCritDamageAttribute(player, 0, 0);
        sendFocusActionBar(player, 0, DeadeyeSetConstants.BASE_MAX_FOCUS);
    }

    @SubscribeEvent
    public static void onEnemyKilled(LivingDeathEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide) return;
        if (target instanceof SpiritEchoEntity) return;
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (!DeadeyeStateHelper.isMarkedBy(target, player)) return;

        int pieces = SetBonusHandler.getSetPieceCount(player, DeadeyeSetConstants.SET_ID);
        if (pieces < 5) return;

        SetAffix affix = SetAffixHelper.getSetAffix(player.getMainHandItem());
        if (!(affix instanceof DeadeyeBowAffix bow)) return;

        SpiritEchoEntity echo = new SpiritEchoEntity(DeadeyeEntities.SPIRIT_ECHO.get(), target.level());
        echo.setOwnerUUID(player.getUUID());
        echo.setPos(target.getX(), target.getY() + 2.0, target.getZ());
        echo.setLifetime(bow.getEchoDurationTicks());
        target.level().addFreshEntity(echo);
    }
}