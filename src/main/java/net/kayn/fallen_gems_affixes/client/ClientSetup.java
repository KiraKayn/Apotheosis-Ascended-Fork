package net.kayn.fallen_gems_affixes.client;

import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.kayn.fallen_gems_affixes.adventure.socket.gem.storage.GemCaseRegistry;
import net.kayn.fallen_gems_affixes.adventure.socket.gem.storage.GemCaseScreen;
import net.kayn.fallen_gems_affixes.attachment.augment.AugmentModel;
import net.kayn.fallen_gems_affixes.client.render.GemCaseTileRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;

@Mod.EventBusSubscriber(modid = FallenGemsAffixes.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    private static final Logger LOGGER = LogManager.getLogger("ClientSetup");

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            gemCaseSetup();

        });
    }

    public static void gemCaseSetup() {
        MenuScreens.register(GemCaseRegistry.GEM_CASE_MENU.get(), GemCaseScreen::new);

        BlockEntityRenderers.register(GemCaseRegistry.GEM_CASE_TILE.get(), ctx -> new GemCaseTileRenderer());
        BlockEntityRenderers.register(GemCaseRegistry.ENDER_GEM_CASE_TILE.get(), ctx -> new GemCaseTileRenderer());
    }

    @SubscribeEvent
    public static void addAugmentModels(ModelEvent.RegisterAdditional e) {
        Set<ResourceLocation> locs = Minecraft.getInstance().getResourceManager()
                .listResources("models", loc ->
                        FallenGemsAffixes.MOD_ID.equals(loc.getNamespace())
                                && loc.getPath().contains("/augments/")
                                && loc.getPath().endsWith(".json"))
                .keySet();
        for (ResourceLocation s : locs) {
            String path = s.getPath().substring("models/".length(), s.getPath().length() - ".json".length());
            e.register(ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, path));
        }
    }

    @SubscribeEvent
    public static void replaceAugmentModels(ModelEvent.ModifyBakingResult event) {
        ModelResourceLocation key = new ModelResourceLocation(FallenGemsAffixes.id("augment"), "inventory");
        BakedModel oldModel = event.getModels().get(key);
        if (oldModel != null) {
            event.getModels().put(key, new AugmentModel(oldModel));
        }
        LOGGER.debug("Wrapped augment model with AugmentModel");
    }
}