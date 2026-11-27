package com.hp.sonsofsinsbugfix.mixin;

import net.mcreator.sonsofsins.entity.BloodPrEntity;
import net.mcreator.sonsofsins.entity.NullPrEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 修复 BloodPrEntity 与 NullPrEntity 弹射物缺陷（修复 MCR-18）：
 * 1. 禁止拾取（DISALLOWED）且 getPickupItem 返回副本，避免修改共享 PROJECTILE_ITEM 单例；
 * 2. 限制 setArrowCount 不小于 0，防止出现负数箭矢计数；
 * 3. 超过 200 tick 自动销毁，防止向天空发射的弹射物永久泄漏。
 */
@Mixin({BloodPrEntity.class, NullPrEntity.class})
public abstract class ProjectileSafetyMixin extends AbstractArrow {
    protected ProjectileSafetyMixin(EntityType<? extends AbstractArrow> type, Level level) {
        super(type, level);
    }

    @Inject(method = "<init>*", at = @At("TAIL"))
    private void sonsofsinsbugfix$disallowPickup(CallbackInfo ci) {
        this.pickup = AbstractArrow.Pickup.DISALLOWED;
    }

    @Inject(method = "getPickupItem", at = @At("RETURN"), cancellable = true)
    private void sonsofsinsbugfix$copyPickupItem(CallbackInfoReturnable<ItemStack> cir) {
        ItemStack ret = cir.getReturnValue();
        if (ret != null && !ret.isEmpty()) {
            cir.setReturnValue(ret.copy());
        }
    }

    @ModifyArg(
            method = "doPostHurtEffects",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setArrowCount(I)V"),
            index = 0
    )
    private int sonsofsinsbugfix$clampArrowCount(int count) {
        return Math.max(0, count);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void sonsofsinsbugfix$discardAfterTimeout(CallbackInfo ci) {
        if (!this.level().isClientSide() && this.tickCount >= 200) {
            this.discard();
        }
    }
}
