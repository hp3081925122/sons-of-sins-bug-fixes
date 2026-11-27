package com.hp.sonsofsinsbugfix.mixin;

import com.hp.sonsofsinsbugfix.config.SonsOfSinsBugFixConfig;
import net.mcreator.sonsofsins.SinMonster;
import net.mcreator.sonsofsins.entity.WalkingBedEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 修复 1.20.1 WalkingBedEntity#baseTick 每 tick 无条件调用 refreshDimensions() 的性能缺陷（修复 MCR-20）：
 * 仅在高度缩放条件（DATA_grave_cooldown > 1 && DATA_scream_animtime == 0）发生变化时才刷新碰撞箱。
 */
@Mixin(WalkingBedEntity.class)
public abstract class WalkingBedBaseTickMixin extends SinMonster {
    @Unique
    private int sonsofsinsbugfix$lastHeightState = -1;

    protected WalkingBedBaseTickMixin(EntityType<? extends SinMonster> type, Level level) {
        super(type, level);
    }

    @Redirect(
            method = "baseTick",
            at = @At(value = "INVOKE", target = "Lnet/mcreator/sonsofsins/entity/WalkingBedEntity;refreshDimensions()V")
    )
    private void sonsofsinsbugfix$refreshOnlyOnStateChange(WalkingBedEntity self) {
        if (!SonsOfSinsBugFixConfig.COMMON.reduceTickOverhead.get()) {
            self.refreshDimensions();
            return;
        }
        int currentState = (self.getEntityData().get(SinMonster.DATA_grave_cooldown) > 1
                && self.getEntityData().get(SinMonster.DATA_scream_animtime) == 0) ? 1 : 0;
        if (currentState != this.sonsofsinsbugfix$lastHeightState) {
            this.sonsofsinsbugfix$lastHeightState = currentState;
            self.refreshDimensions();
        }
    }
}
