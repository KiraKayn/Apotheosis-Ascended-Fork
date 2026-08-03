package net.kayn.fallen_gems_affixes.mixin.iss;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import net.kayn.fallen_gems_affixes.adventure.affix.ConcentrationAffix;
import net.kayn.fallen_gems_affixes.adventure.affix.SpellCastAffix;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(value = AbstractSpell.class, remap = false)
public class AbstractSpellMixin {

    @Inject(method = "canBeInterrupted", at = @At("RETURN"), cancellable = true)
    private void fga$preventInterrupt(@Nullable Player player, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue() && player != null) {
            if (ConcentrationAffix.hasConcentration(player)) {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "attemptInitiateCast", at = @At("HEAD"), cancellable = true)
    private void fga$bypassCastingForProcs(ItemStack stack, int spellLevel, Level level, Player player,
                                           CastSource castSource, boolean triggerCooldown,
                                           String castingEquipmentSlot, CallbackInfoReturnable<Boolean> cir) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            if (SpellCastAffix.isCurrentlyTriggering(player)) {
                AbstractSpell spell = (AbstractSpell) (Object) this;

                spell.castSpell(level, spellLevel, serverPlayer, castSource, false);

                cir.setReturnValue(true);
            }
        }
    }
}