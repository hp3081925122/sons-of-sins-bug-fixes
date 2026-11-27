package com.hp.sonsofsinsbugfix.mixin;

import net.mcreator.sonsofsins.item.UrnItem;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 修复 UrnItem#useOn 未判空 context.getPlayer() 导致假人/非玩家调用直接抛出 NPE（Forge 1.20.1）。
 */
@Mixin(UrnItem.class)
public abstract class UrnItemMixin {
    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void sonsofsinsbugfix$guardNullPlayer(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (context == null || context.getPlayer() == null) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }
}
