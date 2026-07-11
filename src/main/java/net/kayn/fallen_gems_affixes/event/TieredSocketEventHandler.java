package net.kayn.fallen_gems_affixes.event;

import dev.shadowsoffire.apotheosis.adventure.event.ItemSocketingEvent;
import dev.shadowsoffire.apotheosis.adventure.socket.SocketHelper;
import dev.shadowsoffire.apotheosis.adventure.socket.SocketedGems;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemInstance;
import net.kayn.fallen_gems_affixes.adventure.socket.TieredSocketHelper;
import net.kayn.fallen_gems_affixes.adventure.socket.TieredSocketMode;
import net.kayn.fallen_gems_affixes.config.ModConfig;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = "fallen_gems_affixes")
public class TieredSocketEventHandler {

    @SubscribeEvent
    public static void onCanSocket(ItemSocketingEvent.CanSocket event) {
        ItemStack input = event.getInputStack();
        ItemStack gemStack = event.getInputGem();

        GemInstance gem = GemInstance.unsocketed(gemStack);
        if (!gem.isValidUnsocketed()) return;

        TieredSocketMode mode = ModConfig.TIERED_SOCKET_MODE.get();
        if (!TieredSocketHelper.hasCompatibleEmptySocket(input, gem, mode)) {
            event.setResult(Result.DENY);
        }
    }

    @SubscribeEvent
    public static void onModifyResult(ItemSocketingEvent.ModifyResult event) {
        ItemStack gemStack = event.getInputGem();
        ItemStack output = event.getOutput();

        GemInstance gem = GemInstance.unsocketed(gemStack);
        if (!gem.isValidUnsocketed()) return;

        TieredSocketMode mode = ModConfig.TIERED_SOCKET_MODE.get();

        List<GemInstance> gems = new ArrayList<>(SocketHelper.getGems(output).gems());
        int vanillaSlot = -1;
        for (int i = 0; i < gems.size(); i++) {
            if (gems.get(i).isValid() && ItemStack.matches(gems.get(i).gemStack(), gemStack)) {
                vanillaSlot = i;
                break;
            }
        }
        if (vanillaSlot < 0) return;

        int correctSlot = TieredSocketHelper.getFirstCompatibleEmptySocketExcluding(output, gem, mode, vanillaSlot);
        if (correctSlot < 0 || correctSlot == vanillaSlot) return;

        ItemStack insertedGem = gems.get(vanillaSlot).gemStack();
        gems.set(vanillaSlot, GemInstance.EMPTY);
        gems.set(correctSlot, GemInstance.socketed(output, insertedGem));
        SocketHelper.setGems(output, new SocketedGems(gems));
        event.setOutput(output);
    }
}