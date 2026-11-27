package com.hp.sonsofsinsbugfix.mixin.client;

import java.util.function.Supplier;
import net.mcreator.sonsofsins.network.SonsOfSinsModVariables;
import net.minecraft.client.Minecraft;
import net.minecraftforge.network.NetworkEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复 MCR-04③（Forge 1.20.1）：登录同步包早到时 Minecraft.getInstance().player 为 null 导致客户端 NPE 断线。
 */
@Mixin(value = SonsOfSinsModVariables.PlayerVariablesSyncMessage.class, remap = false)
public abstract class PlayerVariablesSyncMessageClientMixin {
    @Inject(method = "handler", at = @At("HEAD"), cancellable = true)
    private static void sonsofsinsbugfix$safeClientHandler(
            SonsOfSinsModVariables.PlayerVariablesSyncMessage message,
            Supplier<NetworkEvent.Context> contextSupplier,
            CallbackInfo ci) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isServer()) {
            context.setPacketHandled(true);
            ci.cancel();
            return;
        }
        ci.cancel();
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().player == null || message == null) {
                return;
            }
            Minecraft.getInstance().player.getCapability(SonsOfSinsModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                    .ifPresent(vars -> {
                        try {
                            java.lang.reflect.Field dataField = SonsOfSinsModVariables.PlayerVariablesSyncMessage.class.getDeclaredField("data");
                            dataField.setAccessible(true);
                            SonsOfSinsModVariables.PlayerVariables incoming = (SonsOfSinsModVariables.PlayerVariables) dataField.get(message);
                            if (incoming != null && incoming.currentOpenItem != null) {
                                vars.currentOpenItem = incoming.currentOpenItem;
                            }
                        } catch (Throwable ignored) {
                        }
                    });
        });
        context.setPacketHandled(true);
    }
}
