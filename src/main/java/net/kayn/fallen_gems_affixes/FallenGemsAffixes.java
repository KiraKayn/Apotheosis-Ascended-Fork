package net.kayn.fallen_gems_affixes;

import dev.shadowsoffire.apotheosis.affix.AffixRegistry;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.apothic_attributes.api.ALObjects;
import dev.shadowsoffire.apothic_attributes.compat.CurioEquipmentSlot;
import net.kayn.fallen_gems_affixes.adventure.affix.AdaptiveSpellPowerAffix;
import net.kayn.fallen_gems_affixes.adventure.affix.SpellEffectAffix;
import net.kayn.fallen_gems_affixes.adventure.socket.gem.bonus.*;
import net.kayn.fallen_gems_affixes.attachment.permanent_effect_v2.PermanentEffectCapability;
import net.kayn.fallen_gems_affixes.attachment.permanent_effect_v2.PermanentEffectCommands;
import net.kayn.fallen_gems_affixes.attributes.AAAttributes;
import net.kayn.fallen_gems_affixes.attributes.MaxHealthDamageHandler;
import net.kayn.fallen_gems_affixes.config.ModConfig;
import net.kayn.fallen_gems_affixes.event.SpellEventHandler;
import net.kayn.fallen_gems_affixes.init.loot.ModLootModifier;
import net.kayn.fallen_gems_affixes.loot.LootCategories;
import net.kayn.fallen_gems_affixes.network.ClientlikeClearPermanentEffectPacket;
import net.kayn.fallen_gems_affixes.network.ClientlikeUpdatePermanentEffectPacket;
import net.kayn.fallen_gems_affixes.util.CodecUtil;
import net.kayn.fallen_gems_affixes.util.EquipmentSlotWrapper;
import net.kayn.fallen_gems_affixes.util.EquipmentSlotWrappers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.stream.Collectors;

@Mod(FallenGemsAffixes.MOD_ID)
public class FallenGemsAffixes {
    public static final String MOD_ID = "fallen_gems_affixes";
    public static final Logger LOGGER = LogManager.getLogger();

    public FallenGemsAffixes(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Loading Fallen Gems & Affixes");

        modContainer.registerConfig(net.neoforged.fml.config.ModConfig.Type.COMMON, ModConfig.SPEC);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerCapabilities);
        NeoForge.EVENT_BUS.addListener(this::registerCommands);
        modEventBus.addListener(this::registerPackets);
        modEventBus.addListener(this::postRegister);
//        modEventBus.addListener(InitNewCodecs::init);

        ModLootModifier.LOOT_MODIFIERS.register(modEventBus);
        AAAttributes.ATTRIBUTES.register(modEventBus);

        LootCategories.bootstrap(modEventBus);
        Fallen.bootstrap(modEventBus);

        AALootCategories.init();
        new MaxHealthDamageHandler();

        // Register EffectsTickEvent.Pre reflectively if fallen.lib is available at runtime.
        try {
            Class<?> preClass = Class.forName("net.rtxyd.fallen.lib.runtime.forgemod.addon.minecraft.mob_effect.EffectsTickEvent$Pre");
            try {
                var getEntity = preClass.getMethod("getEntity");
                java.util.function.Consumer<Object> consumer = (ev) -> {
                    try {
                        Object entityObj = getEntity.invoke(ev);
                        if (entityObj instanceof ServerPlayer player) {
                            PermanentEffectCapability cap = player.getCapability(Fallen.Capabilities.PE_CAP);
                            if (cap != null) {
                                cap.getContainer().forEachEffect((effect, levels) -> {
                                    if (!player.hasEffect(effect)) {
                                        cap.addEffectSilent(effect, levels.getLast());
                                    }
                                });
                            }
                        }
                    } catch (Throwable t) {
                        LOGGER.warn("Failed to handle EffectsTickEvent.Pre reflectively", t);
                    }
                };
                try {
                    var addListenerMethod = NeoForge.EVENT_BUS.getClass().getMethod("addListener", Class.class, java.util.function.Consumer.class);
                    addListenerMethod.invoke(NeoForge.EVENT_BUS, preClass, consumer);
                } catch (Throwable t) {
                    LOGGER.warn("Failed to register reflective EffectsTickEvent.Pre listener", t);
                }
            } catch (NoSuchMethodException nsme) {
                LOGGER.warn("EffectsTickEvent.Pre found but lacks getEntity() method", nsme);
            }
        } catch (ClassNotFoundException ignored) {
            // fallen.lib not present; nothing to register
        }

