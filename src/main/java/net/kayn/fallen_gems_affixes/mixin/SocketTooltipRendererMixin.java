package net.kayn.fallen_gems_affixes.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.shadowsoffire.apotheosis.adventure.client.SocketTooltipRenderer;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemInstance;
import dev.shadowsoffire.placebo.color.GradientColor;
import net.kayn.fallen_gems_affixes.adventure.socket.CatalystSocketConfig;
import net.kayn.fallen_gems_affixes.adventure.socket.CatalystSocketHelper;
import net.kayn.fallen_gems_affixes.adventure.socket.SocketTierDefinition;
import net.kayn.fallen_gems_affixes.adventure.socket.SocketTierManager;
import net.kayn.fallen_gems_affixes.adventure.socket.TieredSocketHelper;
import net.kayn.fallen_gems_affixes.client.RenderSlot;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Mixin(value = SocketTooltipRenderer.class, remap = false)
public class SocketTooltipRendererMixin {

    @Shadow @Final public static ResourceLocation SOCKET;

    private static final ResourceLocation TIERED_SOCKET =
            ResourceLocation.fromNamespaceAndPath("fallen_gems_affixes", "textures/gui/tiered_socket.png");

    @Shadow @Final private SocketTooltipRenderer.SocketComponent comp;
    @Shadow @Final private int spacing;

