package com.hp.sonsofsinsbugfix.mixin.client;

import com.hp.sonsofsinsbugfix.client.SonsOfSinsClientCache;
import net.mcreator.sonsofsins.procedures.GulberDisplayCondition2Procedure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 缓存圣诞帽节日判定，避免 GulberRenderer 每渲染帧调用 Calendar.getInstance() 产生大量临时对象。
 */
@Mixin(value = GulberDisplayCondition2Procedure.class, remap = false)
public abstract class GulberHolidayCacheMixin {

    @Inject(method = "execute()Z", at = @At("HEAD"), cancellable = true)
    private static void sonsofsinsbugfix$cachedDecemberCheck(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(SonsOfSinsClientCache.isDecember());
    }
}
