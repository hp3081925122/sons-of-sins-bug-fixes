package com.hp.sonsofsinsbugfix.mixin;

import net.mcreator.sonsofsins.procedures.EtherLanternBlockAddedProcedure;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复 MCR-12 / MCR-35（NeoForge 1.21.1）：EtherLanternBlockAddedProcedure 仅服务端执行 setBlock。
 */
@Mixin(value = EtherLanternBlockAddedProcedure.class, remap = false)
public abstract class EtherLanternBlockAddedProcedureMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void sonsofsinsbugfix$serverOnly(LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
        if (world == null || world.isClientSide()) {
            ci.cancel();
        }
    }
}
