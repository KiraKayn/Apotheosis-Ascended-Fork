package net.kayn.fallen_gems_affixes.adventure.set.trickster.bonus;

import net.kayn.fallen_gems_affixes.adventure.set.SetAffix;
import net.kayn.fallen_gems_affixes.adventure.set.SetAffixHelper;
import net.kayn.fallen_gems_affixes.adventure.set.SetBonusHandler;
import net.kayn.fallen_gems_affixes.adventure.set.trickster.*;
import net.kayn.fallen_gems_affixes.event.ShadowCloneDeathEvent;
import dev.shadowsoffire.attributeslib.api.ALObjects;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.UUID;

public class TricksterSetBonusHandler {
    private static final String PIECE_COUNT_KEY = "fga.trickster_bonus_pieces";
    private static final UUID SPEED_MODIFIER_UUID  = UUID.fromString("a2c4b6d8-1234-5678-9abc-def012345678");
    private static final UUID DODGE_MODIFIER_UUID  = UUID.fromString("a2c4b6d8-1234-5678-9abc-def012345679");

    public static void onPieceCountChanged(Player player, int newCount) {
        int lastCount = player.getPersistentData().getInt(PIECE_COUNT_KEY);
        if (lastCount == newCount) return;
        player.getPersistentData().putInt(PIECE_COUNT_KEY, newCount);
    }

    public static void sendCloneActionBar(Player player) {
        if (player.level().isClientSide) return;
        int pieces = SetBonusHandler.getSetPieceCount(player, TricksterSetConstants.SET_ID);
        if (pieces < 1) return;
        SetAffix chestAffix = SetAffixHelper.getSetAffix(player.getItemBySlot(EquipmentSlot.CHEST));
        if (!(chestAffix instanceof TricksterChestplateAffix ca)) return;
        int maxClones = ca.getMaxClones() + (pieces >= 5 ? ca.getFivePieceBonusClones() : 0);
        int clones = ShadowCloneManager.getCloneCount(player);
        player.displayClientMessage(Component.translatable("fga.trickster.clone_display", clones, maxClones)
                .withStyle(ChatFormatting.LIGHT_PURPLE), true);
    }

    @SubscribeEvent
    public static void onPlayerTick(net.minecraftforge.event.TickEvent.PlayerTickEvent event) {
        if (event.phase != net.minecraftforge.event.TickEvent.Phase.END) return;
        Player player = event.player;
        if (player.level().isClientSide) return;
        if (player.tickCount % 20 != 0) return;

        int pieces = SetBonusHandler.getSetPieceCount(player, TricksterSetConstants.SET_ID);
        if (pieces < 3) {
            removeModifiers(player);
            return;
        }

        SetAffix weaponAffix = SetAffixHelper.getSetAffix(player.getMainHandItem());
        if (!(weaponAffix instanceof TricksterWeaponAffix wa)) {
            removeModifiers(player);
            return;
        }

        int clones = ShadowCloneManager.getCloneCount(player);
        if (clones <= 0) {
            removeModifiers(player);
            return;
        }

        AttributeInstance speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr != null) {
            speedAttr.removeModifier(SPEED_MODIFIER_UUID);
            double speedBonus = clones * wa.getThreePieceSpeedPerClone();
            speedAttr.addTransientModifier(new AttributeModifier(SPEED_MODIFIER_UUID,
                    "trickster_set_speed", speedBonus, AttributeModifier.Operation.MULTIPLY_BASE));
        }

        AttributeInstance dodgeAttr = player.getAttribute(ALObjects.Attributes.DODGE_CHANCE.get());
        if (dodgeAttr != null) {
            dodgeAttr.removeModifier(DODGE_MODIFIER_UUID);
            double dodgeBonus = clones * wa.getThreePieceDodgePerClone();
            dodgeAttr.addTransientModifier(new AttributeModifier(DODGE_MODIFIER_UUID,
                    "trickster_set_dodge", dodgeBonus, AttributeModifier.Operation.ADDITION));
        }
    }

    private static void removeModifiers(Player player) {
        AttributeInstance speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr != null) speedAttr.removeModifier(SPEED_MODIFIER_UUID);

        AttributeInstance dodgeAttr = player.getAttribute(ALObjects.Attributes.DODGE_CHANCE.get());
        if (dodgeAttr != null) dodgeAttr.removeModifier(DODGE_MODIFIER_UUID);
    }

    @SubscribeEvent
    public static void onCloneDeath(ShadowCloneDeathEvent event) {
        Player owner = event.getOwner();
        if (owner == null || owner.level().isClientSide) return;

        int pieces = SetBonusHandler.getSetPieceCount(owner, TricksterSetConstants.SET_ID);
        if (pieces < 4) return;

        SetAffix leggingsAffix = SetAffixHelper.getSetAffix(owner.getItemBySlot(EquipmentSlot.LEGS));
        if (!(leggingsAffix instanceof TricksterLeggingsAffix la)) return;

        int reduction = la.getFourPieceCooldownReductionTicks();

        CompoundTag data = owner.getPersistentData();
        String prefix = "apoth.affix_cooldown.";
        for (String key : data.getAllKeys()) {
            if (key.startsWith(prefix)) {
                long startTime = data.getLong(key);
                if (startTime > 0) {
                    data.putLong(key, Math.max(0, startTime - reduction));
                }
            }
        }

        String[] tricksterCooldownKeys = {
                TricksterCooldownHelper.CHESTPLATE_CD,
                TricksterCooldownHelper.BOOTS_CD,
                TricksterCooldownHelper.WEAPON_CD
        };

        for (String key : tricksterCooldownKeys) {
            if (TricksterCooldownHelper.isOnCooldown(owner, key)) {
                TricksterCooldownHelper.reduceCooldown(owner, key, reduction);
            }
        }
    }

    @SubscribeEvent
    public static void onKill(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;

        int pieces = SetBonusHandler.getSetPieceCount(player, TricksterSetConstants.SET_ID);
        if (pieces < 5) return;

        SetAffix wa = SetAffixHelper.getSetAffix(player.getMainHandItem());
        if (!(wa instanceof TricksterWeaponAffix weaponAffix)) return;

        if (player.getRandom().nextFloat() < weaponAffix.getFivePieceKillRefreshChance()) {
            TricksterCooldownHelper.clearCooldown(player, TricksterCooldownHelper.CHESTPLATE_CD);
        }
    }
}