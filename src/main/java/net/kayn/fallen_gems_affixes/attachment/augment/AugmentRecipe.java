package net.kayn.fallen_gems_affixes.attachment.augment;

import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.attachment.augment.mod_events.AssembleAugmentRecipeEvent;
import net.kayn.fallen_gems_affixes.item.augments.AugmentItem;
import net.kayn.fallen_gems_affixes.registry.ModItems;
import net.kayn.fallen_gems_affixes.types.augment.IAugment;
import net.kayn.fallen_gems_affixes.types.augment.IAugmentRecipe;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.rtxyd.fallen.lib.runtime.forgemod.util.GameLifecycleHelper;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AugmentRecipe extends SmithingTransformRecipe implements IAugmentRecipe {
    private static final ResourceLocation ID = ResourceLocation.parse("fallen_gems_affixes:add_augment");

    public AugmentRecipe() {
        super(ID, Ingredient.EMPTY, Ingredient.EMPTY, Ingredient.of(ModItems.AUGMENT_ITEM.get()), ItemStack.EMPTY);
    }

    @Override
    public boolean matches(Container cont, Level level) {
        ItemStack base = cont.getItem(1);

        if (base.getTag() != null && base.getTag().contains(Fallen.AugmentMisc.AFFIX_COMBINED)) {
            return false;
        }

        ItemStack addition = cont.getItem(2);
        LootCategory cat = LootCategory.forItem(base);
        boolean isAugmentItem = this.isAdditionIngredient(addition);
        if (!isAugmentItem) return false;
        if (!AugmentItem.canApplyTo(addition, cat)) {return false;}
        if (!AugmentSlotHelper.hasAvailableSlots(base)) {return false;}
        GameLifecycleHelper.submitContextCall(Fallen.ContextKeys.AUG_RECIPE_LEVEL, () -> level);
        return isAugmentItem;
    }

    @Override
    public ItemStack assemble(Container cont, RegistryAccess access) {
        ItemStack result = cont.getItem(1).copy();
        result.setCount(1);
        ItemStack augmentItem = cont.getItem(2).copy();
        augmentItem.setCount(1);
        return addAugmentData(result, augmentItem, cont, access);
    }

    /**
     * Copies augment entries from the augment consumable onto the result item.
     *
     * <p>Augment-specific post-processing (Supremacy, Genesis) is applied AFTER all NBT
     * writes are complete so that those methods see the fully-committed state.
     */
    private ItemStack addAugmentData(ItemStack result, ItemStack augmentItem, Container cont, RegistryAccess access) {
        AugmentMeta meta = AugmentItem.getAugmentData(augmentItem);
        if (meta == null) return result;
        IAugment aug = meta.getAugment();
        Level level = GameLifecycleHelper.callIfSameTick(Fallen.ContextKeys.AUG_RECIPE_LEVEL, e -> {});
        if (level == null) return result;
        if (!MinecraftForge.EVENT_BUS.post(new AssembleAugmentRecipeEvent(cont, level))) {
            if (aug.onAssemble(result, augmentItem, aug, cont, level)) {
                AugmentHelper.applyAugment(result, new AugmentInstance(aug, meta.newDefaultData()));
            }
        }
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return pWidth * pHeight >= 2;
    }

    @Override
    public boolean isBaseIngredient(ItemStack pStack) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess regs) {
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
        return Fallen.RecipeSerializers.ADD_AUGMENT.get();
    }
}