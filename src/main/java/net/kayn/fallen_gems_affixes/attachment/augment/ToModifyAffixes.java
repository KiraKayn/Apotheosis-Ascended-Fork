package net.kayn.fallen_gems_affixes.attachment.augment;

import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixInstance;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.augment.AffixEither;
import net.kayn.fallen_gems_affixes.augment.IAffixPowerProvider;
import net.kayn.fallen_gems_affixes.augment.SupremacyAugment;
import net.minecraft.util.Mth;
import net.rtxyd.fallen.lib.util.INull;
import net.rtxyd.fallen.lib.util.ins_attr.AFactorInsAttributeSystem;
import net.rtxyd.fallen.lib.util.ins_attr.AInsAttribute;
import net.rtxyd.fallen.lib.util.ins_attr.InsAttributeModifier;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ToModifyAffixes extends AFactorInsAttributeSystem<DynamicHolder<? extends Affix>, AffixInstance, ToModifyAffixes.AffixPowerAttribute, LiveAugments> {

    public static final ToModifyAffixes EMPTY = new ToModifyAffixes(Collections.emptyMap(), LiveAugments.EMPTY);
    private Map<DynamicHolder<? extends Affix>, AffixInstance> output;

    public ToModifyAffixes(Map<DynamicHolder<? extends Affix>, AffixInstance> affixes, LiveAugments augments) {
        super(affixes, augments);
    }

    @Override
    public AffixPowerAttribute parse(DynamicHolder<? extends Affix> key, AffixInstance ins) {
            AffixPowerAttribute attr = new AffixPowerAttribute(ins, new HashMap<>(), ins.level(), ins.level());
            for (AugmentInstance instance : this.getFactor().instances()) {
                if (instance.getData() instanceof IAffixPowerProvider pro) {
                    var modifier = pro.getModifierBy(new AffixEither(key));
                    if (!modifier.isEmpty()) {
                        attr.addModifier(modifier.getName(), modifier);
                    }
                }
            }
            return attr;
    }

    @Override
    public AffixInstance createInsWith(AffixInstance old, float value) {
        return new AffixInstance(old.affix(), old.stack(), old.rarity(), Mth.clamp(value, 0, Fallen.Common.AFFIX_POWER_CAP));
    }

    public Map<DynamicHolder<? extends Affix>, AffixInstance> getOutput() {
        if (this.output == null) {
            this.output = this.output();
        }
        return output;
    }

    public static class AffixPowerAttribute extends AInsAttribute<AffixInstance> {
        public AffixPowerAttribute(AffixInstance affixInstance, Map<String,InsAttributeModifier> modifiers, float initBase, float initFinal) {
            super(affixInstance, modifiers, initBase, initFinal);
        }
    }
}