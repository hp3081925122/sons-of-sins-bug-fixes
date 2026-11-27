package com.hp.sonsofsinsbugfix.mixin;

import net.mcreator.sonsofsins.init.SonsOfSinsModMobEffects;
import net.mcreator.sonsofsins.procedures.GreedOnEffectActiveTickProcedure;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复 1.20.1 贪婪效果（Greed）穿墙逻辑：不覆盖旁观者模式，并记录持久化标记以便掉线重连后恢复碰撞。
 */
@Mixin(value = GreedOnEffectActiveTickProcedure.class, remap = false)
public abstract class GreedEffectActiveFixMixin {

    @Inject(method = "execute(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private static void sonsofsinsbugfix$guardGreedSpectator(LevelAccessor world, Entity entity, CallbackInfo ci) {
        if (entity == null || (entity instanceof Player player && player.isSpectator())) {
            ci.cancel();
            return;
        }
        if (entity instanceof LivingEntity living && living.hasEffect(SonsOfSinsModMobEffects.GREED.get())) {
            entity.getPersistentData().putBoolean("sonsofsinsbugfix_greed_nophysics", true);
        }
    }
}
