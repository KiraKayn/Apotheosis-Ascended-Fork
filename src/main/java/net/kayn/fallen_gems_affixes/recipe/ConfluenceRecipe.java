package net.kayn.fallen_gems_affixes.recipe;

import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.socket.SocketHelper;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemRegistry;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.adventure.affix.SocketBonusAffix;
import net.kayn.fallen_gems_affixes.adventure.socket.TieredSocketHelper;
import net.kayn.fallen_gems_affixes.adventure.socket.TieredSocketMode;
import net.kayn.fallen_gems_affixes.attachment.augment.AugmentHelper;
import net.kayn.fallen_gems_affixes.config.ModConfig;
import net.kayn.fallen_gems_affixes.event.FallenEventHandler;
import net.kayn.fallen_gems_affixes.registry.ModItems;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.Set;
import java.util.stream.Collectors;

public class ConfluenceRecipe extends SmithingTransformRecipe {

    private static final ResourceLocation ID = ResourceLocation.parse("fallen_gems_affixes:confluence");


    public ConfluenceRecipe() {
        super(ID,
                Ingredient.of(ModItems.SIGIL_OF_CONFLUENCE.get()),
                Ingredient.EMPTY,
                Ingredient.EMPTY,
                ItemStack.EMPTY);
    }

    @Override
    public boolean isAdditionIngredient(ItemStack pStack) {
        return !pStack.isEmpty();
    }

    @Override
    public boolean matches(Container inv, Level level) {
        ItemStack sigil  = inv.getItem(0);
        ItemStack base   = inv.getItem(1);
        ItemStack source = inv.getItem(2);

        if (!sigil.is(ModItems.SIGIL_OF_CONFLUENCE.get())) return false;
        if (base.isEmpty() || source.isEmpty()) return false;

        LootCategory baseCat = LootCategory.forItem(base);
        LootCategory srcCat  = LootCategory.forItem(source);
        if (baseCat.isNone() || srcCat.isNone() || baseCat != srcCat) return false;

        DynamicHolder<LootRarity> baseRarity = AffixHelper.getRarity(base);
        DynamicHolder<LootRarity> srcRarity  = AffixHelper.getRarity(source);
        if (!baseRarity.isBound() || !srcRarity.isBound()) return false;
        if (!baseRarity.getId().equals(srcRarity.getId())) return false;

        if (!AugmentHelper.getAugments(base).isEmpty())   return false;
        if (!AugmentHelper.getAugments(source).isEmpty()) return false;

        if (FallenEventHandler.isAffixCombined(base) || FallenEventHandler.isAffixCombined(source))   return false;

        boolean baseHasAffixes = AffixHelper.hasAffixes(base);
        if (baseHasAffixes) return true;
        boolean srcHasAffixes  = AffixHelper.hasAffixes(source);
        if (srcHasAffixes) return true;
        return  !SocketHelper.getGems(source).isEmpty();
    }

    @Override
    public ItemStack assemble(Container inv, RegistryAccess access) {
        ItemStack base   = inv.getItem(1);
        ItemStack source = inv.getItem(2);

        ItemStack result = base.copy();
        result.setCount(1);
        CompoundTag baseTag = base.getTag();
        CompoundTag srcTag = source.getTag();
        if (baseTag == null || srcTag == null) return result;
        CompoundTag resultTag = result.getOrCreateTag();

        CompoundTag baseAffixData = baseTag.getCompound(AffixHelper.AFFIX_DATA);
        CompoundTag srcAffixData  = srcTag.getCompound(AffixHelper.AFFIX_DATA);

        mergeAffixScrolls(baseTag, srcTag, resultTag);

        CompoundTag mergedAffixData = baseAffixData.copy();

        mergedAffixData.put(AffixHelper.AFFIXES, mergeAffixes(srcAffixData, mergedAffixData));

        if (baseAffixData.contains(AffixHelper.RARITY)) {
            mergedAffixData.putString(AffixHelper.RARITY, baseAffixData.getString(AffixHelper.RARITY));
        }

        mergedAffixData.put(SocketHelper.GEMS, mergeGems(srcAffixData, mergedAffixData, base, SocketHelper.getSockets(base)));

        resultTag.put(AffixHelper.AFFIX_DATA, mergedAffixData);

        resultTag.putBoolean(Fallen.AugmentMisc.AFFIX_COMBINED, true);

        resultTag.remove(Fallen.AugmentMisc.AUGMENT_DATA);

        return result;
    }

