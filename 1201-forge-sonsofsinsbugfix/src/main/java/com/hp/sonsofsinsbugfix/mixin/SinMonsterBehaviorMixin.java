package com.hp.sonsofsinsbugfix.mixin;

import com.hp.sonsofsinsbugfix.config.SonsOfSinsBugFixConfig;
import net.mcreator.sonsofsins.SinMonster;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 修复 SinMonster（Forge 1.20.1）：
 * 1. 提供可配置的 removeWhenFarAway 开关（默认保持 false 遵循原设计，可选开启防远距堆积）；
 * 2. 防护 travel() 在乘客列表为空时的空指针风险。
 */
@Mixin(SinMonster.class)
public abstract class SinMonsterBehaviorMixin extends Monster {
    protected SinMonsterBehaviorMixin(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    @Inject(method = "removeWhenFarAway", at = @At("HEAD"), cancellable = true)
    private void sonsofsinsbugfix$configurableDespawn(double distanceToClosestPlayer, CallbackInfoReturnable<Boolean> cir) {
        if (SonsOfSinsBugFixConfig.COMMON.allowSinMonsterDespawn.get()) {
            cir.setReturnValue(super.removeWhenFarAway(distanceToClosestPlayer));
        }
    }

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void sonsofsinsbugfix$guardNullPassengerTravel(Vec3 dir, CallbackInfo ci) {
        if (this.isVehicle() && this.getPassengers().isEmpty()) {
            super.travel(dir);
            ci.cancel();
        }
    }
}
