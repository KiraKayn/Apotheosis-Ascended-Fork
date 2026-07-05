package net.kayn.fallen_gems_affixes.config;

import net.kayn.fallen_gems_affixes.adventure.socket.TieredSocketMode;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.List;

public class ModConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.BooleanValue ENABLE_SOCKET_GEM_MODIFIER;
    public static final ForgeConfigSpec.DoubleValue SOCKET_GEM_CHANCE;
    public static final ForgeConfigSpec.BooleanValue ENABLE_SOCKET_MIXIN;
    public static final ForgeConfigSpec.BooleanValue PERMANENT_EFFECT_USE_TICK_EVENT;
    public static final ForgeConfigSpec.BooleanValue STRICT_SCHOOL_MATCH;
    public static final ForgeConfigSpec.BooleanValue ENABLE_CELESTISYNTH_ATTRIBUTES;
    public static final ForgeConfigSpec.BooleanValue ENABLE_SPELL_POWER_PATCH;
    public static final ForgeConfigSpec.ConfigValue<List<String>> IRONS_ITEMS_MAP;
    public static final ForgeConfigSpec.IntValue MAX_AUGMENT_SLOTS;
    public static final ForgeConfigSpec.BooleanValue ENABLE_BOSS_SPAWNER_CONVERSION;
    public static final ForgeConfigSpec.DoubleValue AFFIX_SCROLL_DROP_CHANCE;
    public static final ForgeConfigSpec.IntValue AFFIX_SCROLL_XP_COST;
    public static final ForgeConfigSpec.IntValue MAX_SCROLL_SLOTS;
    public static final ForgeConfigSpec.BooleanValue SHOW_BOSS_RARITY_NAME;
    public static final ForgeConfigSpec.IntValue EXTRA_SOCKETS;
    public static final ForgeConfigSpec.EnumValue<TieredSocketMode> TIERED_SOCKET_MODE;
    public static final ForgeConfigSpec.DoubleValue FABLED_AUGMENT_DROP_CHANCE;
    public static final ForgeConfigSpec.BooleanValue STRICT_UNIVERSAL_BOSS_ENCHANT_DROP;

    static {
        BUILDER.push("Mod Config");

        ENABLE_SOCKET_GEM_MODIFIER = BUILDER
                .comment("Enable gem socket injection into items via loot modifier")
                .define("enableSocketGemInjector", true);

        SOCKET_GEM_CHANCE = BUILDER
                .comment("Chance to insert a random gem into each socket slot, 1.0 means 100%")
                .defineInRange("socketGemChance", 0.3, 0.0, 1.0);

        ENABLE_SOCKET_MIXIN = BUILDER
                .comment("Enable the SocketHelperMixin that allows gaps between sockets")
                .define("enableSocketHelperMixin", true);

        PERMANENT_EFFECT_USE_TICK_EVENT = BUILDER
                .comment("Switch the implementation type of PermanentEffectBonus")
                .comment("If true, switches to Player Tick event. Better compatibility, worse performance.")
                .comment("If false, uses default impl. More performant, less compatible.")
                .define("permanentEffectUseTickEvent", false);

        STRICT_SCHOOL_MATCH = BUILDER
                .comment("If false, Adaptive Spell Power Affixes can apply to any compatible item regardless of spell school.")
                .comment("If true, affixes will only apply to items that already grant spell power of the matching school.")
                .define("strictSpellSchoolMatching", true);

        ENABLE_CELESTISYNTH_ATTRIBUTES = BUILDER
                .comment("If true, applies Spell Power attributes to Celestisynth weapons via ItemAttributeModifierEvent.")
                .define("enableCelestisynthAttributes", true);

        ENABLE_SPELL_POWER_PATCH = BUILDER
                .comment("If true, enables Celestisynth weapons patch to increase weapon damage when held, scaled by the respective Spell Power on the item")
                .define("enableCelestisynthSpellPowerPatch", true);

        MAX_AUGMENT_SLOTS = BUILDER
                .comment("Maximum number of Augment Slots an item can have")
                .defineInRange("maxAugmentSlots", 1, 0, 4);

        ENABLE_BOSS_SPAWNER_CONVERSION = BUILDER
                .comment("If true, natural spawners have a chance to convert into Apotheosis boss spawners on first discovery.")
                .define("enableBossSpawnerConversion", true);

        AFFIX_SCROLL_DROP_CHANCE = BUILDER
                .comment("Chance for a universal boss to drop an Affix Scroll on death (0.0 = never, 1.0 = always)")
                .defineInRange("affixScrollDropChance", 0.05, 0.0, 1.0);

        AFFIX_SCROLL_XP_COST = BUILDER
                .comment("XP level cost to apply an Affix Scroll in the anvil")
                .defineInRange("affixScrollXpCost", 15, 1, 100);

        MAX_SCROLL_SLOTS = BUILDER
                .comment("Maximum number of Affix Scrolls that can be applied to a single item")
                .defineInRange("maxScrollSlots", 2, 0, 10);

        SHOW_BOSS_RARITY_NAME = BUILDER
                .comment("If true, universal bosses will have their rarity name prefixed (e.g. 'Mythic Zombie').")
                .define("showUniversalBossRarityName", true);

        EXTRA_SOCKETS = BUILDER
                .comment(
                        "Adds extra sockets to ALL items with a valid LootCategory.",
                        "This value is added on top of existing sockets.",
                        "Set to 0 to disable."
                )
                .defineInRange("extraSockets", 0, 0, 10);

        TIERED_SOCKET_MODE = BUILDER
                .comment(
                        "Tiered Socket Mode – controls gem-socket compatibility at the smithing table.",
                        "ON       - A gem of equal or LOWER tier may be placed in a higher-tier socket",
                        "           (e.g. a Common gem can go into an Epic socket).",
                        "HARDCORE - Only a gem of the EXACT same tier fits the socket",
                        "           (e.g. only an Epic gem can go into an Epic socket).",
                        "",
                        "To disable tiered sockets entirely, remove or empty the socket tier datapack folder",
                        "When no tier definitions are loaded all sockets behave as plain Apotheosis sockets."
                )
                .defineEnum("tiered_socket_mode", TieredSocketMode.ON);

        FABLED_AUGMENT_DROP_CHANCE = BUILDER
                .comment("Chance for a Fabled universal boss to drop a random Augment on death (0.0 = never, 1.0 = always)")
                .defineInRange("fabledAugmentDropChance", 0.05, 0.0, 1.0);

        STRICT_UNIVERSAL_BOSS_ENCHANT_DROP = BUILDER
                .comment("Use vanilla check for enchanting book drops, unobtainable books / treasure books will be excluded",
                         "If you want more fine-grained control, see data file located at data/fallen_gems_affixes/loot_modifiers/universal_boss_loot.json")
                .define("strictUniversalBoosEnchantDrop", false);

        IRONS_ITEMS_MAP = BUILDER
                .comment("Map of item Resource Locations to School Type IDs for Adaptive Spell Power Affixes")
                .comment("Example: modid:itemid|modid:schoolid")
                .comment("You can also set multiple schools: modid:itemid|modid:schoolid|modid:schoolid")
                .define("irons_items", new ArrayList<>(List.of(
                        "celestisynth:crescentia|irons_spellbooks:ender",
                        "celestisynth:solaris|irons_spellbooks:fire",
                        "celestisynth:aquaflora|irons_spellbooks:nature",
                        "celestisynth:breezebreaker|irons_spellbooks:evocation",
                        "celestisynth:poltergeist|irons_spellbooks:eldritch",
                        "celestisynth:rainfall_serenity|irons_spellbooks:lightning",
                        "celestisynth:keres|irons_spellbooks:blood",
                        "celestisynth:frostbound|irons_spellbooks:ice"
                )));

        BUILDER.pop();
    }

    public static final ForgeConfigSpec SPEC = BUILDER.build();
}