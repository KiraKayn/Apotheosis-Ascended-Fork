package net.kayn.fallen_gems_affixes.recipe;

import com.google.gson.JsonObject;

import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.kayn.fallen_gems_affixes.item.AffixScrollItem;
import net.kayn.fallen_gems_affixes.registry.ModItems;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.IIngredientSerializer;

import java.util.stream.Stream;

public class AffixScrollIngredient extends Ingredient {

    public static final ResourceLocation ID = new ResourceLocation("fallen_gems_affixes", "affix_scroll");
    public static final Serializer SERIALIZER = new Serializer();

    protected final String rarityId;
    protected DynamicHolder<LootRarity> rarityHolder;
    protected ItemStack[] displayStacks;

    protected AffixScrollIngredient(String rarityId) {
        super(Stream.empty());
        this.rarityId = rarityId;
    }

    private LootRarity resolveRarity() {
        if (this.rarityHolder == null) {
            this.rarityHolder = RarityRegistry.byLegacyId(this.rarityId);
        }
        if (!this.rarityHolder.isBound()) return null;
        return this.rarityHolder.get();
    }

    @Override
    public boolean test(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (!(stack.getItem() instanceof AffixScrollItem)) return false;
        LootRarity stackRarity = AffixScrollItem.getAffixRarity(stack);
        LootRarity thisRarity = resolveRarity();
        if (stackRarity == null || thisRarity == null) return stackRarity == thisRarity;
        return RarityRegistry.INSTANCE.getKey(stackRarity).equals(RarityRegistry.INSTANCE.getKey(thisRarity));
    }

    @Override
    public ItemStack[] getItems() {
        if (this.displayStacks == null) {
            LootRarity rarity = resolveRarity();
            if (rarity == null) {
                this.displayStacks = new ItemStack[0];
            } else {
                ItemStack stack = new ItemStack(ModItems.AFFIX_SCROLL.get());
                stack.getOrCreateTag().putString(AffixScrollItem.TAG_AFFIX_RARITY, RarityRegistry.INSTANCE.getKey(rarity).toString());
                this.displayStacks = new ItemStack[] { stack };
            }
        }
        return this.displayStacks;
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public IIngredientSerializer<? extends Ingredient> getSerializer() {
        return SERIALIZER;
    }

    public static class Serializer implements IIngredientSerializer<AffixScrollIngredient> {

        @Override
        public AffixScrollIngredient parse(FriendlyByteBuf buffer) {
            String rarityId = buffer.readUtf();
            return new AffixScrollIngredient(rarityId);
        }

        @Override
        public AffixScrollIngredient parse(JsonObject json) {
            String rarityId = GsonHelper.getAsString(json, "rarity");
            return new AffixScrollIngredient(rarityId);
        }

        @Override
        public void write(FriendlyByteBuf buffer, AffixScrollIngredient ingredient) {
            buffer.writeUtf(ingredient.rarityId);
        }
    }
}