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
 * 修复 1.21.1 WalkingBedOnEntityTickUpdateProcedure：
 * 1. 修复 L807 反转 !isEmpty() 导致每 tick 立刻清空自身有效攻击目标的严重回归 Bug；
 * 2. 保护 MOVEMENT_SPEED 属性判空与按需写入（修复 MCR-26）；
 * 3. 仅在状态切换时刷新碰撞箱（修复 MCR-20）。
 */
@Mixin(value = WalkingBedOnEntityTickUpdateProcedure.class, remap = false)
public abstract class WalkingBedTickFixMixin {
    @Redirect(
            method = "execute",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/ai/attributes/AttributeInstance;setBaseValue(D)V"
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
                    target = "Lnet/minecraft/world/entity/Entity;refreshDimensions()V"
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
                    target = "Lnet/minecraft/world/entity/Mob;setTarget(Lnet/minecraft/world/entity/LivingEntity;)V"
            )
    )
    private static void sonsofsinsbugfix$fixInvertedTargetClear(Mob mob, LivingEntity newTarget) {
        if (newTarget == null) {
            LivingEntity current = mob.getTarget();
            if (current != null && current.isAlive() && !current.isRemoved() && current.level() == mob.level()) {
                return;
            }
        }
        mob.setTarget(newTarget);
    }
}
