package net.kayn.fallen_gems_affixes.attachment.augment;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.ListBuilder;
import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.types.augment.IAugment;
import net.kayn.fallen_gems_affixes.types.augment.IAugmentInnerData;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

@SuppressWarnings("rawtypes")
public class LiveAugments implements Iterable<Map.Entry<IAugment, AugmentInstance>> {
    private final Map<IAugment, AugmentInstance> map;
    public static final LiveAugments EMPTY = new LiveAugments(new HashMap<>());
    public static final Codec<LiveAugments> CODEC = new Codec<LiveAugments>() {
        @Override
        public <T> DataResult<Pair<LiveAugments, T>> decode(DynamicOps<T> ops, T input) {
            Map<IAugment, AugmentInstance> result = new HashMap<>();
            DataResult<Consumer<Consumer<T>>> a = ops.getList(input);
            if (a.error().isPresent()) return DataResult.success(Pair.of(LiveAugments.EMPTY, input));
            a.result().get().accept(b -> {
                DataResult<T> id = ops.get(b, "type");
                if (id.error().isPresent()) return;
                DataResult<Pair<Supplier<IAugment>, T>> aug = IAugment.CODEC.decode(ops, id.result().get());
                if (aug.error().isPresent()) return;
                IAugment augment = aug.result().get().getFirst().get();
                DataResult<T> data = ops.get(b, "inner_data");
                if (data.error().isPresent()) return;
                AugmentMeta meta = Fallen.Registries.AUGMENT_REGISTRY.getMetaData(augment);
                IAugmentInnerData defaultData = meta.getDefaultData();
                DataResult<? extends Pair<? extends IAugmentInnerData, T>> inner = defaultData.getCodec().decode(ops, data.result().get());
                if (inner.error().isPresent()) return;
                result.put(augment, new AugmentInstance(augment, inner.result().get().getFirst()));
            });
            return DataResult.success(Pair.of(new LiveAugments(result), input));
        }

        @Override
        public <T> DataResult<T> encode(LiveAugments input, DynamicOps<T> ops, T prefix) {
            ListBuilder<T> listBuilder = ops.listBuilder();

            for (AugmentInstance instance : input.map.values()) {
                IAugment augment = instance.getAugment();
                DataResult<T> type = IAugment.CODEC.encodeStart(ops, () -> augment);
                if (type.error().isPresent()) {
                    continue;
                }

                AugmentMeta meta = Fallen.Registries.AUGMENT_REGISTRY.getMetaData(augment);
                IAugmentInnerData defaultData = meta.getDefaultData();
                IAugmentInnerData innerData = instance.getData();
                DataResult<T> inner = ((Codec<IAugmentInnerData>) defaultData.getCodec()).encodeStart(ops, innerData);
                if (inner.error().isPresent()) {
                    continue;
                }

                DataResult<T> entry = ops.mapBuilder()
                        .add("type", type.result().get())
                        .add("inner_data", inner.result().get())
                        .build(ops.empty());

                if (entry.error().isPresent()) {
                    continue;
                }

                listBuilder.add(entry.result().get());
            }
            return listBuilder.build(prefix);
        }
    };

    public LiveAugments(Map<IAugment, AugmentInstance> map) {
        this.map = Collections.unmodifiableMap(map);
    }

    public Set<IAugment> augments() {
        return map.keySet();
    }

    public Collection<AugmentInstance> instances() {
        return map.values();
    }

    public AugmentInstance get(IAugment aug) {
        return map.get(aug);
    }

    public IAugmentInnerData getData(IAugment aug) {
        AugmentInstance ins = get(aug);
        return ins == null ? null : ins.getData();
    }

    public boolean contains(IAugment aug) {
        return map.containsKey(aug);
    }

    public Set<Map.Entry<IAugment, AugmentInstance>> entries() {
        return map.entrySet();
    }

    public int size() {
        return map.size();
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public @NotNull Iterator<Map.Entry<IAugment, AugmentInstance>> iterator() {
        return map.entrySet().iterator();
    }

    public Map<IAugment, AugmentInstance> toMap() {
        return new HashMap<>(map);
    }
}
