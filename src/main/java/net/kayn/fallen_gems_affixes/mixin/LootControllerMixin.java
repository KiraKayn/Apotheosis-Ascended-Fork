package net.kayn.fallen_gems_affixes.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootController;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import net.kayn.fallen_gems_affixes.adventure.socket.CatalystSocketHelper;
import net.kayn.fallen_gems_affixes.adventure.socket.TieredSocketHelper;
import net.kayn.fallen_gems_affixes.recipe.ErasureRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LootController.class, remap = false)
public class LootControllerMixin {

    @Unique
    private static final ThreadLocal<Integer> fga$previousSocketCount = new ThreadLocal<>();
    @Unique
    private static final ThreadLocal<int[]> fga$previousTiers = new ThreadLocal<>();

    @WrapOperation(
            method = "createLootItem(Lnet/minecraft/world/item/ItemStack;Ldev/shadowsoffire/apotheosis/adventure/loot/LootCategory;Ldev/shadowsoffire/apotheosis/adventure/loot/LootRarity;Lnet/minecraft/util/RandomSource;)Lnet/minecraft/world/item/ItemStack;",
            at = @At(value = "INVOKE", target = "Ldev/shadowsoffire/apotheosis/adventure/socket/SocketHelper;getSockets(Lnet/minecraft/world/item/ItemStack;)I")
    )
    private static int getBaseSocketsOnly(ItemStack stack, Operation<Integer> original) {
        CompoundTag afxData = stack.getTagElement("affix_data");
        return afxData != null ? afxData.getInt("sockets") : 0;
    }

    @Inject(
            method = "createLootItem(Lnet/minecraft/world/item/ItemStack;Ldev/shadowsoffire/apotheosis/adventure/loot/LootCategory;Ldev/shadowsoffire/apotheosis/adventure/loot/LootRarity;Lnet/minecraft/util/RandomSource;)Lnet/minecraft/world/item/ItemStack;",
            at = @At("HEAD")
    )
    private static void captureExistingState(ItemStack stack, LootCategory cat, LootRarity rarity, RandomSource rand, CallbackInfoReturnable<ItemStack> cir) {
        CompoundTag afxData = stack.getTagElement("affix_data");
        int previousSocketCount = afxData != null ? afxData.getInt("sockets") : 0;
        fga$previousSocketCount.set(previousSocketCount);
        fga$previousTiers.set(TieredSocketHelper.getSocketTiers(stack));
    }

    @Inject(
            method = "createLootItem(Lnet/minecraft/world/item/ItemStack;Ldev/shadowsoffire/apotheosis/adventure/loot/LootCategory;Ldev/shadowsoffire/apotheosis/adventure/loot/LootRarity;Lnet/minecraft/util/RandomSource;)Lnet/minecraft/world/item/ItemStack;",
            at = @At("RETURN")
    )
    private static void onLootItemCreated(
            ItemStack stack, LootCategory cat, LootRarity rarity, RandomSource rand,
            CallbackInfoReturnable<ItemStack> cir) {

        Integer previousSocketCount = fga$previousSocketCount.get();
        int[] previousTiers = fga$previousTiers.get();
        if (previousTiers == null) previousTiers = new int[0];
        int prevCount = previousSocketCount != null ? previousSocketCount : 0;
        fga$previousSocketCount.remove();
        fga$previousTiers.remove();

        ItemStack result = cir.getReturnValue();
        if (!result.isEmpty()) {
            ErasureRecipe.removeScrollAffixes(result);

            if (CatalystSocketHelper.hasCatalystSocket(result)) return;
            TieredSocketHelper.assignSocketTiersToLootItem(result, rand, previousTiers, prevCount);
        }
    }
}