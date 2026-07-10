package net.kayn.fallen_gems_affixes.adventure.boss;

import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.boss.BossStats;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.kayn.fallen_gems_affixes.adventure.entity.EntityAffixEntry;
import net.kayn.fallen_gems_affixes.adventure.entity.EntityAffixHelper;
import net.kayn.fallen_gems_affixes.adventure.entity.EntityAffixInstance;
import net.kayn.fallen_gems_affixes.adventure.entity.MobAffixHelper;
import net.kayn.fallen_gems_affixes.attachment.rarity.FallenRarity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import dev.shadowsoffire.placebo.json.ChancedEffectInstance;
import dev.shadowsoffire.placebo.json.RandomAttributeModifier;
import net.rtxyd.fallen.lib.runtime.forgemod.util.ILocalRarity;

import java.util.*;

public class UniversalBossEventHandler {

    private static final String TAG = "fga.universal_boss";

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEntityJoin(MobSpawnEvent.FinalizeSpawn event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof Monster mob)) return;

        CompoundTag data = mob.getPersistentData();
        if (data.contains(TAG)) return;
        if (data.contains("apoth.boss") || data.contains("apoth.miniboss")) return;

        UniversalBossConfig config = UniversalBossLoader.getConfig();
        if (config == null) return;

        var entityId = ForgeRegistries.ENTITY_TYPES.getKey(mob.getType());
        if (config.isBlacklisted(mob.getType(), entityId)) return;

        ResourceLocation dimensionId = event.getLevel().getLevel().dimension().location();
        List<FallenRarity> allowed = config.getRaritiesForDimension(dimensionId);
        if (allowed != null && allowed.isEmpty()) return;

        Set<FallenRarity> allowedSet = allowed != null ? new java.util.HashSet<>(allowed) : null;

        Map<FallenRarity, Float> gearBonuses = Collections.emptyMap();
        if (!config.gearBonus().isEmpty()) {
            Player nearestPlayer = event.getLevel().getLevel()
                    .getNearestPlayer(event.getX(), event.getY(), event.getZ(), 64, false);
            if (nearestPlayer != null) {
                gearBonuses = new HashMap<>();
                for (EquipmentSlot slot : EquipmentSlot.values()) {
                    ItemStack item = nearestPlayer.getItemBySlot(slot);
                    if (item.isEmpty()) continue;
                    DynamicHolder<LootRarity> rarityHolder = AffixHelper.getRarity(item);
                    if (!rarityHolder.isBound()) continue;
                    ILocalRarity itemRarity = (ILocalRarity) rarityHolder.get();
                    FallenRarity rarity = (FallenRarity) Fallen.Registries.RARITY_REGISTRY.rarityRegistry.getRarityMapView().get(itemRarity.fallen_lib$getId());
                    Float bonus = config.gearBonus().get(rarity);
                    if (bonus != null && bonus > 0f) {
                        gearBonuses.merge(rarity, bonus, Float::sum);
                    }
                }
            }
        }

        FallenRarity rarity = config.rollRarity(mob.getRandom(), allowedSet, gearBonuses);
        if (rarity == null) return;

        BossStats stats = config.stats().get(rarity);
        if (stats == null) {
            FallenGemsAffixes.LOGGER.warn("[FGA] No BossStats for rarity '{}'", config.getRarityKey(rarity));
            return;
        }

        applyBoss(mob, stats, rarity, config);

        data.putBoolean(TAG, true);
        data.putString(TAG + ".rarity", config.getRarityKey(rarity));
        data.putBoolean("apoth.boss", true);
        if (rarity.getRarity() instanceof LootRarity r) {
            ResourceLocation rarityId = RarityRegistry.INSTANCE.getKey(r);
            String rarityIdStr = rarityId != null ? rarityId.toString() : config.getRarityKey(rarity);
            data.putString("apoth.rarity", rarityIdStr);
        }
    }

    private static void applyBoss(Mob mob, BossStats stats, FallenRarity rarity, UniversalBossConfig config) {
        int duration = mob instanceof Creeper ? 6000 : Integer.MAX_VALUE;

        for (ChancedEffectInstance inst : stats.effects()) {
            if (mob.getRandom().nextFloat() <= inst.chance()) {
                mob.addEffect(inst.create(mob.getRandom(), duration));
            }
        }

        float statChance = config.getStatChance(rarity);
        for (RandomAttributeModifier modif : stats.modifiers()) {
            if (mob.getRandom().nextFloat() < statChance) {
                modif.apply(mob.getRandom(), mob);
            }
        }

        mob.setHealth(mob.getMaxHealth());

        String rarityKey = config.getRarityKey(rarity);
        List<EntityAffixEntry> affixEntries = config.getAffixesForRarity(rarity);
        for (EntityAffixEntry entry : affixEntries) {
            if (mob.getRandom().nextFloat() < entry.chance()) {
                EntityAffixHelper.addAffix(mob, entry.affixId(), rarityKey, entry.level());
            }
        }

        if (!affixEntries.isEmpty()) {
            List<EntityAffixInstance> resolved = EntityAffixHelper.getAffixes(mob);
            for (EntityAffixInstance inst : resolved) {
                if (!inst.isValid()) continue;
                for (EquipmentSlot slot : EquipmentSlot.values()) {
                    inst.affix().get().addModifiers(ItemStack.EMPTY, inst.rarity().get(), inst.level(), slot,
                            (attr, mod) -> {
                                AttributeInstance attrInst = mob.getAttribute(attr);
                                if (attrInst != null && !attrInst.hasModifier(mod)) {
                                    attrInst.addPermanentModifier(mod);
                                }
                            });
                }
            }
        }

        for (EntityAffixEntry entry : config.getMobAffixesForRarity(rarity)) {
            if (mob.getRandom().nextFloat() < entry.chance()) {
                MobAffixHelper.addAffix(mob, entry.affixId(), entry.level());
            }
        }
        if (rarity.getRarity() instanceof LootRarity r) {
            String rarityPath = RarityRegistry.INSTANCE.getKey(r) != null
                    ? Objects.requireNonNull(RarityRegistry.INSTANCE.getKey(r)).getPath() : "unknown";
            String rarityTitle = rarityPath.substring(0, 1).toUpperCase() + rarityPath.substring(1);
            Component mobName = mob.hasCustomName() ? mob.getCustomName() : mob.getName();
            assert mobName != null;
            MutableComponent name;

            if (net.kayn.fallen_gems_affixes.config.ModConfig.SHOW_BOSS_RARITY_NAME.get()) {
                name = Component.literal(rarityTitle + " ")
                        .withStyle(Style.EMPTY.withColor(r.getColor()))
                        .append(mobName.copy().withStyle(Style.EMPTY.withColor(r.getColor())));
            } else {
                name = mobName.copy().withStyle(Style.EMPTY.withColor(r.getColor()));
            }
            mob.setCustomName(name);
            mob.setCustomNameVisible(false);
        }
    }
}