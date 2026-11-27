package com.hp.sonsofsinsbugfix.mixin.client;

import com.hp.sonsofsinsbugfix.client.SonsOfSinsClientCache;
import net.mcreator.sonsofsins.procedures.GuzzlerDisplayConditionProcedure;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 缓存亡灵节草帽判定，避免 GuzzlerRenderer 每渲染帧 3 次调用 Calendar.getInstance()。
 */
@Mixin(value = GuzzlerDisplayConditionProcedure.class, remap = false)
public abstract class GuzzlerHolidayCacheMixin {

    @Inject(method = "execute(Lnet/minecraft/world/entity/Entity;)Z", at = @At("HEAD"), cancellable = true)
    private static void sonsofsinsbugfix$cachedDayOfDeadCheck(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity == null) {
            cir.setReturnValue(false);
            return;
        }
        cir.setReturnValue(SonsOfSinsClientCache.isDayOfDead() || "Miguel".equals(entity.getDisplayName().getString()));
    }
}
