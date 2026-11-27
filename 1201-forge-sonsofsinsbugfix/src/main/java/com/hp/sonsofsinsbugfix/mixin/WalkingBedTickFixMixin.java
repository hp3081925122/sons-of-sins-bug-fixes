package com.hp.sonsofsinsbugfix.mixin;

import com.hp.sonsofsinsbugfix.config.SonsOfSinsBugFixConfig;
import net.mcreator.sonsofsins.SinMonster;
import net.mcreator.sonsofsins.procedures.WalkingBedOnEntityTickUpdateProcedure;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 修复 WalkingBedOnEntityTickUpdateProcedure：
 * 1. 保护 MOVEMENT_SPEED 属性判空，且仅在目标值变化时写入，避免每 tick 覆盖并消除空指针（修复 MCR-26）；
 * 2. 仅在 DATA_grave_cooldown 状态未进入 2 时才触发 refreshDimensions()（修复 MCR-20）；
 * 3. 阻止对存活有效目标误清零仇恨。
 */
@Mixin(value = WalkingBedOnEntityTickUpdateProcedure.class, remap = false)
public abstract class WalkingBedTickFixMixin {
    @Redirect(
            method = "execute",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/ai/attributes/AttributeInstance;setBaseValue(D)V",
                    remap = true
            )
    )
    private static void sonsofsinsbugfix$safeSetBaseValue(AttributeInstance instance, double value) {
        if (instance == null) {
            return;
        }
        if (!SonsOfSinsBugFixConfig.COMMON.reduceTickOverhead.get()
                || Math.abs(instance.getBaseValue() - value) > 1.0E-6) {
            instance.setBaseValue(value);
        }
    }

    @Redirect(
            method = "execute",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;refreshDimensions()V",
                    remap = true
            )
    )
    private static void sonsofsinsbugfix$refreshOnlyOnStageEnter(Entity entity) {
        if (entity instanceof SinMonster sin) {
            if (!SonsOfSinsBugFixConfig.COMMON.reduceTickOverhead.get()
                    || sin.getEntityData().get(SinMonster.DATA_grave_cooldown) != 2) {
                entity.refreshDimensions();
            }
        } else {
            entity.refreshDimensions();
        }
    }

    @Redirect(
            method = "execute",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Mob;setTarget(Lnet/minecraft/world/entity/LivingEntity;)V",
                    remap = true
            )
    )
    private static void sonsofsinsbugfix$preventSpuriousTargetClear(Mob mob, LivingEntity newTarget) {
        if (newTarget == null) {
            LivingEntity current = mob.getTarget();
            if (current != null && current.isAlive() && !current.isRemoved() && current.level() == mob.level()) {
                return;
            }
        }
        mob.setTarget(newTarget);
    }
}
