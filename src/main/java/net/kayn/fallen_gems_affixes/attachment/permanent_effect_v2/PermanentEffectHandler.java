package net.kayn.fallen_gems_affixes.attachment.permanent_effect_v2;

import net.kayn.fallen_gems_affixes.network.ClientlikeUpdatePermanentEffectPacket;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.rtxyd.fallen.lib.runtime.forgemod.addon.minecraft.mob_effect.VanillaLikeEffectHandler;
import org.jetbrains.annotations.NotNull;

public class PermanentEffectHandler extends VanillaLikeEffectHandler {
    public PermanentEffectHandler(LivingEntity entity) {
        super(entity);
    }

    @Override
    protected @NotNull CustomPacketPayload createAddEffectPacket(Holder<MobEffect> holder, int i) {
        return new ClientlikeUpdatePermanentEffectPacket(holder, i, false);
    }

    @Override
    protected @NotNull CustomPacketPayload createRemoveEffectPacket(Holder<MobEffect> holder, int i) {
        return new ClientlikeUpdatePermanentEffectPacket(holder, i, true);
    }
}
