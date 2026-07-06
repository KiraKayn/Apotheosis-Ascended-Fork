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

public class AquafloraSpellPowerPatch {

    private static final boolean HAS_CELESTISYNTH = ModList.get().isLoaded("celestisynth");
    private static final boolean HAS_IRONS = ModList.get().isLoaded("irons_spellbooks");

    private static final ResourceLocation AQUAFLORA_ID = ResourceLocation.fromNamespaceAndPath("celestisynth", "aquaflora");
    private static final ResourceLocation NATURE_SPELL_POWER_ID = ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "nature_spell_power");
    private static final Lazy<Attribute> NATURE_SPELL_POWER = Lazy.of(() -> {
        if (!HAS_IRONS) return null;
        return ForgeRegistries.ATTRIBUTES.getValue(NATURE_SPELL_POWER_ID);
    });

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!ModConfig.ENABLE_SPELL_POWER_PATCH.get()) return;
        if (!HAS_CELESTISYNTH || !HAS_IRONS) return;

        DamageSource source = event.getSource();
        if (!(source.getEntity() instanceof Player player)) return;

        ItemStack held = player.getMainHandItem();
        if (!held.is(ForgeRegistries.ITEMS.getValue(AQUAFLORA_ID))) return;

        Attribute attr = NATURE_SPELL_POWER.get();
        if (attr == null) return;

        AttributeInstance instance = player.getAttribute(attr);
        if (instance == null) return;

        double value = instance.getValue();
        if (value <= 0) return;

        event.setAmount(event.getAmount() + (event.getAmount() * (float) value));
    }
}