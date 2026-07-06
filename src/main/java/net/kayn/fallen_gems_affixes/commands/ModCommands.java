package net.kayn.fallen_gems_affixes.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixRegistry;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.adventure.boss.UniversalBossConfig;
import net.kayn.fallen_gems_affixes.adventure.boss.UniversalBossLoader;
import net.kayn.fallen_gems_affixes.attachment.augment.AugmentHelper;
import net.kayn.fallen_gems_affixes.attachment.augment.AugmentInstance;
import net.kayn.fallen_gems_affixes.attachment.augment.AugmentMeta;
import net.kayn.fallen_gems_affixes.attachment.augment.AugmentSlotHelper;
import net.kayn.fallen_gems_affixes.attachment.rarity.FallenRarity;
import net.kayn.fallen_gems_affixes.attachment.rarity.FallenRarityRegistry;
import net.kayn.fallen_gems_affixes.item.AffixScrollItem;
import net.kayn.fallen_gems_affixes.types.augment.IAugment;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static net.kayn.fallen_gems_affixes.Fallen.AugmentMisc.*;

public class ModCommands {

    public static final SuggestionProvider<CommandSourceStack> SUGGEST_APPLICABLE_AUGMENT = (ctx, builder) -> {
        Entity entity = ctx.getSource().getEntity();
        if (entity instanceof ServerPlayer) {
            Stream<String> suggestions = Fallen.Registries.AUGMENT_REGISTRY.registryView().keySet().stream().map(ResourceLocation::toString);
            return SharedSuggestionProvider.suggest(suggestions, builder);
        }
        return SharedSuggestionProvider.suggest(Collections.emptyList(), builder);
    };

    public static final SuggestionProvider<CommandSourceStack> SUGGEST_RARITY = (ctx, builder) ->
            SharedSuggestionProvider.suggest(
                    Fallen.Registries.RARITY_REGISTRY.rarityRegistry.getRarityMapView().keySet().stream().map(ResourceLocation::toString), builder);

    public static final SuggestionProvider<CommandSourceStack> SUGGEST_AFFIX = (ctx, builder) ->
            SharedSuggestionProvider.suggest(
                    AffixRegistry.INSTANCE.getKeys().stream().map(ResourceLocation::toString), builder);

    public static final SuggestionProvider<CommandSourceStack> SUGGEST_ENTITY = SuggestionProviders.SUMMONABLE_ENTITIES;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("fallen_gems_affixes")
                .requires(c -> c.hasPermission(2));

