package net.kayn.fallen_gems_affixes.attachment.permanent_effect_v2;

import net.kayn.fallen_gems_affixes.network.ClientlikeUpdatePermanentEffectPacket;
import net.kayn.fallen_gems_affixes.types.IEffectHandler;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class PermanentEffectHandler implements IEffectHandler {
    private final LivingEntity entity;

    public PermanentEffectHandler(LivingEntity entity) {
        this.entity = entity;
    }

    @Override
    public MobEffectInstance addEffectRet(MobEffectInstance effectInstance) {
        this.entity.addEffect(effectInstance);
        if (this.entity instanceof ServerPlayer player) {
            player.connection.send(new ClientlikeUpdatePermanentEffectPacket(effectInstance.getEffect(), effectInstance.getAmplifier(), false));
        }
        return effectInstance;
    }

    @Override
    public void addEffectSilent(MobEffectInstance effectInstance) {
        // No client sync
        this.entity.addEffect(effectInstance);
    }

    @Override
    public MobEffectInstance removeEffectRet(Holder<MobEffect> effect) {
        MobEffectInstance inst = this.entity.getEffect(effect);
        if (inst != null) {
            this.entity.removeEffect(inst.getEffect());
            if (this.entity instanceof ServerPlayer player) {
                player.connection.send(new ClientlikeUpdatePermanentEffectPacket(effect, inst.getAmplifier(), true));
            }
        }
        return inst;
    }

    @Override
    public void removeEffectNoSync(Holder<MobEffect> effect) {
        MobEffectInstance inst = this.entity.getEffect(effect);
        if (inst != null) {
            this.entity.removeEffect(inst.getEffect());
        }
    }

    @Override
    public MobEffectInstance removeEffectRet(Holder<MobEffect> effect, int amplifier) {
        MobEffectInstance inst = this.entity.getEffect(effect);
        if (inst != null && inst.getAmplifier() == amplifier) {
            this.entity.removeEffect(inst.getEffect());
            if (this.entity instanceof ServerPlayer player) {
                player.connection.send(new ClientlikeUpdatePermanentEffectPacket(effect, amplifier, true));
            }
            return inst;
        }
        return null;
    }
}
