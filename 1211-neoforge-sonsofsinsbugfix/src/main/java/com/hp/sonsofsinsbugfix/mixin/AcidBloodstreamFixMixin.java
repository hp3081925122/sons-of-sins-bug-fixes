package com.hp.sonsofsinsbugfix.mixin;

import net.mcreator.sonsofsins.procedures.AcidBloodstreamOnEffectActiveTickProcedure;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复 1.21.1 酸性血流（AcidBloodstream）每 tick 双端无条件调 removeEffect(POISON) 引发的 MobEffectEvent.Remove 风暴（MCR-27）。
 */
@Mixin(value = AcidBloodstreamOnEffectActiveTickProcedure.class, remap = false)
public abstract class AcidBloodstreamFixMixin {

    @Inject(method = "execute(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private static void sonsofsinsbugfix$onlyRemovePoisonWhenPresent(Entity entity, CallbackInfo ci) {
        if (!(entity instanceof LivingEntity living) || entity.level().isClientSide() || !living.hasEffect(MobEffects.POISON)) {
            ci.cancel();
        }
    }
}
