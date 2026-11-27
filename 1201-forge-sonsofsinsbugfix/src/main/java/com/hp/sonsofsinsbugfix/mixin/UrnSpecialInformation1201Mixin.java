package com.hp.sonsofsinsbugfix.mixin;

import java.util.concurrent.atomic.AtomicInteger;
import net.mcreator.sonsofsins.init.SonsOfSinsModItems;
import net.mcreator.sonsofsins.procedures.UrnSpecialInformationProcedure;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 修复 1.20.1 骨灰坛悬浮提示硬编码英文字符串的问题，同时简化 9 格灰烬统计避免每帧 18 次匿名对象分配。
 */
@Mixin(value = UrnSpecialInformationProcedure.class, remap = false)
public abstract class UrnSpecialInformation1201Mixin {

    @Inject(method = "execute(Lnet/minecraft/world/item/ItemStack;)Ljava/lang/String;", at = @At("HEAD"), cancellable = true)
    private static void sonsofsinsbugfix$localizedAshCount(ItemStack itemstack, CallbackInfoReturnable<String> cir) {
        AtomicInteger count = new AtomicInteger(0);
        if (itemstack != null && !itemstack.isEmpty()) {
            itemstack.getCapability(ForgeCapabilities.ITEM_HANDLER, null).ifPresent(handler -> {
                int slots = Math.min(9, handler.getSlots());
                for (int i = 0; i < slots; i++) {
                    ItemStack stack = handler.getStackInSlot(i);
                    if (!stack.isEmpty() && stack.getItem() == SonsOfSinsModItems.ETHER_ASHES.get()) {
                        count.addAndGet(stack.getCount());
                    }
                }
            });
        }
        cir.setReturnValue("§5§o" + Component.translatable("item.sons_of_sins.urn.contains", String.valueOf(count.get())).getString());
    }
}
