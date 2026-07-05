package net.kayn.fallen_gems_affixes.init.loot;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixRegistry;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kayn.fallen_gems_affixes.config.ModConfig;
import net.kayn.fallen_gems_affixes.item.AffixScrollItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;

public class UniversalBossLootModifier extends LootModifier {

    public static final Supplier<Codec<UniversalBossLootModifier>> CODEC_SUPPLIER = Suppliers.memoize(UniversalBossLootModifier::makeCodec);
    public static final Codec<UniversalBossLootModifier> CODEC = CODEC_SUPPLIER.get();

    private static Codec<UniversalBossLootModifier> makeCodec() {
        return RecordCodecBuilder.create(inst -> codecStart(inst).and(inst.group(Codec.DOUBLE.fieldOf("enchant_book_chance").forGetter(m -> m.enchantBookChance), Codec.unboundedMap(Codec.STRING, Codec.list(RarityDropEntry.CODEC)).fieldOf("rarity_drops").forGetter(m -> m.rarityDrops), Codec.list(ResourceLocation.CODEC).optionalFieldOf("enchant_blacklist", Collections.emptyList()).forGetter(m -> new ArrayList<>(m.enchantBlacklist)))).apply(inst, (conditions, chance, drops, blacklist) -> new UniversalBossLootModifier(conditions, chance, drops, new HashSet<>(blacklist))));
    }

    private final double enchantBookChance;
    private final Map<String, List<RarityDropEntry>> rarityDrops;
    private final Set<ResourceLocation> enchantBlacklist;

    public UniversalBossLootModifier(LootItemCondition[] conditions, double enchantBookChance, Map<String, List<RarityDropEntry>> rarityDrops, Set<ResourceLocation> enchantBlacklist) {
        super(conditions);
        this.enchantBookChance = enchantBookChance;
        this.rarityDrops = rarityDrops;
        this.enchantBlacklist = enchantBlacklist;
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext ctx) {
        // exclude blocks
        if (ctx.hasParam(LootContextParams.BLOCK_STATE)) {
            return loot;
        }
        Entity entity = ctx.getParamOrNull(LootContextParams.THIS_ENTITY);
        if (!(entity instanceof LivingEntity living)) return loot;

        var data = living.getPersistentData();
        if (!data.getBoolean("fga.universal_boss")) return loot;

        String rarityKey = data.getString("fga.universal_boss.rarity");
        if (rarityKey.isEmpty()) return loot;

        List<Runnable> attempts = new ArrayList<>();

        attempts.add(() -> {
            LootRarity rarity = resolveRarity(rarityKey);
            if (rarity != null && ctx.getRandom().nextDouble() < ModConfig.AFFIX_SCROLL_DROP_CHANCE.get()) {
                Affix affix = rollRandomAffix(rarity, ctx.getRandom());
                if (affix != null) {
                    ResourceLocation affixId = AffixRegistry.INSTANCE.getKey(affix);
                    if (affixId != null) {
                        loot.add(AffixScrollItem.createScroll(affixId, rarity, ctx.getRandom().nextFloat()));
                    }
                }
            }
        });

        attempts.add(() -> {
            if (ctx.getRandom().nextDouble() < enchantBookChance) {
                ItemStack book = rollEnchantedBook(ctx, enchantBlacklist);
                if (!book.isEmpty()) loot.add(book);
            }
        });

        List<RarityDropEntry> entries = rarityDrops.getOrDefault(rarityKey, Collections.emptyList());
        for (RarityDropEntry entry : entries) {
            attempts.add(() -> {
                if (ctx.getRandom().nextDouble() < entry.chance()) {
                    var itemObj = ForgeRegistries.ITEMS.getValue(entry.item());
                    if (itemObj != null) {
                        int count = entry.minCount() + ctx.getRandom().nextInt(Math.max(1, entry.maxCount() - entry.minCount() + 1));
                        loot.add(new ItemStack(itemObj, count));
                    }
                }
            });
        }

        Collections.shuffle(attempts, new java.util.Random(ctx.getRandom().nextLong()));
        int sizeBefore = loot.size();
        for (Runnable attempt : attempts) {
            attempt.run();
            if (loot.size() > sizeBefore) break;
        }

        return loot;
    }

    private static LootRarity resolveRarity(String name) {
        try {
            return RarityRegistry.byLegacyId(name).get();
        } catch (Exception e) {
            return null;
        }
    }

    private static Affix rollRandomAffix(LootRarity rarity, net.minecraft.util.RandomSource rand) {
        List<Affix> candidates = new ArrayList<>();
        for (Affix affix : AffixRegistry.INSTANCE.getValues()) {
            if (affix.getType().needsValidation()) {
                for (LootCategory cat : LootCategory.VALUES) {
                    if (!cat.isNone() && affix.canApplyTo(ItemStack.EMPTY, cat, rarity)) {
                        candidates.add(affix);
                        break;
                    }
                }
            }
        }
        if (candidates.isEmpty()) return null;
        return candidates.get(rand.nextInt(candidates.size()));
    }

    private static ItemStack rollEnchantedBook(LootContext ctx, Set<ResourceLocation> blacklist) {
        List<Enchantment> enchants = new ArrayList<>(ForgeRegistries.ENCHANTMENTS.getValues());
        Collections.shuffle(enchants, new java.util.Random(ctx.getRandom().nextLong()));
        boolean strict = ModConfig.STRICT_UNIVERSAL_BOSS_ENCHANT_DROP.get();
        for (Enchantment ench : enchants) {
            if (ench.isCurse()) continue;
            if (strict) {
                if (!ench.isAllowedOnBooks() || !ench.isDiscoverable() || ench.isTreasureOnly()) continue;
            }
            ResourceLocation enchId = ForgeRegistries.ENCHANTMENTS.getKey(ench);
            if (enchId != null && blacklist.contains(enchId)) continue;
            int maxLevel = ench.getMaxLevel();
            int level = maxLevel > 1 ? 1 + ctx.getRandom().nextInt(maxLevel) : 1;
            ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
            EnchantedBookItem.addEnchantment(book, new EnchantmentInstance(ench, level));
            return book;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC_SUPPLIER.get();
    }

    public record RarityDropEntry(ResourceLocation item, double chance, int minCount, int maxCount) {
        public static final Codec<RarityDropEntry> CODEC = RecordCodecBuilder.create(inst -> inst.group(ResourceLocation.CODEC.fieldOf("item").forGetter(RarityDropEntry::item), Codec.DOUBLE.fieldOf("chance").forGetter(RarityDropEntry::chance), Codec.INT.fieldOf("min_count").forGetter(RarityDropEntry::minCount), Codec.INT.fieldOf("max_count").forGetter(RarityDropEntry::maxCount)).apply(inst, RarityDropEntry::new));
    }
}