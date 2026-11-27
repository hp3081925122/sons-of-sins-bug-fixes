package com.hp.sonsofsinsbugfix.mixin;

import net.mcreator.sonsofsins.procedures.BoilingOnEffectActiveTickProcedure;
import net.mcreator.sonsofsins.procedures.DitchedOnEntityTickUpdateProcedure;
import net.mcreator.sonsofsins.procedures.WistiverJawOnEntityTickUpdateProcedure;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 修复 1.20.1 Boiling / Ditched / WistiverJaw 范围伤害波及掉落物、画、物品展示框等非生物实体的问题（MCR-32）。
 */
@Mixin(value = {
        BoilingOnEffectActiveTickProcedure.class,
        DitchedOnEntityTickUpdateProcedure.class,
        WistiverJawOnEntityTickUpdateProcedure.class
}, remap = false)
public abstract class AoeLivingOnlyFixMixin {

    @Redirect(
            method = "execute",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z", remap = true)
    )
    private static boolean sonsofsinsbugfix$onlyHurtLiving(Entity target, DamageSource source, float amount) {
        if (!(target instanceof LivingEntity) || !target.isAlive()) {
            return false;
        }
        return target.hurt(source, amount);
    }
}
