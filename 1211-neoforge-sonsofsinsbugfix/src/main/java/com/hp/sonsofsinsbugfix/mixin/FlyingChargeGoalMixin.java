package com.hp.sonsofsinsbugfix.mixin;

import java.lang.reflect.Field;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复 MCR-17（NeoForge 1.21.1）：3 个飞行冲锋 Goal 缺少 getTarget() 判空导致的 Ticking entity NPE 崩服。
 */
@Mixin(targets = {
        "net.mcreator.sonsofsins.entity.CurseEntity$1",
        "net.mcreator.sonsofsins.entity.DevourerEntity$1",
        "net.mcreator.sonsofsins.entity.EtherGhostEntity$1"
})
public abstract class FlyingChargeGoalMixin extends Goal {
    @Unique
    private Field sonsofsinsbugfix$outerField;

    @Unique
    private boolean sonsofsinsbugfix$hasAliveTarget() {
        try {
            if (this.sonsofsinsbugfix$outerField == null) {
                Field f = this.getClass().getDeclaredField("this$0");
                f.setAccessible(true);
                this.sonsofsinsbugfix$outerField = f;
            }
            Object outer = this.sonsofsinsbugfix$outerField.get(this);
            if (outer instanceof Mob mob) {
                LivingEntity target = mob.getTarget();
                return target != null && target.isAlive();
            }
        } catch (Throwable ignored) {
        }
        return true;
    }

    @Inject(method = "start", at = @At("HEAD"), cancellable = true)
    private void sonsofsinsbugfix$guardStart(CallbackInfo ci) {
        if (!this.sonsofsinsbugfix$hasAliveTarget()) {
            ci.cancel();
        }
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void sonsofsinsbugfix$guardTick(CallbackInfo ci) {
        if (!this.sonsofsinsbugfix$hasAliveTarget()) {
            ci.cancel();
        }
    }
}
