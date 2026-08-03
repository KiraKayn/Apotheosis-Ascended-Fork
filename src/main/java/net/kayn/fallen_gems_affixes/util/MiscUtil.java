package net.kayn.fallen_gems_affixes.util;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import net.kayn.fallen_gems_affixes.recipe.ErasureRecipe;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.HexFormat;
import java.util.function.Supplier;

public class MiscUtil {
    public static final Codec<Integer> COLOR_CODEC = Codec.STRING.xmap(s -> Integer.parseInt(s.startsWith("#") ? s.substring(1) : s, 16), i -> HexFormat.of().withUpperCase().toHexDigits(i & 0xFFFFFF, 6));

    public static boolean isOnCooldown(ResourceLocation id, float cooldown, LivingEntity entity) {
        long lastApplied = entity.getPersistentData().getLong("fga._cooldown." + id.toString());
        return lastApplied != 0 && lastApplied + cooldown >= entity.level().getGameTime();
    }

    public static void startCooldown(ResourceLocation id, LivingEntity entity) {
        entity.getPersistentData().putLong("fga._cooldown." + id.toString(), entity.level().getGameTime());
    }

    public static String formatPercentage(float value) {
        return String.format("%s%s%%", value < 0 ? "" : "+", fmt(value * 100));
    }

    public static String formatFlat(float value) {
        return String.format("%s%s", value < 0 ? "" : "+", value);
    }

    public static String fmt(float f) {
        long l = (long) f;
        if (f == (float) l) {
            return Long.toString(l);
        }
        return ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(f);
    }

    public static <T extends Recipe<?>> Supplier<RecipeSerializer<T>> simpleRecipeSerializer(Supplier<T> supplier) {
        return () -> new RecipeSerializer<>() {
            @Override
            public @NotNull T fromJson(ResourceLocation id, JsonObject json) {
                return supplier.get();
            }

            @Override
            public T fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
                return supplier.get();
            }

            @Override
            public void toNetwork(FriendlyByteBuf pBuffer, T pRecipe) {

            }
        };
    }
}
