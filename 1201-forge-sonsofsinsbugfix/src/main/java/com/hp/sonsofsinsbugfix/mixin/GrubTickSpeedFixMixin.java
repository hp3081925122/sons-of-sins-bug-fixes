package com.hp.sonsofsinsbugfix.mixin;

import com.hp.sonsofsinsbugfix.config.SonsOfSinsBugFixConfig;
import net.mcreator.sonsofsins.procedures.GrubOnEntityTickUpdateProcedure;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 修复 1.20.1 Grub 每 tick 无条件覆写 MOVEMENT_SPEED 且未判空的问题（MCR-26）。
 */
@Mixin(value = GrubOnEntityTickUpdateProcedure.class, remap = false)
public abstract class GrubTickSpeedFixMixin {

    @Redirect(
            method = "execute(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/world/entity/Entity;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/attributes/AttributeInstance;setBaseValue(D)V", remap = true)
    )
    private static void sonsofsinsbugfix$safeGrubSpeed(AttributeInstance instance, double value) {
        if (instance == null) {
            return;
        }
        if (!SonsOfSinsBugFixConfig.COMMON.reduceTickOverhead.get()
                || Math.abs(instance.getBaseValue() - value) > 1.0E-6) {
            instance.setBaseValue(value);
        }
    }
}
