package net.kayn.fallen_gems_affixes.recipe;

import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.socket.SocketHelper;
import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.attachment.augment.AugmentHelper;
import net.kayn.fallen_gems_affixes.event.FallenEventHandler;
import net.kayn.fallen_gems_affixes.registry.ModItems;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TransmutationRecipe extends SmithingTransformRecipe {

    private static final ResourceLocation ID = ResourceLocation.parse("fallen_gems_affixes:transmutation");

    public TransmutationRecipe() {
        super(ID, Ingredient.of(ModItems.SIGIL_OF_TRANSMUTATION.get()), Ingredient.EMPTY, Ingredient.EMPTY, ItemStack.EMPTY);
    }


    @Override
    public boolean isAdditionIngredient(ItemStack pStack) {
        return !pStack.isEmpty();
    }

    @Override
    public boolean matches(Container inv, Level level) {
        ItemStack sigil = inv.getItem(0);
        ItemStack base = inv.getItem(1);
        ItemStack source = inv.getItem(2);

        if (!sigil.is(ModItems.SIGIL_OF_TRANSMUTATION.get())) return false;

        if (base.isEmpty() || source.isEmpty()) return false;

        LootCategory baseCat = LootCategory.forItem(base);
        LootCategory srcCat = LootCategory.forItem(source);
        if (baseCat.isNone() || srcCat.isNone() || baseCat != srcCat) return false;

        if (FallenEventHandler.isAffixCombined(base) || FallenEventHandler.isAffixCombined(source))   return false;

        if (AffixHelper.hasAffixes(base)) return false;
        if (!SocketHelper.getGems(base).isEmpty()) return false;
        if (!AugmentHelper.getAugments(base).isEmpty()) return false;

        boolean hasAffixes = AffixHelper.hasAffixes(source);
        if (hasAffixes) return true;
        boolean hasGems = !SocketHelper.getGems(source).isEmpty();
        if (hasGems) return true;
        return AugmentHelper.hasAugments(base);
    }

    @Override
    public ItemStack assemble(Container inv, RegistryAccess access) {
        ItemStack base = inv.getItem(1);
        ItemStack source = inv.getItem(2);

        ItemStack result = base.copy();
        result.setCount(1);

        CompoundTag srcTag = source.getTag();
        if (srcTag == null) return result;
        CompoundTag resultTag = result.getOrCreateTag();

        CompoundTag srcAffix = srcTag.getCompound(AffixHelper.AFFIX_DATA);

        resultTag.put(AffixHelper.AFFIX_DATA, srcAffix.copy());

        CompoundTag srcAugmentRoot = srcTag.getCompound(Fallen.AugmentMisc.AUGMENT_DATA);
        CompoundTag newAugmentRoot = srcAugmentRoot.copy();
        resultTag.put(Fallen.AugmentMisc.AUGMENT_DATA, newAugmentRoot);
        resultTag.put(ErasureRecipe.TAG_SCROLL_AFFIXES, srcTag.getList(ErasureRecipe.TAG_SCROLL_AFFIXES, CompoundTag.TAG_STRING));
        resultTag.putInt(ErasureRecipe.TAG_SCROLL_SLOTS_USED, srcTag.getInt(ErasureRecipe.TAG_SCROLL_SLOTS_USED));
        return result;
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
        return Fallen.RecipeSerializers.TRANSMUTATION.get();
    }
}