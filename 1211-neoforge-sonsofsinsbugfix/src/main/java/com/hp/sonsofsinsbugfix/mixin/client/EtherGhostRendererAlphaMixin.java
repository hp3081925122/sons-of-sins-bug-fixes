package com.hp.sonsofsinsbugfix.mixin.client;

import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 修复 1.21.1 以太幽灵（EtherGhost）在血肉进度超过 1570 后 _alpha 变负/超界导致 FastColor.ARGB32.color 发生位溢出花色的问题。
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
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/FastColor$ARGB32;color(IIII)I")
    )
    private int sonsofsinsbugfix$clampArgb(int a, int r, int g, int b) {
        return FastColor.ARGB32.color(
                Mth.clamp(a, 0, 255),
                Mth.clamp(r, 0, 255),
                Mth.clamp(g, 0, 255),
                Mth.clamp(b, 0, 255)
        );
    }
}
