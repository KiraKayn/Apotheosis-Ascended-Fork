package net.kayn.fallen_gems_affixes.util;

import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.rtxyd.fallen.lib.runtime.forgemod.util.IHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ArrowFireCache implements IHolder<AbstractArrow, ItemStack> {
    private final AbstractArrow arrow;
    private final ItemStack bow;

    public ArrowFireCache(AbstractArrow arrow, @NotNull ItemStack bow) {
        this.arrow = arrow;
        this.bow = bow;
    }

    @Nullable
    public AbstractArrow getArrow() {
        return arrow;
    }

    public ItemStack getBow() {
        return bow;
    }

    @Override
    @Deprecated
    public AbstractArrow fallen_lib$getClassifier() {
        return arrow;
    }

    @Override
    @Deprecated
    public ItemStack get() {
        return bow;
    }
}
