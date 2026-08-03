package net.kayn.fallen_gems_affixes.adventure.socket;

import com.google.common.base.Predicates;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.kayn.fallen_gems_affixes.config.ModConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.rtxyd.fallen.lib.runtime.forgemod.network.DefaultPacketBoundRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public final class SocketTierManager extends DefaultPacketBoundRegistry<ISocketDefinition> {

    public static final ResourceLocation ID;
    private static final Logger LOGGER;
    private static final String FOLDER;
    public static final SocketTierManager INSTANCE;

    private List<SocketTierDefinition> definitions = new ArrayList<>();
    private boolean needsResolution = true;

    static {
        // init them first.
         ID = ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "tiered_socket");
         LOGGER = LogManager.getLogger();
         FOLDER = "socket_tiers";
         INSTANCE = new SocketTierManager();
    }

    private SocketTierManager() {
        super(LOGGER, FOLDER, "type", Predicates.alwaysTrue(), true, false);
    }

    @Override
    public void beginReload() {
        super.beginReload();
        definitions = new ArrayList<>();
    }

    @Override
    public void onReload() {
        super.onReload();
        CatalystSocketConfig config = null;
        for (ISocketDefinition value : this.registry.values()) {
            if (value instanceof CatalystSocketConfig c) {
                config = c;
            } else if (value instanceof SocketTierDefinition tierDefinition){
                definitions.add(tierDefinition);
            }
        }
        if (config != null) {
            CatalystSocketConfig.INSTANCE.apply(config);
        }
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
        return !definitions.isEmpty() && ModConfig.TIERED_SOCKET_MODE.get() != TieredSocketMode.OFF;
    }

    @Override
    protected void registerBuiltinCodecs() {
        this.registerCodec(ID, SocketTierDefinition.CODEC);
        this.registerCodec(CatalystSocketConfig.ID, CatalystSocketConfig.CODEC);
    }
}