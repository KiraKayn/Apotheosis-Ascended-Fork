package net.kayn.fallen_gems_affixes.mixin.client;

import dev.shadowsoffire.apotheosis.adventure.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.adventure.affix.augmenting.AugmentingMenu;
import dev.shadowsoffire.apotheosis.adventure.affix.augmenting.AugmentingScreen;
import dev.shadowsoffire.apotheosis.adventure.client.SimpleTexButton;
import net.kayn.fallen_gems_affixes.attachment.augment.SpecialAffixEventHandler;
import net.kayn.fallen_gems_affixes.recipe.ErasureRecipe;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = AugmentingScreen.class, remap = false)
public abstract class AugmentingScreenMixin {

    @Shadow protected SimpleTexButton rerollBtn;
    @Shadow protected List<AffixInstance> currentItemAffixes;
    @Shadow protected abstract int getSelectedAffix();

    @Shadow protected SimpleTexButton upgradeBtn;

    @Inject(method = "updateCachedState", at = @At("TAIL"), remap = false)
    private void disableRerollForScrollAffixes(CallbackInfo ci) {
        if (rerollBtn == null || !rerollBtn.active) return;
        int selected = this.getSelectedAffix();

        if (currentItemAffixes == null || currentItemAffixes.isEmpty()) return;
        if (selected < 0 || selected >= currentItemAffixes.size()) return;

        AugmentingMenu menu = ((AugmentingScreen) (Object) this).getMenu();
        ItemStack mainItem = menu.getMainItem();

        if (!mainItem.hasTag() || !mainItem.getTag().contains(ErasureRecipe.TAG_SCROLL_AFFIXES)) return;

        ListTag scrollList = mainItem.getTag().getList(ErasureRecipe.TAG_SCROLL_AFFIXES, Tag.TAG_STRING);
        String selectedId = currentItemAffixes.get(selected).affix().getId().toString();

        for (int i = 0; i < scrollList.size(); i++) {
            if (scrollList.getString(i).equals(selectedId)) {
                rerollBtn.active = false;
                rerollBtn.setInactiveMessage(
                        Component.translatable("button.fallen_gems_affixes.scroll_affix_no_reroll")
                                .withStyle(ChatFormatting.RED)
                );
                return;
            }
        }
    }

    @Inject(method = "updateCachedState", at = @At("TAIL"), remap = false)
    private void disableUpgradeForModifiedAffixes(CallbackInfo ci) {
        if (upgradeBtn == null || !upgradeBtn.active) return;
        int selected = this.getSelectedAffix();

        if (currentItemAffixes == null || currentItemAffixes.isEmpty()) return;
        if (selected < 0 || selected >= currentItemAffixes.size()) return;

        AugmentingMenu menu = ((AugmentingScreen) (Object) this).getMenu();
        ItemStack mainItem = menu.getMainItem();

        AffixInstance inst = currentItemAffixes.get(selected);
        var tma = SpecialAffixEventHandler.getToModifyAffixes(mainItem);
        var input = tma.getInput();
        var attrs = tma.getAttributes();
        var attr = attrs.get(inst.affix());
        if (attr != null) {
            if (attr.hasSetBase() || attr.hasSetFinal()) {
                upgradeBtn.active = false;
                upgradeBtn.setInactiveMessage(
                        Component.translatable("button.fallen_gems_affixes.no_upgrade_for_locked_level")
                                .withStyle(ChatFormatting.RED)
                );
            }
            AffixInstance instInput = input.get(inst.affix());
            if (instInput != null) {
                if (instInput.level() >= 1.0f) {
                    upgradeBtn.active = false;
                    upgradeBtn.setInactiveMessage(
                            Component.translatable("button.fallen_gems_affixes.no_upgrade_for_modified_level_cap")
                                    .withStyle(ChatFormatting.RED)
                    );
                }
            }
        }
    }
}