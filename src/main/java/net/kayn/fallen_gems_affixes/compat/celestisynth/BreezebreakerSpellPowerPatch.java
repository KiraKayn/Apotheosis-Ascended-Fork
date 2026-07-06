package net.kayn.fallen_gems_affixes.compat.celestisynth;

import net.kayn.fallen_gems_affixes.config.ModConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

public class BreezebreakerSpellPowerPatch {

    private static final boolean HAS_IRONS = ModList.get().isLoaded("irons_spellbooks");
    private static final ResourceLocation EVOCATION_SPELL_POWER_ID = ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "evocation_spell_power");
    private static final ResourceLocation BREEZEBREAKER_ID = ResourceLocation.fromNamespaceAndPath("celestisynth", "breezebreaker");

    private static final Lazy<Attribute> EVOCATION_SPELL_POWER = Lazy.of(() ->
            HAS_IRONS ? ForgeRegistries.ATTRIBUTES.getValue(EVOCATION_SPELL_POWER_ID) : null
    );

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!ModConfig.ENABLE_SPELL_POWER_PATCH.get()) return;
        if (!HAS_IRONS) return;

        DamageSource source = event.getSource();
        if (!(source.getEntity() instanceof Player player)) return;

        ItemStack held = player.getMainHandItem();
        if (!held.is(ForgeRegistries.ITEMS.getValue(BREEZEBREAKER_ID))) return;

        Attribute attr = EVOCATION_SPELL_POWER.get();
        if (attr == null) return;

        AttributeInstance instance = player.getAttribute(attr);
        if (instance == null) return;

        double value = instance.getValue();
        if (value <= 0) return;

        event.setAmount(event.getAmount() + (event.getAmount() * (float) value));
    }
}