package net.kayn.fallen_gems_affixes;

import dev.shadowsoffire.placebo.util.PlaceboUtil;
import net.kayn.fallen_gems_affixes.adventure.affix.AdaptiveSpellPowerAffix;
import net.kayn.fallen_gems_affixes.adventure.affix.BerserkerAffix;
import net.kayn.fallen_gems_affixes.adventure.affix.CooldownResetAffix;
import net.kayn.fallen_gems_affixes.adventure.affix.FortifyAffix;
import net.kayn.fallen_gems_affixes.adventure.boss.UniversalBossEventHandler;
import net.kayn.fallen_gems_affixes.adventure.entity.EntityAffixEventHandler;
import net.kayn.fallen_gems_affixes.adventure.entity.affix.MobAffixEventHandler;
import net.kayn.fallen_gems_affixes.adventure.reforging.FabledReforging;
import net.kayn.fallen_gems_affixes.adventure.set.SetAffixRegistry;
import net.kayn.fallen_gems_affixes.adventure.set.SetBonusHandler;
import net.kayn.fallen_gems_affixes.adventure.set.colossus.ColossusSetAffixEventHandler;
import net.kayn.fallen_gems_affixes.adventure.set.colossus.bonus.ColossusSetBonusHandler;
import net.kayn.fallen_gems_affixes.adventure.set.deadeye.DeadeyeEntities;
import net.kayn.fallen_gems_affixes.adventure.set.deadeye.DeadeyeSetAffixEventHandler;
import net.kayn.fallen_gems_affixes.adventure.set.deadeye.bonus.DeadeyeSetBonusHandler;
import net.kayn.fallen_gems_affixes.adventure.set.trickster.TricksterEntities;
import net.kayn.fallen_gems_affixes.adventure.set.trickster.TricksterSetAffixEventHandler;
import net.kayn.fallen_gems_affixes.adventure.set.trickster.bonus.TricksterSetBonusHandler;
import net.kayn.fallen_gems_affixes.adventure.socket.gem.bonus.*;
import net.kayn.fallen_gems_affixes.adventure.socket.gem.storage.GemCaseNetwork;
import net.kayn.fallen_gems_affixes.adventure.socket.gem.storage.GemCaseRegistry;
import net.kayn.fallen_gems_affixes.attachment.augment.SpecialAffixEventHandler;
import net.kayn.fallen_gems_affixes.attributes.AAAttributes;
import net.kayn.fallen_gems_affixes.attributes.MaxHealthDamageHandler;
import net.kayn.fallen_gems_affixes.augment.DualityCritModifierHandler;
import net.kayn.fallen_gems_affixes.augment.GemBonusModifier;
import net.kayn.fallen_gems_affixes.augment.GenesisEventHandler;
import net.kayn.fallen_gems_affixes.augment.ItemAllStatsAugmentHandler;
import net.kayn.fallen_gems_affixes.color.FabledColor;
import net.kayn.fallen_gems_affixes.commands.ModCommands;
import net.kayn.fallen_gems_affixes.compat.celestisynth.*;
import net.kayn.fallen_gems_affixes.compat.curios.CuriosSoulboundCompat;
import net.kayn.fallen_gems_affixes.config.ModConfig;
import net.kayn.fallen_gems_affixes.event.*;
import net.kayn.fallen_gems_affixes.init.loot.ModLootModifier;
import net.kayn.fallen_gems_affixes.loot.CelestialLootCategory;
import net.kayn.fallen_gems_affixes.loot.StaffLootCategory;
import net.kayn.fallen_gems_affixes.recipe.AffixScrollIngredient;
import net.kayn.fallen_gems_affixes.registry.ModCreativeTabs;
import net.kayn.fallen_gems_affixes.registry.ModItems;
import net.kayn.fallen_gems_affixes.util.AffixTypeExtender;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

@Mod(FallenGemsAffixes.MOD_ID)
public class FallenGemsAffixes {
    public static final String MOD_ID = "fallen_gems_affixes";
    public static final Logger LOGGER = LogManager.getLogger();
    public static boolean curiosLoaded = false;

