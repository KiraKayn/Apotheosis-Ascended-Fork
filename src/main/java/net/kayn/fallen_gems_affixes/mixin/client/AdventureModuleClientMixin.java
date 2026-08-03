package net.kayn.fallen_gems_affixes.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.shadowsoffire.apotheosis.adventure.client.AdventureModuleClient;
import net.kayn.fallen_gems_affixes.config.ModConfig;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = AdventureModuleClient.class, remap = false)
@OnlyIn(Dist.CLIENT)
public class AdventureModuleClientMixin {
    @WrapOperation(method = "tooltips", at = @At(value = "INVOKE", target = "Ldev/shadowsoffire/apotheosis/adventure/socket/SocketHelper;getSockets(Lnet/minecraft/world/item/ItemStack;)I"))
    private static int hideApothRemoveMarker(ItemStack stack, Operation<Integer> original) {
        if (original.call(stack) - ModConfig.EXTRA_SOCKETS.get() <= 0) {
            return 0;
        } else return original.call(stack);
    }
}