    public void mergeAffixScrolls(CompoundTag baseTag, CompoundTag srcTag, CompoundTag resultTag) {
        ListTag baseScrollAffixes = baseTag.getList(ErasureRecipe.TAG_SCROLL_AFFIXES, CompoundTag.TAG_STRING);
        Set<String> affixesSet = baseScrollAffixes.stream().map(Tag::getAsString).collect(Collectors.toSet());
        int baseScrollSlotsUsed = baseTag.getInt(ErasureRecipe.TAG_SCROLL_SLOTS_USED);
        ListTag srcScrollAffixes = srcTag.getList(ErasureRecipe.TAG_SCROLL_AFFIXES, CompoundTag.TAG_STRING);
        int maxSlots = ModConfig.MAX_SCROLL_SLOTS.get();

        for (int i = 0; i < srcScrollAffixes.size(); i++) {
            String affix = srcScrollAffixes.getString(i);
            if (affixesSet.add(affix)) {
                baseScrollSlotsUsed++;
                if (baseScrollSlotsUsed >= maxSlots) {
                    resultTag.putInt(ErasureRecipe.TAG_SCROLL_SLOTS_USED, baseScrollSlotsUsed);
                    break;
                }
            }
        }
        ListTag newList = new ListTag();
        for (String s : affixesSet) {
            newList.add(StringTag.valueOf(s));
        }
        resultTag.put(ErasureRecipe.TAG_SCROLL_AFFIXES, newList);
    }

    public Tag mergeGems(CompoundTag srcAffixData, CompoundTag mergedAffixData, ItemStack base, int sockets) {
        if (srcAffixData == null || sockets <= 0) return new ListTag();

        ListTag mergedGems = mergedAffixData.getList(SocketHelper.GEMS, 10).copy();
        ListTag srcGems = srcAffixData.getList(SocketHelper.GEMS, 10).copy();

        int[] tiers = TieredSocketHelper.getSocketTiers(base);
        boolean[] occupied = new boolean[mergedGems.size()];
        for (int i = 0; i < mergedGems.size(); i++) {
            occupied[i] = !mergedGems.getCompound(i).getCompound("tag").isEmpty();
        }

        TieredSocketMode mode = ModConfig.TIERED_SOCKET_MODE.get();

        for (Tag tag : srcGems) {
            CompoundTag gemEntry = (CompoundTag) tag;
            CompoundTag gemTag = gemEntry.getCompound("tag");
            if (gemTag.isEmpty()) continue;

            ResourceLocation gemId = ResourceLocation.tryParse(gemTag.getString("gem"));
            if (gemId == null) continue;

            Gem gem = GemRegistry.INSTANCE.getValue(gemId);
            if (gem == null || !gem.isUnique()) continue;

            int gemOrdinal = resolveGemRarityOrdinal(gemEntry);
            int socketIndex = TieredSocketHelper.getFirstCompatibleEmptySocket(tiers, occupied, gemOrdinal, mode);
            if (socketIndex < 0) continue;

            mergedGems.set(socketIndex, tag);
            occupied[socketIndex] = true;
        }

        return mergedGems;
    }

    private int resolveGemRarityOrdinal(CompoundTag gemEntry) {
        ItemStack gemStack = ItemStack.of(gemEntry);
        DynamicHolder<LootRarity> holder = AffixHelper.getRarity(gemStack);
        return holder.isBound() ? holder.get().ordinal() : -1;
    }

    public Tag mergeAffixes(CompoundTag srcAffixData, CompoundTag mergedAffixData) {
        if (srcAffixData != null && srcAffixData.contains(AffixHelper.AFFIXES, Tag.TAG_COMPOUND)) {
            CompoundTag srcAffixes = srcAffixData.getCompound(AffixHelper.AFFIXES);
            CompoundTag mergedAffixes = mergedAffixData.getCompound(AffixHelper.AFFIXES).copy();

            for (String key : srcAffixes.getAllKeys()) {
                if (!mergedAffixes.contains(key)) {
                    mergedAffixes.putFloat(key, srcAffixes.getFloat(key));
                } else {
                    float value = mergedAffixes.getFloat(key);
                    if (key.equals(SocketBonusAffix.ID.toString())) {
                        mergedAffixes.putFloat(key, Mth.clamp(value + srcAffixes.getFloat(key) + 1, 0, 1.5f));
                    } else {
                        mergedAffixes.putFloat(key, Mth.clamp(value + srcAffixes.getFloat(key), 0, 1f));
                    }
                }
            }
            return mergedAffixes;
        }


        return new CompoundTag();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 3;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess access) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(Blocks.SMITHING_TABLE);
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public RecipeType<?> getType() {
        return RecipeType.SMITHING;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Fallen.RecipeSerializers.CONFLUENCE.get();
    }
}