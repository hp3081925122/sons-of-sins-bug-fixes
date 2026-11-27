package com.hp.sonsofsinsbugfix.mixin.client;

import net.mcreator.sonsofsins.client.gui.FlessCarcassGUIScreen;
import net.mcreator.sonsofsins.client.gui.PuppetOfStrifeGUIScreen;
import net.mcreator.sonsofsins.client.gui.UrnGUIScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复 NeoForge 1.21.1 三个便携容器界面每 tick 无条件发送空 C2S SyncMessage（每秒 20 包）的网络风暴：
 * 改为仅调用父类 containerTick，不再向服务端空发包（且 FlessCarcrassWhileThisGUIIsOpenTickProcedure 的关闭检测已由 stillValid 覆盖）。
 */
@Mixin({
        FlessCarcassGUIScreen.class,
        PuppetOfStrifeGUIScreen.class,
        UrnGUIScreen.class
})
public abstract class GuiScreenNoTickSpamMixin extends AbstractContainerScreen<AbstractContainerMenu> {
    protected GuiScreenNoTickSpamMixin() {
        super(null, null, null);
    }

    @Inject(method = "containerTick", at = @At("HEAD"), cancellable = true)
    private void sonsofsinsbugfix$stopEmptySyncPacketSpam(CallbackInfo ci) {
        super.containerTick();
        ci.cancel();
    }
}
