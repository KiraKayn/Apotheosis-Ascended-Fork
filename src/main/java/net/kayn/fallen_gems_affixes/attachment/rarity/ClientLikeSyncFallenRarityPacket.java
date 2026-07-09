package net.kayn.fallen_gems_affixes.attachment.rarity;

import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.minecraft.resources.ResourceLocation;
import net.rtxyd.fallen.lib.runtime.forgemod.FallenLib;
import net.rtxyd.fallen.lib.runtime.forgemod.network.AbstractRegistryBoundPacketPayload;
import net.rtxyd.fallen.lib.runtime.forgemod.network.IVanillaLikeCustomPacketPayload;
import net.rtxyd.fallen.lib.runtime.forgemod.util.FriendlyByteBufCodec;
import org.jetbrains.annotations.NotNull;

public class ClientLikeSyncFallenRarityPacket extends AbstractRegistryBoundPacketPayload<FallenRarity> {
    public static final FriendlyByteBufCodec<ClientLikeSyncFallenRarityPacket> BUF_CODEC =
            createByteBufCodec(FallenGemsAffixes.LOGGER, FallenRarity.CODEC, ClientLikeSyncFallenRarityPacket::new);

    protected static final String version = "1.0";
    protected static final Type<ClientLikeSyncFallenRarityPacket> TYPE =
            IVanillaLikeCustomPacketPayload.createType(FallenLib.MODID, "fr_cl");

    public ClientLikeSyncFallenRarityPacket(ResourceLocation path, FallenRarity registryItem) {
        super(path, registryItem);
    }

    @Override
    public @NotNull IVanillaLikeCustomPacketPayload.Type<? extends IVanillaLikeCustomPacketPayload> type() {
        return TYPE;
    }

    public static class Begin implements IBegin {
        public static final Type<ClientLikeSyncFallenRarityPacket.Begin> TYPE =
                IVanillaLikeCustomPacketPayload.createType(FallenLib.MODID, "fr_cl_begin");

        @Override
        public Class<ClientLikeSyncFallenRarityPacket> getProcessClass() {
            return ClientLikeSyncFallenRarityPacket.class;
        }

        @Override
        public @NotNull Type<ClientLikeSyncFallenRarityPacket.Begin> type() {
            return TYPE;
        }
    }

    public static class End implements AbstractRegistryBoundPacketPayload.IEnd {
        public static final Type<ClientLikeSyncFallenRarityPacket.End> TYPE =
                IVanillaLikeCustomPacketPayload.createType(FallenLib.MODID, "fr_cl_end");

        @Override
        public Class<ClientLikeSyncFallenRarityPacket> getProcessClass() {
            return ClientLikeSyncFallenRarityPacket.class;
        }

        @Override
        public @NotNull Type<ClientLikeSyncFallenRarityPacket.End> type() {
            return TYPE;
        }
    }
}