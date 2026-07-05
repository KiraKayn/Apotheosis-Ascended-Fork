package net.kayn.fallen_gems_affixes.augment;

import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.attachment.augment.AugmentHelper;
import net.kayn.fallen_gems_affixes.attachment.augment.AugmentInstance;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.IEventBus;

import java.util.ArrayList;
import java.util.List;

public class GenesisEventHandler {

    private static final String SOURCE_APOTH     = "apoth";
    private static final String SOURCE_UNIVERSAL = "universal";

    private static final TagKey<EntityType<?>> BOSS_TAG = TagKey.create(
            Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath("fallen_gems_affixes", "boss_slayer"));

    public static void bootstrap(IEventBus eventBus) {
        eventBus.addListener(GenesisEventHandler::onLivingDeath);
    }

    private static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity dying = event.getEntity();
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;

        String dedupKey = resolveDeduplicationKey(dying);
        if (dedupKey == null) return;

        for (ItemStack stack : getAllItems(player)) {
            if (!stack.isEmpty()) tryApplyGenesis(stack, dedupKey);
        }
    }
    private static String resolveDeduplicationKey(LivingEntity entity) {
        CompoundTag data = entity.getPersistentData();

        boolean isApoth     = data.contains("apoth.boss") || data.contains("apoth.miniboss");
        boolean isUniversal = data.contains("fga.universal_boss");

        if (isApoth && !isUniversal) {
            return extractRarity(data, "apoth.rarity");
        }

        if (entity.getType().is(BOSS_TAG)) {
            ResourceLocation entityType = EntityType.getKey(entity.getType());
            if (entityType == null) return null;
            return entityType.toString();
        }

        return null;
    }

    private static String extractRarity(CompoundTag data, String nbtKey) {
        if (!data.contains(nbtKey)) return null;
        String raw = data.getString(nbtKey).trim();
        if (raw.isEmpty()) return null;
        return raw;
    }

    private static void tryApplyGenesis(ItemStack stack, String dedupKey) {
        AugmentInstance inst = AugmentHelper.getAugments(stack).get(Fallen.Augments.GENESIS);
        if (inst == null) return;

        GenesisAugment.GenesisData data = (GenesisAugment.GenesisData) inst.getData();

        if (!data.killedBossIds.add(dedupKey)) return;

        data.bossKillCount = data.killedBossIds.size();
        data.affixPower   += data.affixPowerBoost;
        data.gemPower     += data.gemPowerBoost;

        AugmentHelper.applyAugment(stack, new AugmentInstance(inst.getAugment(), data));
    }

    private static List<ItemStack> getAllItems(ServerPlayer player) {
        List<ItemStack> items = new ArrayList<>();
        items.addAll(player.getInventory().items);
        items.addAll(player.getInventory().armor);
        items.addAll(player.getInventory().offhand);
        return items;
    }
}