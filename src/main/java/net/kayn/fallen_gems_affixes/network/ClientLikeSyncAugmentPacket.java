package net.kayn.fallen_gems_affixes.network;

import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.kayn.fallen_gems_affixes.attachment.augment.AugmentMeta;
import net.kayn.fallen_gems_affixes.types.augment.IAugmentInnerData;
import net.minecraft.resources.ResourceLocation;
import net.rtxyd.fallen.lib.runtime.forgemod.network.AbstractRegistryBoundPacketPayload;
import net.rtxyd.fallen.lib.runtime.forgemod.network.IVanillaLikeCustomPacketPayload;
import net.rtxyd.fallen.lib.runtime.forgemod.util.FriendlyByteBufCodec;
import org.jetbrains.annotations.NotNull;

public class ClientLikeSyncAugmentPacket extends AbstractRegistryBoundPacketPayload<AugmentMeta> {

    public static final Type<ClientLikeSyncAugmentPacket> TYPE = IVanillaLikeCustomPacketPayload.createType(FallenGemsAffixes.MOD_ID, "aug_cl");
    public static final FriendlyByteBufCodec<ClientLikeSyncAugmentPacket> BUF_CODEC =
            createByteBufCodec(FallenGemsAffixes.LOGGER, Fallen.Registries.AUGMENT_REGISTRY.getCodec(), ClientLikeSyncAugmentPacket::new);

    public ClientLikeSyncAugmentPacket(ResourceLocation path, AugmentMeta data) {
        super(path, data);
    }

    @Override
    public @NotNull Type<ClientLikeSyncAugmentPacket> type() {
        return TYPE;
    }

    public static class Begin implements IBegin {
        public static final Type<ClientLikeSyncAugmentPacket.Begin> TYPE = IVanillaLikeCustomPacketPayload.createType(FallenGemsAffixes.MOD_ID, "aug_cl_begin");

        @Override
        public Class<ClientLikeSyncAugmentPacket> getProcessClass() {
            return ClientLikeSyncAugmentPacket.class;
        }

        @Override
        public @NotNull Type<ClientLikeSyncAugmentPacket.Begin> type() {
            return TYPE;
        }
    }

    public static class End implements IEnd {
        public static final Type<ClientLikeSyncAugmentPacket.End> TYPE = IVanillaLikeCustomPacketPayload.createType(FallenGemsAffixes.MOD_ID, "aug_cl_end");

        @Override
        public Class<ClientLikeSyncAugmentPacket> getProcessClass() {
            return ClientLikeSyncAugmentPacket.class;
        }

        @Override
        public @NotNull Type<ClientLikeSyncAugmentPacket.End> type() {
            return TYPE;
        }
    }
}