    @Inject(method = "renderText", at = @At("HEAD"), cancellable = true, remap = true)
    private void renderTieredText(Font font, int x, int y, Matrix4f matrix,
                                  BufferSource bufferSource, CallbackInfo ci) {

        ItemStack socketed = this.comp.socketed();
        if (socketed.isEmpty()) return;
        if (CatalystSocketHelper.hasCatalystSocket(socketed)) {
            ci.cancel();
            GemInstance inst = this.comp.gems().isEmpty() ? null : this.comp.gems().get(0);
            Component text;

            if (inst != null && inst.isValid()) {
                float multi = CatalystSocketHelper.getGemPowerMultiplier(socketed);
                text = Component.translatable("socket.fallen_gems_affixes.catalyst.filled",
                        inst.getSocketBonusTooltip(),
                        String.format("%.0f", (multi - 1.0f) * 100));
            } else {
                int count = CatalystSocketHelper.getCatalystSocketCount(socketed);
                float power = CatalystSocketHelper.getCatalystPower(socketed);
                text = Component.translatable("socket.fallen_gems_affixes.catalyst.empty",
                        count, String.format("%.0f", count * power * 100));
            }

            font.drawInBatch(text, x + 12, y + 1, catalystTextColor(), true, matrix, bufferSource,
                    Font.DisplayMode.NORMAL, 0, 15728880);
            return;
        }

        if (!SocketTierManager.INSTANCE.isEnabled()) return;
        ci.cancel();

        List<RenderSlot> slots = buildSortedSlots(socketed);
        for (int i = 0; i < slots.size(); i++) {
            RenderSlot slot = slots.get(i);
            final Component text;
            final int color;

            if (slot.tier() == TieredSocketHelper.REGULAR_SOCKET) {
                text  = SocketTooltipRenderer.getSocketDesc(slot.gem());
                color = 0xAABBCC;
            } else if (!slot.gem().isValid()) {
                text  = Component.translatable(TieredSocketHelper.getEmptySocketTranslationKey(slot.tier()));
                color = resolveTextColor(slot.tier());
            } else {
                text  = slot.gem().getSocketBonusTooltip();
                color = 0xAABBCC;
            }

            font.drawInBatch(text, x + 12, y + 1 + this.spacing * i,
                    color, true, matrix, bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
        }
    }

    @Inject(method = "renderImage", at = @At("HEAD"), cancellable = true, remap = true)
    private void renderTieredImage(Font font, int x, int y, GuiGraphics gfx, CallbackInfo ci) {

        ItemStack socketed = this.comp.socketed();

        if (CatalystSocketHelper.hasCatalystSocket(socketed)) {
            ci.cancel();
            float[] rgb = catalystRGB();
            RenderSystem.setShaderColor(rgb[0], rgb[1], rgb[2], 1f);
            gfx.blit(TIERED_SOCKET, x, y, 0, 0, 0, 9, 9, 9, 9);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

            if (!this.comp.gems().isEmpty() && this.comp.gems().get(0).isValid()) {
                PoseStack pose = gfx.pose();
                pose.pushPose();
                pose.scale(0.5F, 0.5F, 1F);
                gfx.renderFakeItem(this.comp.gems().get(0).gemStack(), 2 * x + 1, 2 * y + 1);
                pose.popPose();
            }
            return;
        }

        if (!SocketTierManager.INSTANCE.isEnabled()) return;
        ci.cancel();

        List<RenderSlot> slots = buildSortedSlots(socketed);

        for (int i = 0; i < slots.size(); i++) {
            int tier  = slots.get(i).tier();
            int iconY = y + this.spacing * i;

            if (tier == TieredSocketHelper.REGULAR_SOCKET) {
                RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
                gfx.blit(SOCKET, x, iconY, 0, 0, 0, 9, 9, 9, 9);
            } else {
                float[] rgb = resolveRGB(tier);
                RenderSystem.setShaderColor(rgb[0], rgb[1], rgb[2], 1f);
                gfx.blit(TIERED_SOCKET, x, iconY, 0, 0, 0, 9, 9, 9, 9);
                RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            }
        }

        int currentY = y;
        for (RenderSlot slot : slots) {
            if (slot.gem().isValid()) {
                PoseStack pose = gfx.pose();
                pose.pushPose();
                pose.scale(0.5F, 0.5F, 1F);
                gfx.renderFakeItem(slot.gem().gemStack(), 2 * x + 1, 2 * currentY + 1);
                pose.popPose();
            }
            currentY += this.spacing;
        }
    }

    private List<RenderSlot> buildSortedSlots(ItemStack socketed) {
        List<RenderSlot> slots = new ArrayList<>(this.comp.gems().size());
        for (int i = 0; i < this.comp.gems().size(); i++) {
            int tier = TieredSocketHelper.getSocketTier(socketed, i);
            slots.add(new RenderSlot(i, tier, this.comp.gems().get(i)));
        }
        slots.sort(Comparator.comparingInt(s ->
                s.tier() == TieredSocketHelper.REGULAR_SOCKET ? Integer.MAX_VALUE : s.tier()));
        return slots;
    }

    private static int catalystTextColor() {
        CatalystSocketConfig cfg = CatalystSocketConfig.INSTANCE;
        if (cfg.isRainbow()) return GradientColor.RAINBOW.getValue() | 0xFF000000;
        return cfg.getColorPacked() | 0xFF000000;
    }

    private static float[] catalystRGB() {
        CatalystSocketConfig cfg = CatalystSocketConfig.INSTANCE;
        int packed = cfg.isRainbow() ? GradientColor.RAINBOW.getValue() : cfg.getColorPacked();
        return new float[]{((packed >> 16) & 0xFF) / 255f, ((packed >> 8) & 0xFF) / 255f, (packed & 0xFF) / 255f};
    }

    private static int resolveTextColor(int ordinal) {
        SocketTierDefinition def = SocketTierManager.INSTANCE.getByOrdinal(ordinal);
        if (def == null) return 0xFFFFFF;
        if (def.rainbow()) return GradientColor.RAINBOW.getValue() | 0xFF000000;
        return def.colorPacked() | 0xFF000000;
    }

    private static float[] resolveRGB(int ordinal) {
        SocketTierDefinition def = SocketTierManager.INSTANCE.getByOrdinal(ordinal);
        int packed;
        if (def == null) packed = 0xFFFFFF;
        else if (def.rainbow()) packed = GradientColor.RAINBOW.getValue();
        else packed = def.colorPacked();
        return new float[]{((packed >> 16) & 0xFF) / 255f, ((packed >> 8) & 0xFF) / 255f, (packed & 0xFF) / 255f};
    }
}