package net.kayn.fallen_gems_affixes.recipe;

import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.adventure.socket.TieredSocketHelper;
import net.kayn.fallen_gems_affixes.attachment.augment.AugmentSlotHelper;
import net.kayn.fallen_gems_affixes.event.FallenEventHandler;
import net.kayn.fallen_gems_affixes.registry.ModItems;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class SocketConversionRecipe extends SmithingTransformRecipe {

    private static final ResourceLocation ID = ResourceLocation.parse("fallen_gems_affixes:socket_conversion");

    public SocketConversionRecipe() {
        super(ID, Ingredient.EMPTY, Ingredient.EMPTY, Ingredient.of(ModItems.SIGIL_OF_ASCENSION.get()), ItemStack.EMPTY);
    }

    private int getBaseSocketCount(ItemStack stack) {
        CompoundTag afxData = stack.getTagElement("affix_data");
        return afxData != null ? afxData.getInt("sockets") : 0;
    }

    @Override
    public boolean matches(Container inv, Level level) {
        ItemStack base  = inv.getItem(1);
        ItemStack sigil = inv.getItem(2);

        if (!sigil.is(ModItems.SIGIL_OF_ASCENSION.get())) return false;

        if (!TieredSocketHelper.hasEmptyRegularSocket(base)) return false;

        if (FallenEventHandler.isAffixCombined(base))   return false;

        return AugmentSlotHelper.canAddMoreSlots(base);
    }

    @Override
    public ItemStack assemble(Container inv, RegistryAccess access) {
        ItemStack base   = inv.getItem(1);
        ItemStack result = base.copy();
        result.setCount(1);

        int currentBaseGemSockets = getBaseSocketCount(result);

        int removedSocketIndex = TieredSocketHelper.getFirstEmptyRegularSocket(result);
        if (removedSocketIndex < 0) return result;

        CompoundTag tag     = result.getOrCreateTag();
        CompoundTag afxData = tag.getCompound("affix_data");
        afxData.putInt("sockets", currentBaseGemSockets - 1);
        tag.put("affix_data", afxData);

        TieredSocketHelper.removeSocketTierAt(result, removedSocketIndex);

        AugmentSlotHelper.addAugmentSlots(result, 1);

        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
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
        return Fallen.RecipeSerializers.SOCKET_CONVERSION.get();
    }
}
