package net.kayn.fallen_gems_affixes.adventure.reforging;

import com.google.common.collect.ImmutableSet;
import dev.shadowsoffire.placebo.block_entity.TickingBlockEntityType;
import dev.shadowsoffire.placebo.menu.MenuUtil;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.kayn.fallen_gems_affixes.client.render.FabledReforgingTableTileRenderer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class FabledReforging {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, FallenGemsAffixes.MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, FallenGemsAffixes.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, FallenGemsAffixes.MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, FallenGemsAffixes.MOD_ID);
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, FallenGemsAffixes.MOD_ID);
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, FallenGemsAffixes.MOD_ID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, FallenGemsAffixes.MOD_ID);

    public static final RegistryObject<FabledReforgingTableBlock> BLOCK =
            BLOCKS.register("fabled_reforging_table", () ->
                    new FabledReforgingTableBlock(
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.COLOR_PURPLE)
                                    .strength(2.5F, 1200F)
                                    .sound(SoundType.METAL)
                                    .requiresCorrectToolForDrops(),
                            5)
            );

    public static final RegistryObject<Item> FABLED_REFORGING_TABLE =
            ITEMS.register("fabled_reforging_table", () ->
                    new BlockItem(BLOCK.get(), new Item.Properties())
            );

    public static final RegistryObject<BlockEntityType<FabledReforgingTableTile>> TILE_TYPE =
            TILES.register("fabled_reforging_table", () ->
                    new TickingBlockEntityType<>(FabledReforgingTableTile::new, ImmutableSet.of(BLOCK.get()), true, false)
            );

    public static final RegistryObject<MenuType<FabledReforgingMenu>> MENU_TYPE =
            MENUS.register("fabled_reforging", () -> MenuUtil.posType(FabledReforgingMenu::new));

    public static final RegistryObject<SoundEvent> REFORGE_SOUND =
            SOUNDS.register("fabled_reforge", () ->
                    SoundEvent.createVariableRangeEvent(FallenGemsAffixes.id("fabled_reforge"))
            );

    public static final RegistryObject<RecipeType<FabledReforgingRecipe>> FABLED_REFORGING_TYPE =
            RECIPE_TYPES.register("fabled_reforging", () ->
                    new RecipeType<FabledReforgingRecipe>() {
                        @Override
                        public String toString() { return FallenGemsAffixes.MOD_ID + ":fabled_reforging"; }
                    });

    public static final RegistryObject<RecipeSerializer<FabledReforgingRecipe>> FABLED_REFORGING_SERIALIZER =
            RECIPE_SERIALIZERS.register("fabled_reforging", () -> FabledReforgingRecipe.Serializer.INSTANCE);

    public static void bootstrap(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        TILES.register(modBus);
        MENUS.register(modBus);
        SOUNDS.register(modBus);
        RECIPE_TYPES.register(modBus);
        RECIPE_SERIALIZERS.register(modBus);
    }
}