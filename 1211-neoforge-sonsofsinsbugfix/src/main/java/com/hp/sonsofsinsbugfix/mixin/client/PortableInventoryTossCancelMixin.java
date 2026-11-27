package com.hp.sonsofsinsbugfix.mixin.client;

import net.mcreator.sonsofsins.item.inventory.FleshCarcassInventoryCapability;
import net.mcreator.sonsofsins.item.inventory.PuppetOfStrifeInventoryCapability;
import net.mcreator.sonsofsins.item.inventory.UrnInventoryCapability;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复 MCR-06②（NeoForge 1.21.1）：屏蔽错标 Dist.CLIENT 的 ItemTossEvent 跨线程关容器逻辑。
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
