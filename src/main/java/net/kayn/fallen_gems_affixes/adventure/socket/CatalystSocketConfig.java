package net.kayn.fallen_gems_affixes.adventure.socket;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.kayn.fallen_gems_affixes.util.MiscUtil;
import net.minecraft.resources.ResourceLocation;

public final class CatalystSocketConfig implements ISocketDefinition {

    public static final CatalystSocketConfig INSTANCE = new CatalystSocketConfig();
    public static final Codec<CatalystSocketConfig> CODEC = RecordCodecBuilder.create(inst ->
        inst.group(
                Codec.FLOAT.optionalFieldOf("power_per_socket", 0.1f).forGetter(s -> s.powerPerSocket),
                MiscUtil.COLOR_CODEC.optionalFieldOf("colorPacked", 0x55FFFF).forGetter(s -> s.colorPacked),
                Codec.BOOL.optionalFieldOf("rainbow", false).forGetter(s -> s.rainbow)
        ).apply(inst, (power, color, rainbow) -> {
            CatalystSocketConfig config = new CatalystSocketConfig();
            config.powerPerSocket = power;
            config.colorPacked = color;
            config.rainbow = rainbow;
            return config;
        }));

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "catalyst_socket");

    private float powerPerSocket = 0.1f;
    private int colorPacked = 0x55FFFF;
    private boolean rainbow = false;

    private CatalystSocketConfig() {}

    void apply(CatalystSocketConfig config) {
        powerPerSocket = config.powerPerSocket;
        colorPacked    = config.colorPacked;
        rainbow        = config.rainbow;
    }

    public float getPowerPerSocket() { return powerPerSocket; }
    public int   getColorPacked()   { return colorPacked; }
    public boolean isRainbow()      { return rainbow; }

    @Override
    public Codec<? extends ISocketDefinition> getCodec() {
        return CODEC;
    }
}
