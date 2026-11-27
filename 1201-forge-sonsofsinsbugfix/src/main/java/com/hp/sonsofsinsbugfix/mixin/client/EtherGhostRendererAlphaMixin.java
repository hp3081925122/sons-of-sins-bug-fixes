package com.hp.sonsofsinsbugfix.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 修复 1.20.1 以太幽灵（EtherGhost）在血肉进度超过 1570 后 _alpha 变负/超界导致 RGBA 通道越界的问题。
 */
@Mixin(targets = {
        "net.mcreator.sonsofsins.client.renderer.EtherGhostRenderer$1",
        "net.mcreator.sonsofsins.client.renderer.EtherGhostRenderer$2",
        "net.mcreator.sonsofsins.client.renderer.EtherGhostRenderer$3",
        "net.mcreator.sonsofsins.client.renderer.EtherGhostRenderer$4",
        "net.mcreator.sonsofsins.client.renderer.EtherGhostRenderer$5",
        "net.mcreator.sonsofsins.client.renderer.EtherGhostRenderer$6"
}, remap = false)
public abstract class EtherGhostRendererAlphaMixin {

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/EntityModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V",
                    remap = true
            )
    )
    private void sonsofsinsbugfix$clampAlpha(
            EntityModel<?> model, PoseStack poseStack, VertexConsumer buffer,
            int packedLight, int packedOverlay, float r, float g, float b, float a) {
        model.renderToBuffer(
                poseStack, buffer, packedLight, packedOverlay,
                Mth.clamp(r, 0.0F, 1.0F),
                Mth.clamp(g, 0.0F, 1.0F),
                Mth.clamp(b, 0.0F, 1.0F),
                Mth.clamp(a, 0.0F, 1.0F)
        );
    }
}
