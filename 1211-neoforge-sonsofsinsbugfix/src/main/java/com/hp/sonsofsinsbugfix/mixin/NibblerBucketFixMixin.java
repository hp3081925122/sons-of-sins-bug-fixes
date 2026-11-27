package com.hp.sonsofsinsbugfix.mixin;

import net.mcreator.sonsofsins.procedures.NibblerBucketedProcedure;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复 1.21.1 NibblerBucketedProcedure：支持副手持桶、防止对已移除实体重复装桶、成功后取消交互事件。
 */
@Mixin(value = NibblerBucketedProcedure.class, remap = false)
public abstract class NibblerBucketFixMixin {
    @Redirect(
            method = "onRightClickEntity",
            at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/event/entity/player/PlayerInteractEvent$EntityInteract;getHand()Lnet/minecraft/world/InteractionHand;")
    )
    private static InteractionHand sonsofsinsbugfix$allowBothHands(PlayerInteractEvent.EntityInteract event) {
        return event.getEntity().getUsedItemHand();
    }

    @Inject(method = "onRightClickEntity", at = @At("HEAD"), cancellable = true)
    private static void sonsofsinsbugfix$skipRemovedTarget(PlayerInteractEvent.EntityInteract event, CallbackInfo ci) {
        if (event == null || event.getTarget() == null || event.getTarget().isRemoved()) {
            ci.cancel();
        }
    }

    @Inject(method = "onRightClickEntity", at = @At("TAIL"))
    private static void sonsofsinsbugfix$cancelEventAfterBucketed(PlayerInteractEvent.EntityInteract event, CallbackInfo ci) {
        if (event != null && event.getTarget() != null && event.getTarget().isRemoved()) {
            event.setCanceled(true);
        }
    }
}
