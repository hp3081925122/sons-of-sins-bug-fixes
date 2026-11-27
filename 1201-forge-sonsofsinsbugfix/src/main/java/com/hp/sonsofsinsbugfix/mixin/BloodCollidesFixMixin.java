package com.hp.sonsofsinsbugfix.mixin;

import com.hp.sonsofsinsbugfix.config.SonsOfSinsBugFixConfig;
import net.mcreator.sonsofsins.SonsOfSinsMod;
import net.mcreator.sonsofsins.procedures.BloodMobplayerCollidesBlockProcedure;
import net.mcreator.sonsofsins.procedures.BloodUpdateTickProcedure;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复 1.20.1 血池碰撞与血池刻更新过程：
 * 1. 仅服务端执行，避免双端状态分叉；
 * 2. 对 BloodMobplayerCollidesBlock 中满冷却后的 queueServerWork(20, ...) 做 20 tick 节流，防止站立血池灌爆任务队列；
 * 3. 修复 1.20.1 进度判空（MCR-31）。
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

    @Redirect(
            method = "execute",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerAdvancements;getOrStartProgress(Lnet/minecraft/advancements/Advancement;)Lnet/minecraft/advancements/AdvancementProgress;", remap = true)
    )
    private static AdvancementProgress sonsofsinsbugfix$safeAdvancementProgress(PlayerAdvancements advancements, Advancement adv) {
        if (adv == null) {
            return new AdvancementProgress();
        }
        return advancements.getOrStartProgress(adv);
    }
}