    public FallenGemsAffixes(FMLJavaModLoadingContext context) {
        AffixTypeExtender.init();
        IEventBus modEventBus = context.getModEventBus();
        LOGGER.info("Loading Fallen Gems & Affixes");

        // Ensure required library is available
        // now Fallen Lib check can be replaced by a mod.toml setting.

        // Config & setup listeners
        context.registerConfig(Type.COMMON, ModConfig.SPEC);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(InitNewCodecs::init);

        // Registries
        ModLootModifier.LOOT_MODIFIERS.register(modEventBus);
        AAAttributes.ATTRIBUTES.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        GemCaseRegistry.register(modEventBus);

        // Bootstraps
        Fallen.bootstrap(modEventBus);
        GemBonusModifier.bootstrap(MinecraftForge.EVENT_BUS);
        TricksterEntities.bootstrap(modEventBus);
        DeadeyeEntities.bootstrap(modEventBus);
        FabledReforging.bootstrap(modEventBus);
        MinecraftForge.EVENT_BUS.register(SetBonusHandler.class);
        GenesisEventHandler.bootstrap(MinecraftForge.EVENT_BUS);
        SpecialAffixEventHandler.register();
        AALootCategories.init();
        net.minecraftforge.common.crafting.CraftingHelper.register(
                AffixScrollIngredient.ID, AffixScrollIngredient.SERIALIZER);

        // Attributes / handlers
        new MaxHealthDamageHandler();

        // Event bus
        MinecraftForge.EVENT_BUS.register(SoulboundEventHandler.class);
        MinecraftForge.EVENT_BUS.register(FallenEventHandler.class);
        MinecraftForge.EVENT_BUS.register(BowEventHandler.class);
        MinecraftForge.EVENT_BUS.register(new HurtEventHandler());
        MinecraftForge.EVENT_BUS.register(new DualityCritModifierHandler());
        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
        MinecraftForge.EVENT_BUS.register(UniversalBossEventHandler.class);
        MinecraftForge.EVENT_BUS.register(AffixScrollAnvilHandler.class);
        MinecraftForge.EVENT_BUS.register(AfflictedEventHandler.class);
        MinecraftForge.EVENT_BUS.register(BerserkerAffix.class);
        MinecraftForge.EVENT_BUS.register(FortifyAffix.class);
        MinecraftForge.EVENT_BUS.register(PlayerLogoutHandler.class);
        MinecraftForge.EVENT_BUS.register(EntityAffixEventHandler.class);
        MinecraftForge.EVENT_BUS.register(MobAffixEventHandler.class);
        MinecraftForge.EVENT_BUS.register(CatalystSocketEventHandler.class);
        MinecraftForge.EVENT_BUS.register(EchoingStrikeEventHandler.class);
        MinecraftForge.EVENT_BUS.register(TricksterSetAffixEventHandler.class);
        MinecraftForge.EVENT_BUS.register(TricksterSetBonusHandler.class);
        MinecraftForge.EVENT_BUS.register(ColossusSetAffixEventHandler.class);
        MinecraftForge.EVENT_BUS.register(ColossusSetBonusHandler.class);
        MinecraftForge.EVENT_BUS.register(DeadeyeSetAffixEventHandler.class);
        MinecraftForge.EVENT_BUS.register(DeadeyeSetBonusHandler.class);

        // Mod integrations
        curiosLoaded = ModList.get().isLoaded("curios");

        if (ModList.get().isLoaded("irons_spellbooks")) {
            StaffLootCategory.STAFF.toString();
            modEventBus.addListener(AdaptiveSpellPowerAffix::loadingIronsItemsFromConfig);
            MinecraftForge.EVENT_BUS.register(SpellEventHandler.class);
            MinecraftForge.EVENT_BUS.register(CooldownResetAffix.class);
            MinecraftForge.EVENT_BUS.register(VoidHunterEventHandler.class);
            MinecraftForge.EVENT_BUS.register(SpellEchoHandler.class);
            MinecraftForge.EVENT_BUS.register(ArrowTeleSlashEventHandler.class);
            MinecraftForge.EVENT_BUS.register(BloodEruptionEventHandler.class);
            MinecraftForge.EVENT_BUS.register(BloodNovaEventHandler.class);
            MinecraftForge.EVENT_BUS.register(HolyWrathEventHandler.class);
            MinecraftForge.EVENT_BUS.register(HolyMarkEventHandler.class);
        }

        if (ModList.get().isLoaded("celestisynth")) {
            CelestialLootCategory.CELESTIAL_MELEE.toString();
            CelestialLootCategory.CELESTIAL_RANGED.toString();
            MinecraftForge.EVENT_BUS.register(SolarisSpellPowerPatch.class);
            MinecraftForge.EVENT_BUS.register(CrescentiaSpellPowerPatch.class);
            MinecraftForge.EVENT_BUS.register(BreezebreakerSpellPowerPatch.class);
            MinecraftForge.EVENT_BUS.register(KeresSpellPowerPatch.class);
            MinecraftForge.EVENT_BUS.register(AquafloraSpellPowerPatch.class);
            MinecraftForge.EVENT_BUS.register(PoltergeistSpellPowerPatch.class);
            MinecraftForge.EVENT_BUS.register(RainfallSpellPowerPatch.class);
            MinecraftForge.EVENT_BUS.register(FrostboundSpellPowerPatch.class);
            MinecraftForge.EVENT_BUS.register(CelestisynthAttributeHandler.class);
        }
    }

    public void commonSetup(final FMLCommonSetupEvent event) {
        GemCaseNetwork.init();
        // Any common setup logic
        event.enqueueWork(() -> {
            PlaceboUtil.registerCustomColor(FabledColor.FABLED);
            ItemAllStatsAugmentHandler.init();
            if (curiosLoaded) {
                CuriosSoulboundCompat.init();
            }
        });
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher(), event.getBuildContext());
    }

    public static ResourceLocation id(@NotNull String path) {
        return ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, path);
    }
}