package net.kayn.fallen_gems_affixes.attachment.permanent_effect_v2;

import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.types.IEffectHandler;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import java.util.Set;

import static net.kayn.fallen_gems_affixes.Fallen.Capabilities.PE_CAP;

public class PermanentEffectCapability {

    final LivingEntity entity;
    final PermanentEffectContainer pEContainer;
    final IEffectHandler effectHandler;

    public PermanentEffectCapability(LivingEntity entity) {
        this.entity = entity;
        this.pEContainer = entity.getData(Fallen.PE_CONTAINER);
        this.effectHandler = new PermanentEffectHandler(entity);
    }

    public IEffectHandler getEffectHandler() {
        return this.effectHandler;
    }

    public LivingEntity getEntity() {
        return this.entity;
    }

    public Set<Holder<MobEffect>> getEffects() {
        return this.pEContainer.getEffects();
    }

    public MobEffectInstance addEffect(Holder<MobEffect> effect, int amplifier) {
        MobEffectInstance inst = this.effectHandler.addEffectRet(new MobEffectInstance(effect, -1, amplifier));
        this.pEContainer.addEffect(effect, amplifier);
        return inst;
    }

    public void addEffectSilent(Holder<MobEffect> effect, int amplifier) {
        this.effectHandler.addEffectSilent(new MobEffectInstance(effect, -1, amplifier));
    }

    public MobEffectInstance removeEffect(Holder<MobEffect> effect, int amplifier) {
        int level = this.pEContainer.tryRemoveEffect(effect, amplifier);
        return level >= 0 ? this.effectHandler.removeEffectRet(effect, amplifier) : null;
    }

    public boolean containsEffect(Holder<MobEffect> effect) {
        return this.pEContainer.containsEffect(effect);
    }

    public PermanentEffectContainer getContainer() {
        return this.pEContainer;
    }

    public static boolean clearEffects(LivingEntity entity) {
        PermanentEffectCapability cap = entity.getCapability(PE_CAP);
        if (cap != null) {
            cap.pEContainer.clearEffects();
            return true;
        }
        return false;
    }
}
