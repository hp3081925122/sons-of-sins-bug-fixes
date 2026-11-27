package com.hp.sonsofsinsbugfix.mixin;

import net.mcreator.sonsofsins.init.SonsOfSinsModItems;
import net.mcreator.sonsofsins.procedures.UrnItemInInventoryTickProcedure;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复 UrnItemInInventoryTickProcedure 两大严重漏洞：
 * 1. 空骨灰坛在背包靠前槽位时不再提前清除 isEmptying 标记（彻底堵死“空坛放 0 号槽无限白嫖以太充能”漏洞）；
 * 2. 槽位仅剩 1 份以太骨灰时正确扣减为空栈 ItemStack.EMPTY，而非写入 count = -1 的损坏负数物品。
 */
@Mixin(value = UrnItemInInventoryTickProcedure.class, remap = false)
public abstract class UrnItemTickFixMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void sonsofsinsbugfix$safeConsumeEtherAshes(Entity entity, ItemStack itemstack, CallbackInfo ci) {
        ci.cancel();
        if (entity == null || entity.level().isClientSide() || itemstack == null || itemstack.isEmpty()) {
            return;
        }
        if (!entity.getPersistentData().getBoolean("isEmptying")) {
            return;
        }
        itemstack.getCapability(ForgeCapabilities.ITEM_HANDLER, null).ifPresent(handler -> {
            if (!(handler instanceof IItemHandlerModifiable modifiable)) {
                return;
            }
            int slots = Math.min(9, modifiable.getSlots());
            for (int i = 0; i < slots; i++) {
                ItemStack slotStack = modifiable.getStackInSlot(i);
                if (!slotStack.isEmpty() && slotStack.getItem() == SonsOfSinsModItems.ETHER_ASHES.get()) {
                    int remaining = slotStack.getCount() - 2;
                    if (remaining <= 0) {
                        modifiable.setStackInSlot(i, ItemStack.EMPTY);
                    } else {
                        ItemStack updated = slotStack.copy();
                        updated.setCount(remaining);
                        modifiable.setStackInSlot(i, updated);
                    }
                    entity.getPersistentData().putBoolean("isEmptying", false);
                    return;
                }
            }
        });
    }
}
