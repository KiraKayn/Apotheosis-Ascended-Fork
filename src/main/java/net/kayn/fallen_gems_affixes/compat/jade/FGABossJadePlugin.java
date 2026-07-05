package net.kayn.fallen_gems_affixes.compat.jade;

import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.kayn.fallen_gems_affixes.adventure.entity.EntityAffixHelper;
import net.kayn.fallen_gems_affixes.adventure.entity.MobAffixHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;

@WailaPlugin
public class FGABossJadePlugin implements IWailaPlugin, IEntityComponentProvider, IServerDataProvider<EntityAccessor> {

    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "entity_affixes");

    @Override
    public void register(IWailaCommonRegistration reg) {
        reg.registerEntityDataProvider(this, LivingEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration reg) {
        reg.registerEntityComponent(this, Entity.class);
    }

    @Override
    public void appendServerData(CompoundTag tag, EntityAccessor access) {
        if (!(access.getEntity() instanceof LivingEntity living)) return;

        ListTag out = new ListTag();
        CompoundTag data = living.getPersistentData();

        if (data.contains(EntityAffixHelper.TAG, Tag.TAG_LIST)) {
            for (Tag t : data.getList(EntityAffixHelper.TAG, Tag.TAG_COMPOUND)) {
                if (t instanceof CompoundTag entry) {
                    String id = entry.getString("affix");
                    if (!id.isEmpty()) out.add(StringTag.valueOf(id));
                }
            }
        }

        if (data.contains(MobAffixHelper.TAG, Tag.TAG_LIST)) {
            for (Tag t : data.getList(MobAffixHelper.TAG, Tag.TAG_COMPOUND)) {
                if (t instanceof CompoundTag entry) {
                    String id = entry.getString("affix");
                    if (!id.isEmpty()) out.add(StringTag.valueOf(id));
                }
            }
        }

        if (!out.isEmpty()) tag.put("fga.entity_affixes", out);
    }

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        CompoundTag serverData = accessor.getServerData();
        if (!serverData.contains("fga.entity_affixes", Tag.TAG_LIST)) return;

        ListTag list = serverData.getList("fga.entity_affixes", Tag.TAG_STRING);
        if (list.isEmpty()) return;

        tooltip.add(Component.translatable("jade.fga.entity_affixes").withStyle(ChatFormatting.GRAY));

        MutableComponent line = Component.empty();
        int countInLine = 0;

        for (int i = 0; i < list.size(); i++) {
            String affixKey = list.get(i).getAsString();
            MutableComponent affixComp = Component.translatable(affixKey).withStyle(ChatFormatting.GOLD);

            line.append(affixComp);
            countInLine++;

            boolean isLastInTotal = (i == list.size() - 1);

            if (!isLastInTotal) {
                if (countInLine < 3) {
                    line.append(Component.literal(", ").withStyle(ChatFormatting.GOLD));
                }
            }

            if (countInLine == 3 || isLastInTotal) {
                tooltip.add(line);
                line = Component.empty();
                countInLine = 0;
            }
        }
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}