package com.hp.sonsofsinsbugfix.mixin.client;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 修复 1.21.1 WalkingBedRenderer$2 发光层被错误降级为 RenderType.entityCutoutNoCull 导致失去自发光效果的退化问题。
 */
@Mixin(targets = "net.mcreator.sonsofsins.client.renderer.WalkingBedRenderer$2", remap = false)
public abstract class WalkingBedGlowLayer1211Mixin {

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/RenderType;entityCutoutNoCull(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;"
            )
    )
    private RenderType sonsofsinsbugfix$restoreEmissiveEyes(ResourceLocation texture) {
        return RenderType.eyes(texture);
    }
}
