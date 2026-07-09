package net.kayn.fallen_gems_affixes;

import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixInstance;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.kayn.fallen_gems_affixes.adventure.set.SetAffixRegistry;
import net.kayn.fallen_gems_affixes.attachment.augment.AugmentRecipeSerializer;
import net.kayn.fallen_gems_affixes.attachment.augment.AugmentRegistry;
import net.kayn.fallen_gems_affixes.attachment.rarity.ClientLikeSyncFallenRarityPacket;
import net.kayn.fallen_gems_affixes.attachment.rarity.FallenRarity;
import net.kayn.fallen_gems_affixes.attachment.rarity.FallenRarityRegistry;
import net.kayn.fallen_gems_affixes.augment.*;
import net.kayn.fallen_gems_affixes.network.ClientLikeSyncAugmentPacket;
import net.kayn.fallen_gems_affixes.recipe.*;
import net.kayn.fallen_gems_affixes.types.augment.IAugment;
import net.kayn.fallen_gems_affixes.util.MiscUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.rtxyd.fallen.lib.runtime.forgemod.network.Connection;
import net.rtxyd.fallen.lib.runtime.forgemod.util.GameLifecycleHelper;
import net.rtxyd.fallen.lib.util.call.ContextKey;

import java.util.Map;
import java.util.Set;

