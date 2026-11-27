package com.hp.sonsofsinsbugfix.mixin;

import net.mcreator.sonsofsins.entity.DevourerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 取消 1.20.1 DevourerEntity.init() 中注册的 NO_RESTRICTIONS 永真刷怪规则（修复 MCR-15 遗留风险，与 1.21.1 保持一致）。
 */
@Mixin(value = DevourerEntity.class, remap = false)
public abstract class DevourerSpawnMixin {
    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private static void sonsofsinsbugfix$cancelUnrestrictedSpawnPlacement(CallbackInfo ci) {
        ci.cancel();
    }
}
