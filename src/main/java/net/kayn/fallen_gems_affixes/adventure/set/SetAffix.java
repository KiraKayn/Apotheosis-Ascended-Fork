package net.kayn.fallen_gems_affixes.adventure.set;

import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.placebo.codec.CodecProvider;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kayn.fallen_gems_affixes.Fallen;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
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
import net.rtxyd.fallen.lib.runtime.forgemod.util.ICodecProvider;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.BiConsumer;

public abstract class SetAffix implements CodecProvider<SetAffix>, ICodecProvider<SetAffix> {
    protected final ResourceLocation setId;

    protected SetAffix(ResourceLocation setId) {
        this.setId = setId;
    }

    public ResourceLocation getSetId() {
        return this.setId;
    }

    public abstract ResourceLocation getTypeId();

    public void addModifiers(ItemStack stack, LootRarity rarity, float level, EquipmentSlot type, BiConsumer<Attribute, AttributeModifier> map) {}

    public MutableComponent getDescription(ItemStack stack, LootRarity rarity, float level) {
        ResourceLocation id = this.getId();
        String cleanKey = id != null ? id.toString().replace(":", ".").replace("/", "_") : "unknown";
        return Component.translatable("set_affix." + cleanKey + ".desc", fmt(level * 100))
                .withStyle(ChatFormatting.DARK_RED);
    }

    public Component getName(boolean prefix) {
        ResourceLocation id = this.getId();
        String cleanKey = id != null ? id.toString().replace(":", ".").replace("/", "_") : "unknown";
        if (prefix) return Component.translatable("set_affix." + cleanKey);
        return Component.translatable("set_affix." + cleanKey + ".suffix");
    }

    public Component getAugmentingText(ItemStack stack, LootRarity rarity, float level) {
        return this.getDescription(stack, rarity, level);
    }

    public int getDamageProtection(ItemStack stack, LootRarity rarity, float level, DamageSource source) { return 0; }

    public float getDamageBonus(ItemStack stack, LootRarity rarity, float level, MobType creatureType) { return 0.0F; }

    public void doPostAttack(ItemStack stack, LootRarity rarity, float level, LivingEntity user, @Nullable Entity target) {}

    public void doPostHurt(ItemStack stack, LootRarity rarity, float level, LivingEntity user, @Nullable Entity attacker) {}

    public void onArrowFired(ItemStack stack, LootRarity rarity, float level, LivingEntity user, AbstractArrow arrow) {}

    @Nullable
    public InteractionResult onItemUse(ItemStack stack, LootRarity rarity, float level, UseOnContext ctx) { return null; }

    public void onArrowImpact(AbstractArrow arrow, LootRarity rarity, float level, HitResult res, HitResult.Type type) {}

    public float onShieldBlock(ItemStack stack, LootRarity rarity, float level, LivingEntity entity, DamageSource source, float amount) { return amount; }

    public void onBlockBreak(ItemStack stack, LootRarity rarity, float level, Player player, LevelAccessor world, BlockPos pos, BlockState state) {}

    public float getDurabilityBonusPercentage(ItemStack stack, LootRarity rarity, float level, @Nullable ServerPlayer user) { return 0; }

    public float onHurt(ItemStack stack, LootRarity rarity, float level, DamageSource src, LivingEntity ent, float amount) { return amount; }

    public boolean enablesTelepathy() { return false; }

    public void getEnchantmentLevels(ItemStack stack, LootRarity rarity, float level, Map<Enchantment, Integer> enchantments) {}

    public void modifyLoot(ItemStack stack, LootRarity rarity, float level, ObjectArrayList<ItemStack> loot, LootContext ctx) {}

    public abstract void applySetBonus(Player player, int pieceCount);

    public abstract void removeSetBonus(Player player);

    public int[] getBonusThresholds() { return new int[]{2, 3, 4, 5}; }

    public abstract Component getBonusDescription(int threshold);

    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) { return !cat.isNone(); }

    public static boolean isOnCooldown(ResourceLocation id, int cooldown, LivingEntity entity) {
        long lastApplied = entity.getPersistentData().getLong("fga.set_affix_cooldown." + id);
        return lastApplied != 0 && lastApplied + cooldown >= entity.level().getGameTime();
    }

    public static void startCooldown(ResourceLocation id, LivingEntity entity) {
        entity.getPersistentData().putLong("fga.set_affix_cooldown." + id, entity.level().getGameTime());
    }

    public static String fmt(float f) {
        if (f == (long) f) return String.format("%d", (long) f);
        else return ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(f);
    }

    public static MutableComponent valueBounds(Component min, Component max) {
        return CommonComponents.space().append(
                Component.translatable("misc.apotheosis.affix_bounds", min, max).withStyle(ChatFormatting.DARK_GRAY));
    }

    protected static boolean isShiftDown() {
        return net.minecraft.client.gui.screens.Screen.hasShiftDown();
    }

    public final ResourceLocation getId() {
        return Fallen.Registries.SET_AFFIX_REGISTRY.getKey(this);
    }

    @Override
    public String toString() {
        return String.format("SetAffix: %s [Set: %s]", this.getId(), this.setId);
    }
}