public class Fallen {
    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, FallenGemsAffixes.MOD_ID);

    public static class ContextKeys {
        public static final ContextKey<Level> AUG_RECIPE_LEVEL = GameLifecycleHelper.registerContextKey("fga.augment_recipe.level");
        public static final ContextKey<Container> AUG_RECIPE_CONTAINER = GameLifecycleHelper.registerContextKey("fga.augment_recipe.container");
        public static final ContextKey<Map<DynamicHolder<? extends Affix>, AffixInstance>> AFFIXES_HOLER = GameLifecycleHelper.registerContextKey("fga.special_event.affixes_holder");

        public static final ContextKey<Set<FallenRarity>> FALLEN_RARITIES = GameLifecycleHelper.registerContextKey("fga.reload.fallen_rarities");
        public static final ContextKey<Void> DELAYED_RARITY_REGISTER = GameLifecycleHelper.registerContextKey("fga.reload.delayed_rarity");
        public static final ContextKey<AffixInstance> APPLIED_AFFIX = GameLifecycleHelper.registerContextKey("fga.affix.applied_affix");
        public static final ContextKey<AffixInstance> REROLLED_AFFIX = GameLifecycleHelper.registerContextKey("fga.affix.rerolled_affix");
        public static final ContextKey<AffixInstance> REROLLED_REMOVE = GameLifecycleHelper.registerContextKey("fga.affix.rerolled_remove");

        public static void register() {}
    }

    public static void bootstrap(IEventBus bus) {
        RecipeSerializers.bootstrap(bus);
        Augments.bootstrap();
        AugmentMisc.bootstrap();
        Registries.bootstrap();
        ContextKeys.register();
    }

    public static class Registries {
        public static final AugmentRegistry AUGMENT_REGISTRY = new AugmentRegistry();
        public static final FallenRarityRegistry RARITY_REGISTRY = new FallenRarityRegistry();
        public static final SetAffixRegistry SET_AFFIX_REGISTRY = new SetAffixRegistry();

        public static void bootstrap() {
            Connection.registerDefaultPacketBoundRegistry(SET_AFFIX_REGISTRY);
            Connection.registerRegistryBoundPacketPayloads(AUGMENT_REGISTRY, ClientLikeSyncAugmentPacket.BUF_CODEC,
                    ClientLikeSyncAugmentPacket.Begin.class, ClientLikeSyncAugmentPacket.Begin::new, ClientLikeSyncAugmentPacket.Begin::handle,
                    ClientLikeSyncAugmentPacket.class, ClientLikeSyncAugmentPacket::new, ClientLikeSyncAugmentPacket::handle,
                    ClientLikeSyncAugmentPacket.End.class, ClientLikeSyncAugmentPacket.End::new, ClientLikeSyncAugmentPacket.End::handle);

            for (IAugment augment : AUGMENT_REGISTRY.registryView().values()) {
                AUGMENT_REGISTRY.registerCodec(augment.getId(), augment.getMetaDataCodec());
            }

            Connection.registerRegistryBoundPacketPayloads(RARITY_REGISTRY, ClientLikeSyncFallenRarityPacket.BUF_CODEC,
                    ClientLikeSyncFallenRarityPacket.Begin.class, ClientLikeSyncFallenRarityPacket.Begin::new, ClientLikeSyncFallenRarityPacket.Begin::handle,
                    ClientLikeSyncFallenRarityPacket.class, ClientLikeSyncFallenRarityPacket::new, ClientLikeSyncFallenRarityPacket::handle,
                    ClientLikeSyncFallenRarityPacket.End.class, ClientLikeSyncFallenRarityPacket.End::new, ClientLikeSyncFallenRarityPacket.End::handle);
        }
    }

    public static class RecipeSerializers {
        public static final RegistryObject<AugmentRecipeSerializer> ADD_AUGMENT =
                SERIALIZERS.register("add_augment",
                        AugmentRecipeSerializer::new);

        public static final RegistryObject<RecipeSerializer<AugmentCraftingRecipe>> AUGMENT_CRAFTING =
                SERIALIZERS.register("augment_crafting",
                        AugmentCraftingRecipe.Serializer::new);

        public static final RegistryObject<RecipeSerializer<SocketConversionRecipe>> SOCKET_CONVERSION =
                SERIALIZERS.register("socket_conversion",
                        MiscUtil.simpleRecipeSerializer(SocketConversionRecipe::new));

        public static final RegistryObject<RecipeSerializer<TransmutationRecipe>> TRANSMUTATION =
                SERIALIZERS.register("transmutation",
                        MiscUtil.simpleRecipeSerializer(TransmutationRecipe::new));

        public static final RegistryObject<RecipeSerializer<SeveranceRecipe>> SEVERANCE =
                SERIALIZERS.register("severance",
                        MiscUtil.simpleRecipeSerializer(SeveranceRecipe::new));

        public static final RegistryObject<RecipeSerializer<ErasureRecipe>> ERASURE =
                SERIALIZERS.register("erasure",
                        MiscUtil.simpleRecipeSerializer(ErasureRecipe::new));
        public static final RegistryObject<RecipeSerializer<ElevationRecipe>> SOCKET_ELEVATION =
                SERIALIZERS.register("elevation",
                        MiscUtil.simpleRecipeSerializer(ElevationRecipe::new));
        public static final RegistryObject<RecipeSerializer<PrismaticConversionRecipe>> PRISMATIC_CONVERSION =
                SERIALIZERS.register("prismatic_conversion",
                        MiscUtil.simpleRecipeSerializer(PrismaticConversionRecipe::new));
        public static final RegistryObject<RecipeSerializer<ConfluenceRecipe>> CONFLUENCE =
                SERIALIZERS.register("confluence",
                        MiscUtil.simpleRecipeSerializer(ConfluenceRecipe::new));

        private static void bootstrap(IEventBus bus) {
            SERIALIZERS.register(bus);
        }
    }

    public static class AugmentMisc {
        public static final ResourceLocation AUGMENT_CAP_ID = ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "augment_cap");
        public static final ResourceLocation FABLED_ID = ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "fabled");
        // Not a tag key
        public static final String ADDITIONAL_SOCKET_AFFIX_CACHE = "fallen_gems_affixes:additional_socket";
        /**
         * used for augment cache
         */
        public static final String AUGMENT_CACHED_OBJECT = "fallen_gems_affixes:augments";
        /**
         * used to refresh affixes manually
         */
        public static final String AFFIXES_REFRESH_MARKER = "fga:refresh_marker";
        /**
         * used to store to modify affixes for affix power
         */
        public static final String TO_MODIFY_AFFIXES_OBJECT = "fallen_gems_affixes:tm_affixes";
        /**
         * used to store to modify affixes version for cache refresh
         */
        public static final String TO_MODIFY_AFFIXES_VERSION = "fga:tma_version";
        /**
         * fabled tag
         */
        public static final String FABLED_TAG = "fallen_gems_affixes:fabled";
        // Top-level tags
        /**
         * stores all augment data for ordinary item stacks
         */
        public static final String AUGMENT_DATA = "fallen_gems_affixes:augment_data";
        /**
         * stores affix data for inverting affix power or do something else
         */
        public static final String MODIFIER_DATA = "fallen_gems_affixes:modifier_data";
        /**
         * stores augment id for {@link net.kayn.fallen_gems_affixes.item.augments.AugmentItem}
         */
        public static final String AUGMENT_ID_TAG = "AugmentId";
        // 2nd-level tags
        /**
         * stores slot number
         */
        public static final String AUGMENT_SLOTS = "augment_slots";
        /**
         * a list as augment container of compound tag
         */
        public static final String AUGMENTS = "augments";
        // 3nd-level tags
        /**
         * a compound tag element to store Augment id
         */
        public static final String TYPE = "type";
        /**
         * a compound tag element to store Augment inner data
         */
        public static final String INNER_DATA = "inner_data";
        /**
         * a compound tag element to store Augment uuid,
         * it's intended to function on augment that needs to attach to entity.
         */
        public static final String UNIQUE_ID = "uuid";

        /**
         * Marks an item as the product of a Sigil of Confluence merge.
         * Items carrying this tag cannot receive augments.
         */
        public static final String AFFIX_COMBINED = "fallen_gems_affixes:affix_combined";

        public static void bootstrap() {
        }
    }

    public static class Augments {
        public static final IAugment GEM_POWER = Registries.AUGMENT_REGISTRY.register(new GemPowerAugment());
        public static final IAugment SUPREMACY = Registries.AUGMENT_REGISTRY.register(new SupremacyAugment());
        public static final IAugment GENESIS = Registries.AUGMENT_REGISTRY.register(new GenesisAugment());
        public static final IAugment CASCADE = Registries.AUGMENT_REGISTRY.register(new CascadeAugment());
        public static final IAugment DUALITY = Registries.AUGMENT_REGISTRY.register(new DualityAugment());
        public static final IAugment MALICE = Registries.AUGMENT_REGISTRY.register(new MaliceAugment());
        public static final IAugment CONVERGENCE = Registries.AUGMENT_REGISTRY.register(new ConvergenceAugment());

        public static void bootstrap() {

        }
    }
}