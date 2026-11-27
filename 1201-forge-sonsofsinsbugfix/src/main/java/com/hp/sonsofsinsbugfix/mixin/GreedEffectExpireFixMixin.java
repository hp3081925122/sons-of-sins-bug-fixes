package com.hp.sonsofsinsbugfix.mixin;

import net.mcreator.sonsofsins.procedures.GreedEffectExpiresProcedure;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复 1.20.1 贪婪效果结束时误改写旁观者玩家 noPhysics 的问题，并同步清理持久化穿墙标记。
 */
@Mixin(value = GreedEffectExpiresProcedure.class, remap = false)
public abstract class GreedEffectExpireFixMixin {

    @Inject(method = "execute(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private static void sonsofsinsbugfix$guardSpectatorOnExpire(Entity entity, CallbackInfo ci) {
        if (entity == null) {
            ci.cancel();
            return;
        }
        if (entity.getPersistentData().getBoolean("sonsofsinsbugfix_greed_nophysics")) {
            entity.getPersistentData().remove("sonsofsinsbugfix_greed_nophysics");
        }
        if (entity instanceof Player player && player.isSpectator()) {
            ci.cancel();
        }
    }
}
