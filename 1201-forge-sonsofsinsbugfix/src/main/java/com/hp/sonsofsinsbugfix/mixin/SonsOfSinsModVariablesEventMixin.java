package com.hp.sonsofsinsbugfix.mixin;

import net.mcreator.sonsofsins.network.SonsOfSinsModVariables;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复 MCR-04（Forge 1.20.1）：
 * 1. PlayerEvent.Clone 禁止调用 original.revive() 清空 RemovalReason，改用安全的 reviveCaps()/invalidateCaps()；
 * 2. 登录/切维事件仅对 ServerPlayer 发包，防止假人 ClassCastException。
 */
@Mixin(value = SonsOfSinsModVariables.EventBusVariableHandlers.class, remap = false)
public abstract class SonsOfSinsModVariablesEventMixin {
    @Inject(method = "clonePlayer", at = @At("HEAD"), cancellable = true)
    private static void sonsofsinsbugfix$safeClonePlayer(PlayerEvent.Clone event, CallbackInfo ci) {
        ci.cancel();
        Player originalPlayer = event.getOriginal();
        Player newPlayer = event.getEntity();
        if (originalPlayer == null || newPlayer == null || event.isWasDeath()) {
            return;
        }
        originalPlayer.reviveCaps();
        try {
            originalPlayer.getCapability(SonsOfSinsModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(origVars ->
                    newPlayer.getCapability(SonsOfSinsModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(cloneVars ->
                            cloneVars.currentOpenItem = origVars.currentOpenItem));
        } finally {
            originalPlayer.invalidateCaps();
        }
    }

    @Inject(method = "onPlayerLoggedIn", at = @At("HEAD"), cancellable = true)
    private static void sonsofsinsbugfix$guardLoggedIn(PlayerEvent.PlayerLoggedInEvent event, CallbackInfo ci) {
        if (!(event.getEntity() instanceof ServerPlayer)) {
            ci.cancel();
        }
    }

    @Inject(method = "onPlayerChangedDimension", at = @At("HEAD"), cancellable = true)
    private static void sonsofsinsbugfix$guardChangedDim(PlayerEvent.PlayerChangedDimensionEvent event, CallbackInfo ci) {
        if (!(event.getEntity() instanceof ServerPlayer)) {
            ci.cancel();
        }
    }
}
