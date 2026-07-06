package net.kayn.fallen_gems_affixes.adventure.socket.gem.storage;

import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class GemCaseRegistry {

    private GemCaseRegistry() {
    }


    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, FallenGemsAffixes.MOD_ID);

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, FallenGemsAffixes.MOD_ID);

    public static final DeferredRegister<BlockEntityType<?>> TILES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, FallenGemsAffixes.MOD_ID);

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, FallenGemsAffixes.MOD_ID);

    public static final RegistryObject<GemCaseBlock> GEM_CASE_BLOCK = BLOCKS.register("gem_case",
            () -> new GemCaseBlock(GemCaseTile.BasicGemCaseTile::new,
                    BlockBehaviour.Properties.of()
                            .requiresCorrectToolForDrops()
                            .strength(2.5F, 1200F)
                            .sound(SoundType.GLASS)
                            .noOcclusion()
                            .lightLevel(s -> 2),
                    Short.MAX_VALUE));

    public static final RegistryObject<GemCaseBlock> ENDER_GEM_CASE_BLOCK = BLOCKS.register("ender_gem_case",
            () -> new GemCaseBlock(GemCaseTile.EnderGemCaseTile::new,
                    BlockBehaviour.Properties.of()
                            .requiresCorrectToolForDrops()
                            .strength(2.5F, 1200F)
                            .sound(SoundType.GLASS)
                            .noOcclusion()
                            .lightLevel(s -> 2),
                    Integer.MAX_VALUE));

    public static final RegistryObject<Item> GEM_CASE_ITEM = ITEMS.register("gem_case",
            () -> new BlockItem(GEM_CASE_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<Item> ENDER_GEM_CASE_ITEM = ITEMS.register("ender_gem_case",
            () -> new BlockItem(ENDER_GEM_CASE_BLOCK.get(), new Item.Properties()));

    @SuppressWarnings("ConstantConditions")
    public static final RegistryObject<BlockEntityType<GemCaseTile.BasicGemCaseTile>> GEM_CASE_TILE =
            TILES.register("gem_case",
                    () -> BlockEntityType.Builder
                            .of(GemCaseTile.BasicGemCaseTile::new, GEM_CASE_BLOCK.get())
                            .build(null));

    public static final RegistryObject<BlockEntityType<GemCaseTile.EnderGemCaseTile>> ENDER_GEM_CASE_TILE =
            TILES.register("ender_gem_case",
                    () -> BlockEntityType.Builder
                            .of(GemCaseTile.EnderGemCaseTile::new, ENDER_GEM_CASE_BLOCK.get())
                            .build(null));

    public static final RegistryObject<MenuType<GemCaseMenu>> GEM_CASE_MENU =
            MENUS.register("gem_case",
                    () -> IForgeMenuType.create((id, inv, buf) ->
                            new GemCaseMenu(id, inv, buf.readBlockPos())));

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        TILES.register(bus);
        MENUS.register(bus);
    }
}

