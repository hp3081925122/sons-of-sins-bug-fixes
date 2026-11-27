package com.hp.sonsofsinsbugfix.mixin;

import net.mcreator.sonsofsins.network.SonsOfSinsModVariables;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复 MCR-04③（NeoForge 1.21.1）：PlayerVariablesSyncMessage.handleData 增加 player != null 防护及非空异常断开消息。
 */
@Mixin(value = SonsOfSinsModVariables.PlayerVariablesSyncMessage.class, remap = false)
public abstract class PlayerVariablesSyncMessageMixin {
    @Inject(method = "handleData", at = @At("HEAD"), cancellable = true)
    private static void sonsofsinsbugfix$safeHandleData(
            SonsOfSinsModVariables.PlayerVariablesSyncMessage message,
            IPayloadContext context,
            CallbackInfo ci) {
        ci.cancel();
        if (context.flow() != PacketFlow.CLIENTBOUND || message == null || message.data() == null) {
            return;
        }
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player == null) {
                return;
            }
            SonsOfSinsModVariables.PlayerVariables target = player.getData(SonsOfSinsModVariables.PLAYER_VARIABLES);
            target.deserializeNBT(player.registryAccess(), message.data().serializeNBT(player.registryAccess()));
        }).exceptionally(e -> {
            String msg = e.getMessage() != null ? e.getMessage() : e.toString();
            context.connection().disconnect(Component.literal(msg));
            return null;
        });
    }
}
