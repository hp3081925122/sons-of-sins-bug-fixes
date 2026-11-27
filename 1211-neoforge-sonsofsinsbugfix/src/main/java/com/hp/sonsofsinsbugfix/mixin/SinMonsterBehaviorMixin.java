package com.hp.sonsofsinsbugfix.mixin;

import com.hp.sonsofsinsbugfix.config.SonsOfSinsBugFixConfig;
import net.mcreator.sonsofsins.SinMonster;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 修复 SinMonster（NeoForge 1.21.1）：提供可配置的远距离消失开关。
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
}
