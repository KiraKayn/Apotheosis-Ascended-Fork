package net.kayn.fallen_gems_affixes.adventure.socket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStreamReader;
import java.util.*;

public final class SocketTierManager extends SimplePreparableReloadListener<Map<ResourceLocation, JsonObject>> {

    public static final SocketTierManager INSTANCE = new SocketTierManager();
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().create();
    private static final String FOLDER = "socket_tiers";

    private List<SocketTierDefinition> definitions = new ArrayList<>();
    private boolean needsResolution = true;

    private SocketTierManager() {}

    @Override
    protected Map<ResourceLocation, JsonObject> prepare(ResourceManager manager, ProfilerFiller profiler) {
        Map<ResourceLocation, JsonObject> raw = new LinkedHashMap<>();
        manager.listResources(FOLDER, id -> id.getPath().endsWith(".json")).forEach((fileId, resource) -> {
            try (InputStreamReader reader = new InputStreamReader(resource.open())) {
                raw.put(fileId, GSON.fromJson(reader, JsonObject.class));
            } catch (Exception e) {
                LOGGER.error("[FGA] Failed to read socket tier file '{}'", fileId);
            }
        });
        return raw;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonObject> data, ResourceManager manager, ProfilerFiller profiler) {
        List<SocketTierDefinition> newDefs = new ArrayList<>();
        data.forEach((id, json) -> {
            SocketTierDefinition def = SocketTierDefinition.parse(json);
            if (def != null) newDefs.add(def);
        });

        this.definitions = newDefs;
        this.needsResolution = true;
        LOGGER.info("[FGA] Prepared {} socket tier definitions.", definitions.size());
    }

    private void ensureResolved() {
        if (!needsResolution) return;

        Iterator<SocketTierDefinition> it = definitions.iterator();
        while (it.hasNext()) {
            SocketTierDefinition def = it.next();
            DynamicHolder<LootRarity> holder = RarityRegistry.INSTANCE.holder(def.rarityId());

            if (holder.isBound()) {
                def.setResolvedOrdinal(holder.get().ordinal());
            } else {
                LOGGER.warn("[FGA] Removing tier: Rarity '{}'", def.rarityId());
                it.remove();
            }
        }

        definitions.sort(Comparator.comparingInt(SocketTierDefinition::ordinal).reversed());
        needsResolution = false;
        LOGGER.info("[FGA] Successfully resolved and sorted {} socket tiers.", definitions.size());
    }

    public int rollSocketTier(RandomSource rand) {
        ensureResolved();

        for (SocketTierDefinition def : definitions) {
            if (rand.nextFloat() < def.chance()) {
                return def.enabled() ? def.ordinal() : TieredSocketHelper.REGULAR_SOCKET;
            }
        }

        if (!definitions.isEmpty()) {
            SocketTierDefinition last = definitions.get(definitions.size() - 1);
            return last.enabled() ? last.ordinal() : TieredSocketHelper.REGULAR_SOCKET;
        }

        return TieredSocketHelper.REGULAR_SOCKET;
    }

    public int getMaxOrdinal() {
        ensureResolved();
        if (definitions.isEmpty()) return -1;
        return definitions.get(0).ordinal();
    }

    public SocketTierDefinition getByOrdinal(int ordinal) {
        ensureResolved();
        for (SocketTierDefinition def : definitions) {
            if (def.ordinal() == ordinal) return def;
        }
        return null;
    }

    public boolean isEnabled() {
        return !definitions.isEmpty();
    }
}