package com.hp.sonsofsinsbugfix.mixin;

import net.mcreator.sonsofsins.entity.WalkingBedEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复 1.21.1 行走之床删掉 baseTick 中的 refreshDimensions 后碰撞箱无法在伏地/站立状态间切换的问题，且仅在状态变化时刷新。
 */
@Mixin(WalkingBedEntity.class)
public abstract class WalkingBedBaseTick1211Mixin {

    @Unique
    private int sonsofsinsbugfix$lastCrouchState = -1;

    @Inject(method = "baseTick", at = @At("TAIL"))
    private void sonsofsinsbugfix$refreshDimensionsOnStateChange(CallbackInfo ci) {
        WalkingBedEntity self = (WalkingBedEntity) (Object) this;
        boolean crouched = self.getEntityData().get(WalkingBedEntity.DATA_grave_cooldown) > 1
                && self.getEntityData().get(WalkingBedEntity.DATA_scream_animtime) == 0;
        int state = crouched ? 1 : 0;
        if (this.sonsofsinsbugfix$lastCrouchState != state) {
            this.sonsofsinsbugfix$lastCrouchState = state;
            self.refreshDimensions();
        }
    }
}