        if (ModList.get().isLoaded("irons_spellbooks")) {
            if (!ModList.get().isLoaded("irons_apothic")) {
                modEventBus.addListener(AdaptiveSpellPowerAffix::loadingIronsItemsFromConfig);
            }
            NeoForge.EVENT_BUS.addListener(SpellEventHandler::onSpellHeal);
            NeoForge.EVENT_BUS.addListener(SpellEventHandler::onSpellDamage);
        }
//        if (ModList.get().isLoaded("celestisynth")) {
//            CelestialLootCategory.CELESTIAL_WEAPONS.toString();
//            NeoForge.EVENT_BUS.register(SolarisSpellPowerPatch.class);
//            NeoForge.EVENT_BUS.register(CrescentiaSpellPowerPatch.class);
//            NeoForge.EVENT_BUS.register(BreezebreakerSpellPowerPatch.class);
//            NeoForge.EVENT_BUS.register(KeresSpellPowerPatch.class);
//            NeoForge.EVENT_BUS.register(AquafloraSpellPowerPatch.class);
//            NeoForge.EVENT_BUS.register(PoltergeistSpellPowerPatch.class);
//            NeoForge.EVENT_BUS.register(RainfallSpellPowerPatch.class);
//            NeoForge.EVENT_BUS.register(FrostboundSpellPowerPatch.class);
//            NeoForge.EVENT_BUS.register(CelestisynthAttributeHandler.class);
//        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            GemBonus.CODEC.register(ResourceLocation.fromNamespaceAndPath("fallen_gems_affixes", "multi_effect"), MultiEffectBonus.CODEC);
            GemBonus.CODEC.register(ResourceLocation.fromNamespaceAndPath("fallen_gems_affixes", "attribute_effect"), AttributeEffectBonus.CODEC);
            GemBonus.CODEC.register(ResourceLocation.fromNamespaceAndPath("fallen_gems_affixes", "permanent_effect"), PermanentEffectBonus.CODEC);
            GemBonus.CODEC.register(ResourceLocation.fromNamespaceAndPath("fallen_gems_affixes", "boss_slayer"), BossSlayerBonus.CODEC);
            GemBonus.CODEC.register(ResourceLocation.fromNamespaceAndPath("fallen_gems_affixes", "con_cat_bonus"), CodecUtil.CONDITIONAL_CAT_CODEC);
            AffixRegistry.INSTANCE.registerCodec(ResourceLocation.fromNamespaceAndPath("fallen_gems_affixes", "con_affix_type"), CodecUtil.CONDITIONAL_AFFIX_TYPE_CODEC);
            if (ModList.get().isLoaded("irons_spellbooks")) {
                GemBonus.CODEC.register(ResourceLocation.fromNamespaceAndPath("fallen_gems_affixes", "spell_effect"), SpellEffectBonus.CODEC);
                AffixRegistry.INSTANCE.registerCodec(ResourceLocation.fromNamespaceAndPath("fallen_gems_affixes", "adaptive_spell_power"), AdaptiveSpellPowerAffix.CODEC);
                AffixRegistry.INSTANCE.registerCodec(ResourceLocation.fromNamespaceAndPath("fallen_gems_affixes", "spell_effect"), SpellEffectAffix.CODEC);
            }
        });
    }

    private void registerPackets(RegisterPayloadHandlersEvent event) {
        event.registrar(ClientlikeUpdatePermanentEffectPacket.version)
                .playToClient(ClientlikeUpdatePermanentEffectPacket.TYPE, ClientlikeUpdatePermanentEffectPacket.STREAM_CODEC, ClientlikeUpdatePermanentEffectPacket::handleClient);

        event.registrar(ClientlikeClearPermanentEffectPacket.version)
                .playToClient(ClientlikeClearPermanentEffectPacket.TYPE, ClientlikeClearPermanentEffectPacket.STREAM_CODEC, ClientlikeClearPermanentEffectPacket::handleClient);
    }

    private void registerCommands(RegisterCommandsEvent event) {
        PermanentEffectCommands.register(event.getDispatcher(), event.getBuildContext());
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
            event.registerEntity(Fallen.Capabilities.PE_CAP, entityType, (entity, ctx) -> {
                if (entity instanceof Player player) {
                    return new PermanentEffectCapability(player);
                }
                return null;
            });
        }
    }

    private void postRegister(FMLLoadCompleteEvent event) {
        if (ModList.get().isLoaded("curios")) {
            EquipmentSlotWrappers.curioWrappers.putAll(
                    ALObjects.BuiltInRegs.ENTITY_EQUIPMENT_SLOT.holders()
                            .filter(a -> a.getDelegate().isBound() && a.getDelegate().value() instanceof CurioEquipmentSlot)
                            .map(a -> new EquipmentSlotWrapper(null, ((CurioEquipmentSlot) a.getDelegate().value()).curioType(), a.getDelegate()))
                            .collect(Collectors.toMap(EquipmentSlotWrapper::toString, Function.identity()))
            );
        }
    }


    public static ResourceLocation id(@NotNull String path) {
        return ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, path);
    }
}