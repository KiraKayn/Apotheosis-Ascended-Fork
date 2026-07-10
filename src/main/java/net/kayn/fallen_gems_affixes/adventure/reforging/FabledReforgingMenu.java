package net.kayn.fallen_gems_affixes.adventure.reforging;

import com.mojang.logging.LogUtils;
import net.kayn.fallen_gems_affixes.Fallen;
import org.slf4j.Logger;

import dev.shadowsoffire.apotheosis.adventure.Adventure.Items;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import dev.shadowsoffire.placebo.cap.InternalItemHandler;
import dev.shadowsoffire.placebo.menu.BlockEntityMenu;
import net.kayn.fallen_gems_affixes.adventure.set.SetAffix;
import net.kayn.fallen_gems_affixes.adventure.set.SetAffixHelper;
import net.kayn.fallen_gems_affixes.adventure.set.SetAffixRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class FabledReforgingMenu extends BlockEntityMenu<FabledReforgingTableTile> {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final String REFORGE_SEED = "fga_fabled_reforge_seed";

    protected final Player player;
    protected InternalItemHandler itemInv = new InternalItemHandler(1);
    protected InternalItemHandler choicesInv = new InternalItemHandler(3);
    protected final int[] costs = new int[3];
    protected int seed = -1;

    private final List<SetAffix> currentChoices = new ArrayList<>();

    public FabledReforgingMenu(int id, Inventory inv, BlockPos pos) {
        super(FabledReforging.MENU_TYPE.get(), id, inv, pos);
        this.player = inv.player;

        this.addSlot(new UpdatingSlot(this.itemInv, 0, 81, 62, stack -> !LootCategory.forItem(stack).isNone()) {
            @Override public int getMaxStackSize() { return 1; }
            @Override public int getMaxStackSize(ItemStack s) { return 1; }
        });
        this.addSlot(new UpdatingSlot(this.tile.inv, 0, 39, 40, this.tile::isValidRarityMat));
        this.addSlot(new UpdatingSlot(this.tile.inv, 1, 123, 86, stack -> stack.getItem() == Items.SIGIL_OF_REBIRTH.get()));
        this.addSlot(new FabledResultSlot(this.choicesInv, 0, 27, 135));
        this.addSlot(new FabledResultSlot(this.choicesInv, 1, 81, 135));
        this.addSlot(new FabledResultSlot(this.choicesInv, 2, 135, 135));
        this.addPlayerSlots(inv, 8, 184);

        this.mover.registerRule((stack, slot) -> slot >= this.playerInvStart && !LootCategory.forItem(stack).isNone(), 0, 1);
        this.mover.registerRule((stack, slot) -> slot >= this.playerInvStart && this.tile.isValidRarityMat(stack), 1, 2);
        this.mover.registerRule((stack, slot) -> slot >= this.playerInvStart && stack.getItem() == Items.SIGIL_OF_REBIRTH.get(), 2, 3);
        this.mover.registerRule((stack, slot) -> slot < this.playerInvStart, this.playerInvStart, this.hotbarStart + 9);
        this.registerInvShuffleRules();

        this.updateSeed();
        this.addDataSlot(DataSlot.shared(this.costs, 0));
        this.addDataSlot(DataSlot.shared(this.costs, 1));
        this.addDataSlot(DataSlot.shared(this.costs, 2));
    }

    @Override
    public void removed(Player pPlayer) {
        super.removed(pPlayer);
        this.clearContainer(pPlayer, new RecipeWrapper(this.itemInv));
    }

    protected void updateSeed() {
        int seed = this.player.getPersistentData().getInt(REFORGE_SEED);
        if (seed == 0) {
            seed = this.player.getRandom().nextInt();
            this.player.getPersistentData().putInt(REFORGE_SEED, seed);
        }
        this.seed = seed;
    }

    public int getMatCount() { return this.getSlot(1).getItem().getCount(); }
    public int getSigilCount() { return this.getSlot(2).getItem().getCount(); }

    public int getSigilCost(int slot) { return this.costs[0]; }
    public int getMatCost(int slot) { return this.costs[1]; }
    public int getLevelCost(int slot) { return this.costs[2]; }

    @Nullable
    public LootRarity getFabledRarity() {
        return RarityRegistry.INSTANCE.getValue(Fallen.Common.FABLED_ID);
    }

    private List<SetAffix> computeChoices(ItemStack input) {
        LootCategory cat = LootCategory.forItem(input);
        LootRarity fabledRarity = getFabledRarity();
        if (fabledRarity == null) return List.of();
        LOGGER.info("=== REFORGE SCANNING SYSTEM REGISTRIES ===");
        Fallen.Registries.SET_AFFIX_REGISTRY.getKeys().forEach(key -> LOGGER.info("Discovered SetAffix ID: {}", key));

        List<SetAffix> applicable = Fallen.Registries.SET_AFFIX_REGISTRY.getValues().stream()
                .filter(sa -> sa.canApplyTo(input, cat, fabledRarity))
                .collect(Collectors.toList());

        LOGGER.info("Total matching options discovered for item: {}", applicable.size());

        if (applicable.isEmpty()) return List.of();
        Random rand = new Random((long) this.seed ^ ForgeRegistries.ITEMS.getKey(input.getItem()).hashCode());
        Collections.shuffle(applicable, rand);
        return applicable.subList(0, Math.min(3, applicable.size()));
    }

    private ItemStack buildChoiceStack(ItemStack input, SetAffix setAffix) {
        ItemStack output = input.copy();
        LootRarity fabled = getFabledRarity();

        if (fabled != null) {
            output.getOrCreateTagElement(dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper.AFFIX_DATA);
            AffixHelper.setRarity(output, fabled);
        } else {
            return ItemStack.EMPTY;
        }

        SetAffixHelper.applySetAffix(output, setAffix);
        return output;
    }

    @Override
    public void slotsChanged(Container pContainer) {
        FabledReforgingRecipe recipe = this.tile.getRecipe();
        if (recipe != null) {
            this.costs[0] = recipe.sigilCost();
            this.costs[1] = recipe.matCost();
            this.costs[2] = recipe.levelCost();
        } else {
            this.costs[0] = 1;
            this.costs[1] = 1;
            this.costs[2] = 5;
        }

        ItemStack input = this.getSlot(0).getItem();
        boolean hasMat = !this.getSlot(1).getItem().isEmpty();

        this.currentChoices.clear();

        if (!input.isEmpty() && hasMat) {
            this.currentChoices.addAll(computeChoices(input));
        }

        for (int slot = 0; slot < 3; slot++) {
            if (slot < this.currentChoices.size()) {
                this.choicesInv.setStackInSlot(slot, buildChoiceStack(input, this.currentChoices.get(slot)));
            } else {
                this.choicesInv.setStackInSlot(slot, ItemStack.EMPTY);
            }
        }

        super.slotsChanged(pContainer);
        this.tile.setChanged();
    }

    public class FabledResultSlot extends SlotItemHandler {
        public FabledResultSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }

        @Override
        public boolean mayPlace(@NotNull ItemStack stack) { return false; }

        @Override
        public boolean mayPickup(Player playerIn) {
            ItemStack input = FabledReforgingMenu.this.getSlot(0).getItem();
            if (input.isEmpty()) return false;
            if (FabledReforgingMenu.this.getSlot(1).getItem().isEmpty()) return false;
            if (playerIn.isCreative()) return super.mayPickup(playerIn);

            int idx = this.getSlotIndex();
            int sigils = FabledReforgingMenu.this.getSigilCount();
            int sigilCost = FabledReforgingMenu.this.getSigilCost(idx);
            int mats = FabledReforgingMenu.this.getMatCount();
            int matCost = FabledReforgingMenu.this.getMatCost(idx);
            int levels = FabledReforgingMenu.this.player.experienceLevel;
            int levelCost = FabledReforgingMenu.this.getLevelCost(idx);

            return sigils >= sigilCost && mats >= matCost && levels >= levelCost && super.mayPickup(playerIn);
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            if (!player.level().isClientSide) {
                FabledReforgingMenu.this.getSlot(0).set(ItemStack.EMPTY);
                if (!player.isCreative()) {
                    int idx = this.getSlotIndex();
                    int sigilCost = FabledReforgingMenu.this.getSigilCost(idx);
                    int matCost = FabledReforgingMenu.this.getMatCost(idx);
                    int levelCost = FabledReforgingMenu.this.getLevelCost(idx);

                    FabledReforgingMenu.this.getSlot(1).getItem().shrink(matCost);
                    FabledReforgingMenu.this.getSlot(2).getItem().shrink(sigilCost);

                    player.giveExperienceLevels(-levelCost);
                }
                player.getPersistentData().putInt(REFORGE_SEED, player.getRandom().nextInt());
                FabledReforgingMenu.this.updateSeed();
            }
            player.playSound(SoundEvents.EVOKER_CAST_SPELL, 0.99F, player.level().random.nextFloat() * 0.25F + 1.0F);
            player.playSound(SoundEvents.AMETHYST_CLUSTER_STEP, 0.34F, player.level().random.nextFloat() * 0.2F + 0.8F);
            player.playSound(SoundEvents.SMITHING_TABLE_USE, 0.45F, player.level().random.nextFloat() * 0.5F + 0.75F);
        }
    }
}