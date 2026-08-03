package net.kayn.fallen_gems_affixes.adventure.socket;

import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemInstance;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemRegistry;
import net.kayn.fallen_gems_affixes.config.ModConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

public final class GemSocketInjectionHelper {

    private GemSocketInjectionHelper() {}

    private static final int MAX_ROLL_ATTEMPTS = 5;

    public static GemInstance rollGemForSocket(
            ItemStack stack, LootCategory category,
            int socketIndex, RandomSource rand, ServerLevel level) {

        if (rand.nextFloat() > ModConfig.SOCKET_GEM_CHANCE.get()) return GemInstance.EMPTY;

        int socketTier = TieredSocketHelper.getSocketTier(stack, socketIndex);
        boolean anyGemOk = !SocketTierManager.INSTANCE.isEnabled()
                || socketTier == TieredSocketHelper.REGULAR_SOCKET;

        TieredSocketMode mode = ModConfig.TIERED_SOCKET_MODE.get();

        int attempts = anyGemOk ? 1 : MAX_ROLL_ATTEMPTS;

        for (int attempt = 0; attempt < attempts; attempt++) {
            ItemStack gemStack = GemRegistry.createRandomGemStack(rand, level, 1.0F,
                    g -> g.getBonuses().stream()
                            .anyMatch(bonus -> bonus.getGemClass() != null
                                    && bonus.getGemClass().types().contains(category)));

            if (gemStack.isEmpty()) break;

            GemInstance instance = GemInstance.socketed(stack, gemStack);
            if (!instance.isValid()) continue;

            if (anyGemOk || isCompatible(instance, socketTier, mode)) {
                return instance;
            }
        }

        return GemInstance.EMPTY;
    }

    private static boolean isCompatible(GemInstance gem, int socketTier, TieredSocketMode mode) {
        int gemOrdinal = TieredSocketHelper.getGemRarityOrdinal(gem);
        if (gemOrdinal < 0) return false;
        return switch (mode) {
            case HARDCORE -> gemOrdinal == socketTier;
            case ON       -> gemOrdinal <= socketTier;
            default -> true;
        };
    }
}