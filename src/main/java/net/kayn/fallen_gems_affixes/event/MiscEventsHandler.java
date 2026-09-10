package net.kayn.fallen_gems_affixes.event;

import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.socket.SocketHelper;
import dev.shadowsoffire.apotheosis.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.socket.gem.GemInstance;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.adventure.socket.gem.bonus.PermanentEffectBonus;
import net.kayn.fallen_gems_affixes.attachment.permanent_effect_v2.PermanentEffectCapability;
import net.kayn.fallen_gems_affixes.network.ClientlikeClearPermanentEffectPacket;
import net.kayn.fallen_gems_affixes.util.EquipmentSlotUtil;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.rtxyd.fallen.lib.runtime.forgemod.addon.minecraft.mob_effect.EffectsTickEvent;
import top.theillusivec4.curios.api.event.CurioChangeEvent;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

@EventBusSubscriber
public class MiscEventsHandler {
    @SubscribeEvent
    public static void onEntityEquipmentChange(LivingEquipmentChangeEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            EquipmentSlot slot = event.getSlot();
            ItemStack from = event.getFrom();
            onEquip(player, from, slot, true);
            ItemStack to = event.getTo();
            onEquip(player, to, slot, false);
        }
    }

    @SubscribeEvent
    public static void onCurioEquipmentChange(CurioChangeEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        String slot = event.getIdentifier();
        ItemStack from = event.getFrom();
        onCurioEquip(player, from, slot, true);
        ItemStack to = event.getTo();
        onCurioEquip(player, to, slot, false);
    }

    // EffectsTickEvent.Pre handler is registered reflectively in FallenGemsAffixes to avoid a hard runtime
    // dependency on net.rtxyd.fallen.lib. If that library is present at runtime, FallenGemsAffixes will
    // register a listener that mirrors the behavior previously implemented here.

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player entity = event.getEntity();
        if (entity instanceof ServerPlayer player) {
            PermanentEffectCapability cap = player.getCapability(Fallen.Capabilities.PE_CAP);
            if (cap != null) {
                for (Iterator<Map.Entry<Holder<MobEffect>, List<Integer>>> it = cap.getContainer().getIterator(); it.hasNext(); ) {
                    Map.Entry<Holder<MobEffect>, List<Integer>> entry = it.next();
                    if (player.hasEffect(entry.getKey())) {
                        cap.getEffectHandler().removeEffectNoSync(entry.getKey());
                        it.remove();
                    }
                }
                player.connection.send(new ClientlikeClearPermanentEffectPacket());
            }
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    public static void onEffectRemove(MobEffectEvent.Remove event) {
        if (event.getEntity() instanceof ServerPlayer) {
            var a = event.getEntity().getCapability(Fallen.Capabilities.PE_CAP);
            if (a != null) {
                if (a.containsEffect(event.getEffect())) {
                    event.setCanceled(true);
                }
            }
        }
    }

    private static void onCurioEquip(LivingEntity entity, ItemStack itemStack, String slot, boolean remove) {
        if (EquipmentSlotUtil.curioSlotMatches(itemStack, entity, slot)) {
            checkGemBonus(itemStack, (bonus, rarity) -> {
                onEquipDefault(entity, bonus.getEffect(), bonus.getAmplifier(rarity), remove);
            });
        }
    }

    public static void checkGemBonus(ItemStack itemStack, BiConsumer<PermanentEffectBonus, Purity> processor) {
        if (itemStack.isEmpty()) return;
        LootCategory cat = LootCategory.forItem(itemStack);
        for (GemInstance g : SocketHelper.getGems(itemStack)) {
            if (!g.isValid()) continue;
            Purity purity = g.purity();
            Gem gem = g.gem().get();
            gem.getBonus(cat, purity)
                    .filter(b -> b instanceof PermanentEffectBonus)
                    .map(b -> (PermanentEffectBonus) b)
                    .ifPresent(bonus -> processor.accept(bonus, purity));
        }
    }

    private static void onEquip(LivingEntity entity, ItemStack itemStack, EquipmentSlot slot, boolean remove) {
        if (EquipmentSlotUtil.vanillaSlotMatches(itemStack, slot)) {
            checkGemBonus(itemStack, (bonus, rarity) -> {
                onEquipDefault(entity, bonus.getEffect(), bonus.getAmplifier(rarity), remove);
            });
        }
    }

    private static void onEquipDefault(LivingEntity entity, Holder<MobEffect> effect, int amplifier, boolean remove) {
        var cap = entity.getCapability(Fallen.Capabilities.PE_CAP);
        if (cap != null) {
            if (remove) {
                cap.removeEffect(effect, amplifier);
            }
            else {
                cap.addEffect(effect, amplifier);
            }
        }
    }
}
