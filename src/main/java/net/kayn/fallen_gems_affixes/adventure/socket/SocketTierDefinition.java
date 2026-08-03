package net.kayn.fallen_gems_affixes.adventure.socket;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kayn.fallen_gems_affixes.util.MiscUtil;
import net.minecraft.resources.ResourceLocation;

public final class SocketTierDefinition implements ISocketDefinition {

    public static final Codec<SocketTierDefinition> CODEC = RecordCodecBuilder.create(inst ->
        inst.group(
                ResourceLocation.CODEC.fieldOf("rarity").forGetter(s -> s.rarityId),
                MiscUtil.COLOR_CODEC.optionalFieldOf("color", 0xFFFFFF).forGetter(s -> s.colorPacked),
                Codec.BOOL.optionalFieldOf("rainbow", false).forGetter(s -> s.rainbow),
                Codec.FLOAT.optionalFieldOf("chance", 0f).forGetter(s -> s.chance),
                Codec.BOOL.optionalFieldOf("enabled", false).forGetter(s -> s.enabled)
        ).apply(inst, SocketTierDefinition::new));

    private final ResourceLocation rarityId;
    private final int colorPacked;
    private final boolean rainbow;
    private final float chance;
    private final boolean enabled;

    private int resolvedOrdinal = -1;

    public SocketTierDefinition(ResourceLocation rarityId, int colorPacked, boolean rainbow,
                                float chance, boolean enabled) {
        this.rarityId    = rarityId;
        this.colorPacked = colorPacked;
        this.rainbow     = rainbow;
        this.chance      = chance;
        this.enabled     = enabled;
    }


    public ResourceLocation rarityId()    { return rarityId; }
    public int              colorPacked() { return colorPacked; }
    public boolean          rainbow()     { return rainbow; }
    public float            chance()      { return chance; }
    public boolean          enabled()     { return enabled; }


    public int ordinal() { return resolvedOrdinal; }

    void setResolvedOrdinal(int ordinal) { this.resolvedOrdinal = ordinal; }

    public String getEmptyTranslationKey() {
        return "socket_tier." + rarityId.getNamespace() + "." + rarityId.getPath() + ".empty";
    }

    @Override
    public Codec<? extends ISocketDefinition> getCodec() {
        return CODEC;
    }
}