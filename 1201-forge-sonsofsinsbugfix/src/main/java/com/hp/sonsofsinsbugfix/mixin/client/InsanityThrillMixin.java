package com.hp.sonsofsinsbugfix.mixin.client;

import net.mcreator.sonsofsins.procedures.InsanityThrillProcedure;
import net.minecraftforge.client.event.ViewportEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 在相机角度事件结束后清空静态 provider 字段，避免跨帧强引用 ViewportEvent 实例。
 */
@Mixin(value = InsanityThrillProcedure.class, remap = false)
public abstract class InsanityThrillMixin {

    @Inject(method = "computeCameraAngles", at = @At("RETURN"))
    private static void sonsofsinsbugfix$clearProvider(ViewportEvent.ComputeCameraAngles event, CallbackInfo ci) {
        InsanityThrillProcedure.provider = null;
    }
}
