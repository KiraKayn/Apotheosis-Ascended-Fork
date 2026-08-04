package net.kayn.fallen_gems_affixes.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.shadowsoffire.apotheosis.adventure.AdventureEvents;
import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.util.ArrowFireCache;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.rtxyd.fallen.lib.runtime.forgemod.util.GameLifecycleHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = AdventureEvents.class, priority = 900, remap = false)
public class AdventureEventsMixin {
    @WrapOperation(method = "fireArrow", at = @At(value = "INVOKE", target = "Ldev/shadowsoffire/apotheosis/adventure/affix/AffixHelper;copyFrom(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/Entity;)V"))
    private static void recordArrowFireEventA(ItemStack bow, Entity entity, Operation<Void> original) {
        original.call(bow, entity);
        if (entity instanceof AbstractArrow arrow) {
            GameLifecycleHelper.submitContextCall(Fallen.ContextKeys.ARROW_FIRE_CACHE, () -> new ArrowFireCache(arrow, bow));
            arrow.getPersistentData().putBoolean(Fallen.Common.KEY_ARROW_FIRE_CACHE, true);
        }
    }
}
