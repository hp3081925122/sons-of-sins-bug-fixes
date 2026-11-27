package com.hp.sonsofsinsbugfix.mixin;

import net.mcreator.sonsofsins.entity.NibblerEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 修复 1.20.1 NibblerEntity#mobInteract 末尾固定返回 InteractionResult.FAIL 导致交互与装桶反馈异常的问题。
 */
@Mixin(NibblerEntity.class)
public abstract class NibblerInteractFixMixin extends TamableAnimal {
    protected NibblerInteractFixMixin(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
    }

    @Inject(method = "mobInteract", at = @At("RETURN"), cancellable = true)
    private void sonsofsinsbugfix$fixReturnResult(Player sourceentity, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (cir.getReturnValue() != InteractionResult.FAIL) {
            return;
        }
        ItemStack stack = sourceentity.getItemInHand(hand);
        if (stack.getItem() instanceof SpawnEggItem || this.isFood(stack)) {
            cir.setReturnValue(InteractionResult.sidedSuccess(this.level().isClientSide()));
        } else if (this.isTame() && this.isOwnedBy(sourceentity)) {
            cir.setReturnValue(InteractionResult.sidedSuccess(this.level().isClientSide()));
        } else {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }
}
