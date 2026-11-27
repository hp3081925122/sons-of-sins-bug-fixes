package com.hp.sonsofsinsbugfix.mixin.client;

import net.mcreator.sonsofsins.item.inventory.FleshCarcassInventoryCapability;
import net.mcreator.sonsofsins.item.inventory.PuppetOfStrifeInventoryCapability;
import net.mcreator.sonsofsins.item.inventory.UrnInventoryCapability;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复 MCR-06②（Forge 1.20.1）：屏蔽原模组错标 Dist.CLIENT 的 ItemTossEvent 客户端跨线程关容器逻辑，
 * 由服务端 SonsOfSinsBugFixEvents#onItemToss 统一安全关闭容器。
 */
@Mixin(value = {
        FleshCarcassInventoryCapability.class,
        PuppetOfStrifeInventoryCapability.class,
        UrnInventoryCapability.class
}, remap = false)
public abstract class PortableInventoryTossCancelMixin {
    @Inject(method = "onItemDropped", at = @At("HEAD"), cancellable = true)
    private static void sonsofsinsbugfix$cancelClientCrossThreadToss(ItemTossEvent event, CallbackInfo ci) {
        ci.cancel();
    }
}
