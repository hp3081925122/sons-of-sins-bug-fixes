package com.hp.sonsofsinsbugfix.mixin;

import net.mcreator.sonsofsins.init.SonsOfSinsModItems;
import net.mcreator.sonsofsins.procedures.CreeperRibsThunderingProcedure;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复 1.20.1 苦力怕肋骨雷击充能仅服务端执行，并在充能成功后取消本次雷击事件避免同 tick 重复触发。
 */
@Mixin(value = CreeperRibsThunderingProcedure.class, remap = false)
public abstract class CreeperRibsLightningFixMixin {

    @Inject(method = "onEntityStruckByLightning", at = @At("HEAD"), cancellable = true)
    private static void sonsofsinsbugfix$guardLightningServerOnly(EntityStruckByLightningEvent event, CallbackInfo ci) {
        if (event == null || event.getEntity() == null || event.getEntity().isRemoved() || event.getEntity().level().isClientSide()) {
            ci.cancel();
        }
    }

    @Inject(method = "onEntityStruckByLightning", at = @At("TAIL"))
    private static void sonsofsinsbugfix$cancelLightningAfterCharge(EntityStruckByLightningEvent event, CallbackInfo ci) {
        if (event != null && event.getEntity() instanceof LivingEntity living && event.isCancelable()) {
            if (living.getMainHandItem().getItem() == SonsOfSinsModItems.CHARGED_CREEPER_RIBS.get()
                    || living.getOffhandItem().getItem() == SonsOfSinsModItems.CHARGED_CREEPER_RIBS.get()) {
                event.setCanceled(true);
            }
        }
    }
}
