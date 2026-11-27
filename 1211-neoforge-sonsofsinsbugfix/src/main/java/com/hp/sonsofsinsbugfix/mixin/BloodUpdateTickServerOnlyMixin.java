package com.hp.sonsofsinsbugfix.mixin;

import net.mcreator.sonsofsins.procedures.BloodUpdateTickProcedure;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复 1.21.1 血池刻更新在客户端空跑的问题。
 */
@Mixin(value = BloodUpdateTickProcedure.class, remap = false)
public abstract class BloodUpdateTickServerOnlyMixin {

    @Inject(method = "execute(Lnet/minecraft/world/level/LevelAccessor;DDD)V", at = @At("HEAD"), cancellable = true)
    private static void sonsofsinsbugfix$bloodTickServerOnly(
            LevelAccessor world, double x, double y, double z, CallbackInfo ci) {
        if (world == null || world.isClientSide()) {
            ci.cancel();
        }
    }
}