        // augment add
        builder.then(Commands.literal("augment")
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("Augment", ResourceLocationArgument.id()).suggests(SUGGEST_APPLICABLE_AUGMENT)
                                .then(Commands.argument("ExtraData", FloatArgumentType.floatArg(1f))
                                        .executes(c -> addAugmentToMainHandItem(c, FloatArgumentType.getFloat(c, "ExtraData"))))
                                .executes(c -> addAugmentToMainHandItem(c, 1f)))));

        // augment clear
        builder.then(Commands.literal("clearAugment")
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(ModCommands::clearAugments)));

        // scroll give
        builder.then(Commands.literal("scroll")
                .then(Commands.literal("give")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("rarity", ResourceLocationArgument.id()).suggests(SUGGEST_RARITY)
                                        .then(Commands.argument("affix", ResourceLocationArgument.id()).suggests(SUGGEST_AFFIX)
                                                .then(Commands.argument("level", FloatArgumentType.floatArg(0, 1))
                                                        .executes(c -> giveScroll(c,
                                                                ResourceLocationArgument.getId(c, "rarity"),
                                                                ResourceLocationArgument.getId(c, "affix"),
                                                                FloatArgumentType.getFloat(c, "level"))))
                                                .executes(c -> giveScroll(c,
                                                        ResourceLocationArgument.getId(c, "rarity"),
                                                        ResourceLocationArgument.getId(c, "affix"),
                                                        0.5f)))))));

        // spawn empowered entity
        builder.then(Commands.literal("spawn")
                .then(Commands.argument("entity", ResourceLocationArgument.id())
                        .suggests(SUGGEST_ENTITY)
                        .then(Commands.argument("rarity", ResourceLocationArgument.id()).suggests(SUGGEST_RARITY)
                                .then(Commands.argument("pos", Vec3Argument.vec3())
                                        .executes(c -> spawnEmpowered(c,
                                                ResourceLocationArgument.getId(c, "entity"),
                                                ResourceLocationArgument.getId(c, "rarity"),
                                                Vec3Argument.getVec3(c, "pos"))))
                                .executes(c -> spawnEmpowered(c,
                                        ResourceLocationArgument.getId(c, "entity"),
                                        ResourceLocationArgument.getId(c, "rarity"),
                                        null)))));

        dispatcher.register(builder);
    }

    private static int giveScroll(CommandContext<CommandSourceStack> ctx,
                                  ResourceLocation rarityId,
                                  ResourceLocation affixId,
                                  float level) throws CommandSyntaxException {
        DynamicHolder<LootRarity> rarityHolder = RarityRegistry.INSTANCE.holder(rarityId);
        if (!rarityHolder.isBound()) {
            ctx.getSource().sendFailure(Component.literal("Unknown rarity: " + rarityId));
            return -1;
        }

        DynamicHolder<Affix> affixHolder = AffixRegistry.INSTANCE.holder(affixId);
        if (!affixHolder.isBound()) {
            ctx.getSource().sendFailure(Component.literal("Unknown affix: " + affixId));
            return -2;
        }

        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        ItemStack scroll = AffixScrollItem.createScroll(affixId, rarityHolder.get(), level);

        if (!player.addItem(scroll)) {
            player.drop(scroll, false);
        }

        ctx.getSource().sendSuccess(() -> Component.literal(
                "Gave " + player.getName().getString() +
                        " a scroll: " + affixId.getPath() +
                        " (" + rarityId.getPath() + ", level " + String.format("%.2f", level) + ")"), true);
        return 1;
    }

    private static int spawnEmpowered(CommandContext<CommandSourceStack> ctx,
                                      ResourceLocation entityId,
                                      ResourceLocation rarityId,
                                      Vec3 pos) throws CommandSyntaxException {
        var rarity = (FallenRarity) Fallen.Registries.RARITY_REGISTRY.rarityRegistry.getRarityMapView().get(rarityId);
        if (rarity == null) {
            ctx.getSource().sendFailure(Component.literal("Unknown rarity: " + rarityId));
            return -1;
        }

        EntityType<?> entityType = net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getValue(entityId);
        if (entityType == null) {
            ctx.getSource().sendFailure(Component.literal("Unknown entity: " + entityId));
            return -2;
        }

        ServerLevel level = ctx.getSource().getLevel();
        Vec3 spawnPos = pos != null ? pos : ctx.getSource().getPosition();

        var entity = entityType.create(level);
        if (!(entity instanceof Mob mob)) {
            ctx.getSource().sendFailure(Component.literal("Entity must be a mob."));
            return -3;
        }

        mob.moveTo(spawnPos.x, spawnPos.y, spawnPos.z, 0, 0);
        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.COMMAND, null, null);

        UniversalBossConfig config = UniversalBossLoader.getConfig();
        if (config != null) {
            var stats = config.stats().get(rarity);
            if (stats != null) {
                int duration = mob instanceof Creeper ? 6000 : Integer.MAX_VALUE;
                for (var inst : stats.effects()) {
                    if (mob.getRandom().nextFloat() <= inst.chance()) mob.addEffect(inst.create(mob.getRandom(), duration));
                }
                float statChance = config.getStatChance(rarity);
                for (var modif : stats.modifiers()) {
                    if (mob.getRandom().nextFloat() < statChance) modif.apply(mob.getRandom(), mob);
                }
                mob.setHealth(mob.getMaxHealth());
            }

            String rarityKey = rarityId.getPath();

            // Player-compatible affixes
            for (var entry : config.getAffixesForRarity(rarity)) {
                if (mob.getRandom().nextFloat() < entry.chance()) {
                    net.kayn.fallen_gems_affixes.adventure.entity.EntityAffixHelper.addAffix(mob, entry.affixId(), rarityKey, entry.level());
                }
            }

            List<net.kayn.fallen_gems_affixes.adventure.entity.EntityAffixInstance> resolved =
                    net.kayn.fallen_gems_affixes.adventure.entity.EntityAffixHelper.getAffixes(mob);
            for (var inst : resolved) {
                if (!inst.isValid()) continue;
                for (net.minecraft.world.entity.EquipmentSlot slot : net.minecraft.world.entity.EquipmentSlot.values()) {
                    inst.affix().get().addModifiers(ItemStack.EMPTY, inst.rarity().get(), inst.level(), slot, (attr, mod) -> {
                        var attrInst = mob.getAttribute(attr);
                        if (attrInst != null && !attrInst.hasModifier(mod)) attrInst.addPermanentModifier(mod);
                    });
                }
            }

            // Mob-only affixes
            for (var entry : config.getMobAffixesForRarity(rarity)) {
                if (mob.getRandom().nextFloat() < entry.chance()) {
                    net.kayn.fallen_gems_affixes.adventure.entity.MobAffixHelper.addAffix(mob, entry.affixId(), entry.level());
                }
            }
        }

        mob.getPersistentData().putBoolean("fga.universal_boss", true);
        mob.getPersistentData().putString("fga.universal_boss.rarity", rarityId.getPath());
        mob.getPersistentData().putBoolean("apoth.boss", true);
        ResourceLocation resId = rarity.getClassifier();
        String rarityIdStr = resId != null ? resId.toString() : rarityId.toString();
        if (rarity.getRarity() instanceof LootRarity r) {
            mob.getPersistentData().putString("apoth.rarity", rarityIdStr);

            mob.setCustomName(mob.getName().copy().withStyle(Style.EMPTY.withColor(r.getColor())));
            mob.setCustomNameVisible(false);

            level.addFreshEntity(mob);

            final Vec3 finalPos = spawnPos;
            ctx.getSource().sendSuccess(() -> Component.literal(
                    "Spawned " + rarityId.getPath() + " " + entityId.getPath() +
                            " at " + String.format("%.1f %.1f %.1f", finalPos.x, finalPos.y, finalPos.z)), true);
            return 1;
        } else {
            ctx.getSource().sendFailure(Component.literal("Unknown rarity: " + rarityId));
            return -1;
        }
    }

    private static int clearAugments(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal("Player has no item in mainhand."));
            return 0;
        } else if (stack.hasTag() && stack.getTag().contains(AUGMENT_DATA)) {
            CompoundTag augmentData = stack.getTagElement(AUGMENT_DATA);
            ListTag augments = augmentData.getList(AUGMENTS, Tag.TAG_COMPOUND);
            augments.clear();
        }

        ctx.getSource().sendSuccess(
                () -> Component.literal("Cleared mainhand augments for " + player.getName().getString()),
                false);
        return 1;
    }

    private static int addAugmentToMainHandItem(CommandContext<CommandSourceStack> ctx, float extraData) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        ResourceLocation augID = ctx.getArgument("Augment", ResourceLocation.class);
        AugmentMeta meta = augID != null ? Fallen.Registries.AUGMENT_REGISTRY.getMetaData(augID) : null;

        if (meta == null) {
            ctx.getSource().sendFailure(Component.literal("Unknown Augment"));
            return 0;
        }

        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal("Player has no item in mainhand."));
            return 0;
        }

        for (IAugment iAugment : AugmentHelper.getAugments(stack).augments()) {
            if (iAugment == meta.getAugment() && iAugment.isUnique()) {
                ctx.getSource().sendFailure(Component.literal("Failed adding Augment, Augment is unique."));
                return 0;
            }
        }
        AugmentSlotHelper.setAugmentSlotsDirect(stack,AugmentSlotHelper.getAugmentSlots(stack) + 1);
        AugmentHelper.applyAugment(stack, new AugmentInstance(meta.getAugment(), meta.newDefaultData()));

        ctx.getSource().sendSuccess(
                () -> Component.literal("Added Augment " + augID + " to " + player.getName().getString()),
                false);
        return 1;
    }
}