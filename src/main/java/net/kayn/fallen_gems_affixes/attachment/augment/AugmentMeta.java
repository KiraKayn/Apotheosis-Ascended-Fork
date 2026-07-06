package net.kayn.fallen_gems_affixes.attachment.augment;

import com.mojang.datafixers.Products;
import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import net.kayn.fallen_gems_affixes.types.augment.IAugment;
import net.kayn.fallen_gems_affixes.types.augment.IAugmentInnerData;
import net.kayn.fallen_gems_affixes.util.ConditionalLootCategory;
import net.minecraft.resources.ResourceLocation;
import net.rtxyd.fallen.lib.runtime.forgemod.util.ICodecProvider;

import java.util.Collections;
import java.util.Set;
import java.util.function.Supplier;

@SuppressWarnings("unchecked")
public class AugmentMeta implements ICodecProvider<AugmentMeta> {
    private AugmentGeneral general;
    private Supplier<IAugment> augmentHolder;
    private IAugmentInnerData defaultData;

    public static <T extends IAugmentInnerData> Codec<AugmentMeta> codecCreate(Codec<T> innerDataCodec) {
        return RecordCodecBuilder.create(ins ->
            ins.group(
                    AugmentGeneral.generalCodec(),
                    IAugment.CODEC.fieldOf("augment").forGetter(t -> t.augmentHolder),
                    innerDataCodec.fieldOf("inner_data").forGetter(t -> (T) t.defaultData)
            ).apply(ins, (general, augment, defaultData) -> {
                AugmentMeta meta = new AugmentMeta();
                meta.general = general;
                meta.augmentHolder = augment;
                meta.defaultData = defaultData;
                return meta;
            }));
    }

    public IAugment getAugment() {
        return augmentHolder.get();
    }

    // we shouldn't change the default data, it should be frozon after registered
    IAugmentInnerData getDefaultData() {
        return defaultData;
    }

    public IAugmentInnerData newDefaultData() {
        return defaultData.copy();
    }

    @Override
    public Codec<AugmentMeta> getCodec() {
        return augmentHolder.get().getMetaDataCodec();
    }

    public boolean canApplyTo(LootCategory cat) {
        return general.categories.isEmpty() || general.categories.contains(cat);
    }

    public Set<LootCategory> getCategories() {
        return general.categories;
    }

    public static class AugmentGeneral {
        private static final Products.P3<RecordCodecBuilder.Mu<AugmentGeneral>, ResourceLocation, Set<LootCategory>, Set<ConditionalLootCategory>> P3 = new RecordCodecBuilder.Instance<AugmentGeneral>().group(
                ResourceLocation.CODEC.fieldOf("texture").forGetter(t -> t.texture),
                LootCategory.SET_CODEC.fieldOf("categories").forGetter(t -> t.categories),
                ConditionalLootCategory.SET_CODEC.optionalFieldOf("con_cat", Set.of()).forGetter(t -> t.conCat)
        );
        private static final Codec<AugmentGeneral> INTERNAL_CODEC = RecordCodecBuilder.create(inst ->
                P3.apply(inst, (texture, categories, conCategories) -> {
                    AugmentGeneral general = new AugmentGeneral();
                    general.texture = texture;

                    Set<LootCategory> mutableCategories = new java.util.HashSet<>(categories);
                    ConditionalLootCategory.addAll(conCategories, mutableCategories);

                    general.conCat = conCategories;
                    general.categories = Collections.unmodifiableSet(mutableCategories);
                    return general;
                }));

        private ResourceLocation texture;
        private Set<LootCategory> categories;
        private Set<ConditionalLootCategory> conCat;

        public static App<RecordCodecBuilder.Mu<AugmentMeta>, AugmentGeneral> generalCodec() {
            return INTERNAL_CODEC.fieldOf("general").forGetter(t -> t.general);
        }
    }
}
