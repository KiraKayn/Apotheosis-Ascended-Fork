package net.kayn.fallen_gems_affixes.mixin;

import dev.shadowsoffire.placebo.reload.DynamicHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = DynamicHolder.class, remap = false)
public interface DynamicHolderAccessor {
    @Invoker("unbind")
    void invokeUnbind();
    @Invoker("bind")
    void invokeBind();
}
