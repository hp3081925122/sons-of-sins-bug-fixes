package com.hp.sonsofsinsbugfix.mixin;

import com.hp.sonsofsinsbugfix.config.SonsOfSinsBugFixConfig;
import net.mcreator.sonsofsins.SonsOfSinsMod;
import net.mcreator.sonsofsins.procedures.BloodMobplayerCollidesBlockProcedure;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复 1.21.1 血池碰撞过程：仅服务端执行，并对满冷却后的 queueServerWork(20, ...) 节流防止灌爆任务队列。
 */
@Mixin(value = BloodMobplayerCollidesBlockProcedure.class, remap = false)
public abstract class BloodCollidesFixMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void sonsofsinsbugfix$serverOnly(
            LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
        if (world == null || world.isClientSide() || entity == null) {
            ci.cancel();
        }
    }

    @Redirect(
            method = "execute",
            at = @At(value = "INVOKE", target = "Lnet/mcreator/sonsofsins/SonsOfSinsMod;queueServerWork(ILjava/lang/Runnable;)V")
    )
    private static void sonsofsinsbugfix$throttleResetWork(
            int tick, Runnable action,
            LevelAccessor world, double x, double y, double z, Entity entity) {
        if (SonsOfSinsBugFixConfig.COMMON.fixWorkQueueLeaks.get() && world instanceof Level level) {
            long now = level.getGameTime();
            long last = entity.getPersistentData().getLong("sonsofsinsbugfix_blood_reset_tick");
            if (now - last < 20L) {
                return;
            }
            entity.getPersistentData().putLong("sonsofsinsbugfix_blood_reset_tick", now);
        }
        SonsOfSinsMod.queueServerWork(tick, action);
    }
}
