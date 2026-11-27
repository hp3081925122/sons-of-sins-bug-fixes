package com.hp.sonsofsinsbugfix.mixin.client;

import com.hp.sonsofsinsbugfix.config.SonsOfSinsBugFixConfig;
import net.mcreator.sonsofsins.procedures.FogColorSinMonstersProcedure;
import net.neoforged.neoforge.client.event.ViewportEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 跳过每帧空扫实体却从未调用 setColor 的无效雾色事件，并在返回时清理静态 provider 引用防止事件对象泄漏（NeoForge 1.21.1）。
 */
@Mixin(value = FogColorSinMonstersProcedure.class, remap = false)
public abstract class FogColorSinMonstersMixin {

    @Inject(method = "computeFogColor", at = @At("HEAD"), cancellable = true)
    private static void sonsofsinsbugfix$skipDeadScan(ViewportEvent.ComputeFogColor event, CallbackInfo ci) {
        if (SonsOfSinsBugFixConfig.CLIENT.skipDeadFogColorScan.get()) {
            ci.cancel();
        }
    }

    @Inject(method = "computeFogColor", at = @At("RETURN"))
    private static void sonsofsinsbugfix$clearProvider(ViewportEvent.ComputeFogColor event, CallbackInfo ci) {
        FogColorSinMonstersProcedure.provider = null;
    }
}
