package net.kayn.fallen_gems_affixes.adventure.set;

import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.HitResult;
import net.rtxyd.fallen.lib.runtime.forgemod.network.BoundHolder;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.BiConsumer;

public record SetAffixInstance(BoundHolder<SetAffix> affix, ItemStack stack, DynamicHolder<LootRarity> rarity, float level) {

    public boolean isValid() {
        return this.affix.isValid() && this.rarity.isBound();
    }

    public SetAffix afx() {
        return this.affix.get();
    }

    public LootRarity rty() {
        return this.rarity.get();
    }

    public void addModifiers(EquipmentSlot type, BiConsumer<Attribute, AttributeModifier> map) {
        if (this.isValid()) this.afx().addModifiers(this.stack, this.rty(), this.level, type, map);
    }

    public MutableComponent getDescription() {
        return this.isValid() ? this.afx().getDescription(this.stack, this.rty(), this.level) : Component.empty();
    }

    public Component getAugmentingText() {
        return this.isValid() ? this.afx().getAugmentingText(this.stack, this.rty(), this.level) : Component.empty();
    }

    public Component getName(boolean prefix) {
        return this.isValid() ? this.afx().getName(prefix) : Component.empty();
    }

    public int getDamageProtection(DamageSource source) {
        return this.isValid() ? this.afx().getDamageProtection(this.stack, this.rty(), this.level, source) : 0;
    }

    public float getDamageBonus(MobType creatureType) {
        return this.isValid() ? this.afx().getDamageBonus(this.stack, this.rty(), this.level, creatureType) : 0.0F;
    }

    public void doPostAttack(LivingEntity user, @Nullable Entity target) {
        if (this.isValid()) this.afx().doPostAttack(this.stack, this.rty(), this.level, user, target);
    }

    public void doPostHurt(LivingEntity user, @Nullable Entity attacker) {
        if (this.isValid()) this.afx().doPostHurt(this.stack, this.rty(), this.level, user, attacker);
    }

    public void onArrowFired(LivingEntity user, AbstractArrow arrow) {
        if (this.isValid()) this.afx().onArrowFired(this.stack, this.rty(), this.level, user, arrow);
    }

    @Nullable
    public InteractionResult onItemUse(UseOnContext ctx) {
        return this.isValid() ? this.afx().onItemUse(this.stack, this.rty(), this.level, ctx) : null;
    }

    public float onShieldBlock(LivingEntity entity, DamageSource source, float amount) {
        return this.isValid() ? this.afx().onShieldBlock(this.stack, this.rty(), this.level, entity, source, amount) : amount;
    }

    public void onBlockBreak(Player player, LevelAccessor world, BlockPos pos, BlockState state) {
        if (this.isValid()) this.afx().onBlockBreak(this.stack, this.rty(), this.level, player, world, pos, state);
    }

    public float getDurabilityBonusPercentage(@Nullable ServerPlayer user) {
        return this.isValid() ? this.afx().getDurabilityBonusPercentage(this.stack, this.rty(), this.level, user) : 0.0F;
    }

    public void onArrowImpact(AbstractArrow arrow, HitResult res, HitResult.Type type) {
        if (this.isValid()) this.afx().onArrowImpact(arrow, this.rty(), this.level, res, type);
    }

    public boolean enablesTelepathy() {
        return this.isValid() && this.afx().enablesTelepathy();
    }

    public float onHurt(DamageSource src, LivingEntity ent, float amount) {
        return this.isValid() ? this.afx().onHurt(this.stack, this.rty(), this.level, src, ent, amount) : amount;
    }

    public void getEnchantmentLevels(Map<Enchantment, Integer> enchantments) {
        if (this.isValid()) this.afx().getEnchantmentLevels(this.stack, this.rty(), this.level, enchantments);
    }

    public void modifyLoot(ObjectArrayList<ItemStack> loot, LootContext ctx) {
        if (this.isValid()) this.afx().modifyLoot(this.stack, this.rty(), this.level, loot, ctx);
    }

    public SetAffixInstance withNewLevel(float newLevel) {
        return new SetAffixInstance(this.affix, this.stack, this.rarity, Mth.clamp(newLevel, 0.0F, 1.0F));
    }
}