package com.hp.sonsofsinsbugfix.mixin;

import java.util.HashMap;
import net.mcreator.sonsofsins.procedures.FlessCarcrassWhileThisGUIIsOpenTickProcedure;
import net.mcreator.sonsofsins.world.inventory.FlessCarcassGUIMenu;
import net.mcreator.sonsofsins.world.inventory.PuppetOfStrifeGUIMenu;
import net.mcreator.sonsofsins.world.inventory.UrnGUIMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 修复 MCR-05 与远程 OOM DoS（NeoForge 1.21.1）：
 * 1. 截断 readTextState 的无上限 buffer.readInt() 反序列化（最多 16 项），防恶意客户端打爆服务端内存；
 * 2. 禁止 handleSyncAction 向全局静态永不清理的 guistate 写入客户端任意字符串，并严格校验玩家已打开对应容器。
 */
@Mixin(value = {
        FlessCarcassGUIMenu.FlessCarcassGUISyncMessage.class,
        PuppetOfStrifeGUIMenu.PuppetOfStrifeGUISyncMessage.class,
        UrnGUIMenu.UrnGUISyncMessage.class
}, remap = false)
public abstract class GuiSyncHardeningMixin {
    @Inject(method = "readTextState", at = @At("HEAD"), cancellable = true)
    private static void sonsofsinsbugfix$capTextState(RegistryFriendlyByteBuf buffer, CallbackInfoReturnable<HashMap<String, String>> cir) {
        int size = buffer.readInt();
        if (size < 0 || size > 16) {
            throw new IllegalArgumentException("Invalid GUI sync textstate size: " + size);
        }
        HashMap<String, String> map = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            String key = net.minecraft.network.chat.ComponentSerialization.TRUSTED_STREAM_CODEC.decode(buffer).getString(128);
            String val = net.minecraft.network.chat.ComponentSerialization.TRUSTED_STREAM_CODEC.decode(buffer).getString(256);
            map.put(key, val);
        }
        cir.setReturnValue(map);
    }

    @Inject(method = "handleSyncAction", at = @At("HEAD"), cancellable = true)
    private static void sonsofsinsbugfix$guardSyncAction(
            Player entity, int mode, int x, int y, int z, HashMap<String, String> textstate, CallbackInfo ci) {
        ci.cancel();
        if (entity == null || !entity.isAlive() || entity.isRemoved()) {
            return;
        }
        boolean inMenu = entity.containerMenu instanceof FlessCarcassGUIMenu
                || entity.containerMenu instanceof PuppetOfStrifeGUIMenu
                || entity.containerMenu instanceof UrnGUIMenu;
        if (!inMenu) {
            return;
        }
        if (mode == 0) {
            FlessCarcrassWhileThisGUIIsOpenTickProcedure.execute(entity);
        }
    }
}
