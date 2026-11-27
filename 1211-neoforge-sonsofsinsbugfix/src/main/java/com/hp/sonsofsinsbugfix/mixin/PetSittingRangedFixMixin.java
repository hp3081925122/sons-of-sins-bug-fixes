package com.hp.sonsofsinsbugfix.mixin;

import net.mcreator.sonsofsins.entity.DevourerEntity;
import net.mcreator.sonsofsins.entity.GulberEntity;
import net.mcreator.sonsofsins.entity.GuzzlerEntity;
import net.mcreator.sonsofsins.procedures.DevourerSitConditionProcedure;
import net.mcreator.sonsofsins.procedures.GulberSitConditionProcedure;
import net.mcreator.sonsofsins.procedures.GuzzlerNotmoveConditionProcedure;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 阻止处于坐下/待机状态的 Devourer、Gulber、Guzzler 继续发射 NullPrEntity（NeoForge 1.21.1）。
 */
@Mixin({DevourerEntity.class, GulberEntity.class, GuzzlerEntity.class})
public abstract class PetSittingRangedFixMixin {
    @Inject(method = "performRangedAttack", at = @At("HEAD"), cancellable = true)
    private void sonsofsinsbugfix$cancelWhenSitting(LivingEntity target, float flval, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (self instanceof DevourerEntity && !DevourerSitConditionProcedure.execute(self)) {
            ci.cancel();
        } else if (self instanceof GulberEntity && !GulberSitConditionProcedure.execute(self)) {
            ci.cancel();
        } else if (self instanceof GuzzlerEntity) {
            int status = self.getEntityData().get(GuzzlerEntity.DATA_status);
            if (status == 2 || !GuzzlerNotmoveConditionProcedure.execute(self)) {
                ci.cancel();
            }
        }
    }
}
