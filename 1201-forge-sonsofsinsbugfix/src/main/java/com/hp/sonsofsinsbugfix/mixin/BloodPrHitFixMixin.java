package com.hp.sonsofsinsbugfix.mixin;

import net.mcreator.sonsofsins.entity.BloodPrEntity;
import net.mcreator.sonsofsins.procedures.BloodPrProjectileHitsLivingEntityProcedure;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复 BloodPrEntity 命中时无视盾牌格挡与无敌直接触发贪婪效果的问题：
 * 将命中过程移至原版伤害确认成功后的 doPostHurtEffects 中执行。
 */
@Mixin(BloodPrEntity.class)
public abstract class BloodPrHitFixMixin {
    @Redirect(
            method = "onHitEntity",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/mcreator/sonsofsins/procedures/BloodPrProjectileHitsLivingEntityProcedure;execute(Lnet/minecraft/world/entity/Entity;)V",
                    remap = false
            )
    )
    private void sonsofsinsbugfix$skipUnconditionalHitProc(Entity target) {
        // 延迟至 doPostHurtEffects 中确认造成有效伤害后再施加效果
    }

    @Inject(method = "doPostHurtEffects", at = @At("TAIL"))
    private void sonsofsinsbugfix$applyOnConfirmedHit(LivingEntity target, CallbackInfo ci) {
        BloodPrProjectileHitsLivingEntityProcedure.execute(target);
    }
}
