package net.kayn.fallen_gems_affixes.adventure.set.deadeye;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public final class DeadeyeStateHelper {
    private static final String FOCUS_KEY = "fga.deadeye.focus";
    private static final String LAST_ACTION_KEY = "fga.deadeye.last_action";
    private static final String STACKS_KEY = "fga.deadeye.stacks";
    private static final String STACKS_LAST_HIT_KEY = "fga.deadeye.stacks_last_hit";
    private static final String MARKED_UNTIL_KEY = "fga.deadeye.marked_until";
    private static final String MARKED_BY_KEY = "fga.deadeye.marked_by";

    public static int getFocus(Player player) {
        return player.getPersistentData().getInt(FOCUS_KEY);
    }

    public static void setFocus(Player player, int value, int maxFocus) {
        player.getPersistentData().putInt(FOCUS_KEY, Math.max(0, Math.min(value, maxFocus)));
    }

    public static long getLastActionTime(Player player) {
        return player.getPersistentData().getLong(LAST_ACTION_KEY);
    }

    public static void markAction(Player player) {
        player.getPersistentData().putLong(LAST_ACTION_KEY, player.level().getGameTime());
    }

    public static int getStacks(Player player) {
        return player.getPersistentData().getInt(STACKS_KEY);
    }

    public static void setStacks(Player player, int value) {
        player.getPersistentData().putInt(STACKS_KEY, Math.max(0, value));
    }

    public static long getStacksLastHit(Player player) {
        return player.getPersistentData().getLong(STACKS_LAST_HIT_KEY);
    }

    public static void markStackHit(Player player) {
        player.getPersistentData().putLong(STACKS_LAST_HIT_KEY, player.level().getGameTime());
    }

    public static boolean isMarkedBy(LivingEntity target, Player player) {
        CompoundTag data = target.getPersistentData();
        if (!data.contains(MARKED_UNTIL_KEY)) return false;
        long until = data.getLong(MARKED_UNTIL_KEY);
        if (until < target.level().getGameTime()) return false;
        UUID by = data.hasUUID(MARKED_BY_KEY) ? data.getUUID(MARKED_BY_KEY) : null;
        return player.getUUID().equals(by);
    }

    public static void applyMark(LivingEntity target, Player player, int durationTicks) {
        CompoundTag data = target.getPersistentData();
        data.putLong(MARKED_UNTIL_KEY, target.level().getGameTime() + durationTicks);
        data.putUUID(MARKED_BY_KEY, player.getUUID());
    }

    private DeadeyeStateHelper() {}
}