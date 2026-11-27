package com.hp.sonsofsinsbugfix.mixin;

import net.mcreator.sonsofsins.entity.BludAggressiveEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 修复 1.21.1 BludAggressiveEntity#mobInteract 误允许玩家右键直接骑乘在 Boss 召唤物身上的回归 Bug。
 */
@Mixin(BludAggressiveEntity.class)
public abstract class BludAggressiveInteractFixMixin {
    @Redirect(
            method = "mobInteract",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;startRiding(Lnet/minecraft/world/entity/Entity;)Z")
    )
    private boolean sonsofsinsbugfix$blockManualRiding(Player player, Entity target) {
        return false;
    }
}
