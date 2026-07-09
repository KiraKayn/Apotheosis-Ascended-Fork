package net.kayn.fallen_gems_affixes.adventure.set;

import com.google.common.base.Predicates;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import dev.shadowsoffire.placebo.reload.DynamicRegistry;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.kayn.fallen_gems_affixes.adventure.set.colossus.*;
import net.kayn.fallen_gems_affixes.adventure.set.deadeye.*;
import net.kayn.fallen_gems_affixes.adventure.set.trickster.*;
import net.kayn.fallen_gems_affixes.attachment.rarity.ClientLikeSyncFallenRarityPacket;
import net.kayn.fallen_gems_affixes.attachment.rarity.FallenRarity;
import net.minecraft.resources.ResourceLocation;
import net.rtxyd.fallen.lib.runtime.forgemod.network.AbstractPacketBoundRegistry;
import net.rtxyd.fallen.lib.runtime.forgemod.network.DefaultPacketBoundRegistry;
import org.apache.logging.log4j.Logger;

import java.util.function.Predicate;

public class SetAffixRegistry extends DefaultPacketBoundRegistry<SetAffix> {

    public SetAffixRegistry() {
        super(org.apache.logging.log4j.LogManager.getLogger("FallenGemsAffixes"), "set_affixes", "type", Predicates.alwaysTrue(), true, true);
    }

    @Override
    protected void registerBuiltinCodecs() {
        this.registerCodec(FallenGemsAffixes.id("trickster_helmet"),     TricksterHelmetAffix.CODEC);
        this.registerCodec(FallenGemsAffixes.id("trickster_chestplate"), TricksterChestplateAffix.CODEC);
        this.registerCodec(FallenGemsAffixes.id("trickster_leggings"),   TricksterLeggingsAffix.CODEC);
        this.registerCodec(FallenGemsAffixes.id("trickster_boots"),      TricksterBootsAffix.CODEC);
        this.registerCodec(FallenGemsAffixes.id("trickster_weapon"),     TricksterWeaponAffix.CODEC);

        this.registerCodec(FallenGemsAffixes.id("colossus_helmet"),      ColossusHelmetAffix.CODEC);
        this.registerCodec(FallenGemsAffixes.id("colossus_chestplate"),  ColossusChestplateAffix.CODEC);
        this.registerCodec(FallenGemsAffixes.id("colossus_leggings"),    ColossusLeggingsAffix.CODEC);
        this.registerCodec(FallenGemsAffixes.id("colossus_boots"),       ColossusBootsAffix.CODEC);
        this.registerCodec(FallenGemsAffixes.id("colossus_shield"),      ColossusShieldAffix.CODEC);

        this.registerCodec(FallenGemsAffixes.id("deadeye_helmet"),     DeadeyeHelmetAffix.CODEC);
        this.registerCodec(FallenGemsAffixes.id("deadeye_chestplate"), DeadeyeChestplateAffix.CODEC);
        this.registerCodec(FallenGemsAffixes.id("deadeye_leggings"),   DeadeyeLeggingsAffix.CODEC);
        this.registerCodec(FallenGemsAffixes.id("deadeye_boots"),      DeadeyeBootsAffix.CODEC);
        this.registerCodec(FallenGemsAffixes.id("deadeye_bow"),     DeadeyeBowAffix.CODEC);
    }
}